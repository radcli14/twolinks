import ComposeApp
import Observation
import RealityKit
import SceneViewSwift
import UIKit

@Observable class SceneManager {

    // Entity refs — set once during SceneView buildScene, used by controller each frame
    weak var link1AnchorEntity: Entity?
    weak var link2AnchorEntity: Entity?
    weak var pivot2AnchorEntity: Entity?
    weak var link1Entity: ModelEntity?
    weak var link2Entity: ModelEntity?

    // Color change detection
    var lastLink0Color = SIMD3<Float>(repeating: -1)
    var lastLink1Color = SIMD3<Float>(repeating: -1)
    private var lastIsPaused = false

    // Particle emitter at the tip of link2
    weak var particleEmitterEntity: Entity?

    // Planet entity cache — populated on first load, cloned on subsequent scene builds
    private var planetCache: [String: Entity] = [:]
    
    // Constant dimensions
    private let door = StaticObjects.Door.shared
    private let pivot = StaticObjects.Pivot.shared

    // MARK: - Build
    
    func buildScene(root: Entity, representing viewModel: MainViewModel, includeParticles: Bool = true) {
        lastLink0Color = SIMD3(repeating: -1)
        lastLink1Color = SIMD3(repeating: -1)
        // 180° Y-rotation wrapper so the camera at +Z faces the mechanism side
        let wrapper = Entity()
        wrapper.orientation = simd_quatf(angle: .pi, axis: [0, 1, 0])
        root.addChild(wrapper)

        // Door (static, metallic gray panel)
        let doorMesh = MeshResource.generateBox(size: door.size.asSIMD3)
        var doorMat = PhysicallyBasedMaterial()
        doorMat.baseColor = .init(tint: door.color.asUIColor)
        doorMat.metallic = .init(floatLiteral: door.metallic)
        doorMat.roughness = .init(floatLiteral: door.roughness)
        let doorEntity = ModelEntity(mesh: doorMesh, materials: [doorMat])
        doorEntity.position = SIMD3<Float>(0, 0, 0.5 * door.thickness)
        wrapper.addChild(doorEntity)

        // Pivot 1 (static cylinder at origin, lying along Z)
        let pivot1Anchor = Entity()
        pivot1Anchor.position = SIMD3<Float>(0, 0, -0.5 * pivot.height)
        wrapper.addChild(pivot1Anchor)
        
        let p1Mesh = MeshResource.generateCylinder(height: pivot.height, radius: pivot.radius)
        var pivotMat = PhysicallyBasedMaterial()
        pivotMat.baseColor = .init(tint: pivot.color.asUIColor)
        pivotMat.metallic = .init(floatLiteral: pivot.metallic)
        pivotMat.roughness = .init(floatLiteral: pivot.roughness)
        let pivot1 = ModelEntity(mesh: p1Mesh, materials: [pivotMat])
        pivot1.orientation = simd_quatf(angle: .pi / 2, axis: [1, 0, 0])
        pivot1Anchor.addChild(pivot1)

        // Link 1 anchor (rotates around Z, carries link1 and pivot2 hierarchy)
        let link1Anchor = Entity()
        pivot1Anchor.addChild(link1Anchor)
        link1AnchorEntity = link1Anchor

        // Link 1 visual (unit cube scaled each frame to link dimensions)
        let link1Mesh = MeshResource.generateBox(size: 1.0)
        let link1 = ModelEntity(mesh: link1Mesh, materials: [SimpleMaterial(color: .red, isMetallic: true)])
        link1Anchor.addChild(link1)
        link1Entity = link1

        // Pivot 2 anchor (translated along link1 to the hinge point each frame)
        let pivot2Anchor = Entity()
        link1Anchor.addChild(pivot2Anchor)
        pivot2AnchorEntity = pivot2Anchor

        // Pivot 2 cylinder (lying along Z)
        let p2Mesh = MeshResource.generateCylinder(height: pivot.height, radius: pivot.radius)
        let pivot2 = ModelEntity(mesh: p2Mesh, materials: [pivotMat])
        pivot2.orientation = simd_quatf(angle: .pi / 2, axis: [1, 0, 0])
        pivot2Anchor.addChild(pivot2)

        // Link 2 anchor (rotates around Z relative to pivot2)
        let link2Anchor = Entity()
        pivot2Anchor.addChild(link2Anchor)
        link2AnchorEntity = link2Anchor

        // Link 2 visual (unit cube scaled each frame)
        let link2Mesh = MeshResource.generateBox(size: 1.0)
        let link2 = ModelEntity(mesh: link2Mesh, materials: [SimpleMaterial(color: .blue, isMetallic: true)])
        link2Anchor.addChild(link2)
        link2Entity = link2

        // Particle emitter at the tip of link2, child of link2AnchorEntity (rotation-only, unscaled).
        // Z-rotation never changes the Z direction, so emission in local -Z is always world +Z
        // (outward from the door toward the camera) regardless of the pendulum angle.
        // Omitted in AR mode: RealityView's spatialTracking render pass lacks the stencil
        // attachment that ParticleEmitterComponent's alpha-fade pipeline requires.
        if includeParticles, let l1 = viewModel.twoLinks.links.last, let link2AnchorEntity {
            let tipEntity = Entity()
            tipEntity.position = SIMD3<Float>(-l1.endDistance, 0, 0)
            // Orient local +Y toward link2Anchor's -Z (= world +Z, outward from door)
            tipEntity.orientation = simd_quatf(angle: -.pi / 2, axis: [1, 0, 0])
            var emitter = ParticleEmitterComponent()
            emitter.emitterShape = .point
            emitter.mainEmitter.lifeSpan = 6.28
            emitter.mainEmitter.color = .evolving(
                start: .single(.white),
                end: .single(l1.color.asUIColor)
            )
            emitter.speed = 0.0628
            tipEntity.components.set(emitter)
            link2AnchorEntity.addChild(tipEntity)
            particleEmitterEntity = tipEntity
        }

        // Async planet loading — placed under entities.root (root.parent), not contentRoot,
        // so the planets' large bounding boxes don't inflate the shadow frustum.
        Task { @MainActor [weak root] in
            guard let root else { return }
            await loadPlanets(into: root)
        }
    }
    
    // MARK: - Frame Update

    func updateOnFrame(viewModel: MainViewModel) {
        let nanos = Int64(ProcessInfo.processInfo.systemUptime * 1_000_000_000)
        viewModel.updateOnFrame(frameTime: nanos)
        applyTransforms(from: viewModel.twoLinks)
        applyColors(from: viewModel.twoLinks)
        let isPaused = viewModel.isPaused.value as? Bool ?? false
        if isPaused != lastIsPaused {
            lastIsPaused = isPaused
            if var comp = particleEmitterEntity?.components[ParticleEmitterComponent.self] {
                comp.isEmitting = !isPaused
                particleEmitterEntity?.components[ParticleEmitterComponent.self] = comp
            }
        }
    }

    // MARK: - Update
    
    func applyTransforms(from twoLinks: TwoLinks) {
        // The 180° Y wrapper negates world X and Z; negate angles and positions to compensate
        link1AnchorEntity?.orientation = twoLinks.linkOneOrientation
        link1Entity?.position = twoLinks.linkOnePosition
        link1Entity?.scale = twoLinks.linkOneScale

        pivot2AnchorEntity?.position = -twoLinks.pivotPosition.asSIMD3

        link2AnchorEntity?.orientation = twoLinks.linkTwoOrientation
        link2Entity?.position = twoLinks.linkTwoPosition
        link2Entity?.scale = twoLinks.linkTwoScale

        if let l1 = twoLinks.links.last {
            particleEmitterEntity?.position.x = -l1.endDistance
        }
    }

    func applyColors(from twoLinks: TwoLinks) {
        guard let l0 = twoLinks.links.first, let l1 = twoLinks.links.last else { return }

        let c0 = SIMD3<Float>(l0.color.x, l0.color.y, l0.color.z)
        let c1 = SIMD3<Float>(l1.color.x, l1.color.y, l1.color.z)

        if c0 != lastLink0Color, let link1Entity {
            lastLink0Color = c0
            applyColor(l0.color.asUIColor, to: link1Entity)
        }

        if c1 != lastLink1Color, let link2Entity {
            lastLink1Color = c1
            applyColor(l1.color.asUIColor, to: link2Entity)
            if var comp = particleEmitterEntity?.components[ParticleEmitterComponent.self] {
                comp.mainEmitter.color = .evolving(start: .single(.white), end: .single(l1.color.asUIColor))
                particleEmitterEntity?.components[ParticleEmitterComponent.self] = comp
            }
        }
    }
    
    private func applyColor(_ color: UIColor, to link: ModelEntity) {
        var mat = SimpleMaterial()
        mat.color    = .init(tint: color)
        mat.metallic  = .init(floatLiteral: 0.5)
        mat.roughness = .init(floatLiteral: 0.4)
        link.model?.materials = [mat]
    }
    
    
    // MARK: - Planets
    
    @MainActor
    func loadPlanets(into root: Entity) async {
        await loadPlanet(representing: Planet.companion.moon, into: root)
        await loadPlanet(representing: Planet.companion.earth, into: root)
        // TODO: there is an extra shadown being cast when I add the sun entity, it doesn't match the sun location or shape, but I can't trace the source. Omitting it for now.
        //await loadPlanet(representing: Planet.companion.sun, into: root)
    }

    @MainActor
    func loadPlanet(representing planet: Planet, into root: Entity) async {
        // Fast path: clone the cached entity directly
        if let cached = planetCache[planet.file] {
            root.addChild(cached.clone(recursive: true))
            return
        }

        // Add placeholder sphere immediately at full opacity
        let sphere = GeometryNode.sphere(radius: planet.scale * 0.5, color: planet.color.asUIColor)
        let placeholder = sphere.entity
        placeholder.position = planet.position.asSIMD3
        placeholder.orientation = planet.rotation.asQuatf
        placeholder.components.set(OpacityComponent(opacity: 1))
        root.addChild(placeholder)

        // Load the model; leave the placeholder in place on failure
        guard let node = try? await ModelNode.load(planet.file) else { return }
        node.scaleToUnits(planet.scale)
        let model = node.entity
        model.position = planet.position.asSIMD3
        model.orientation = planet.rotation.asQuatf

        // Cache a clone before adding the opacity animation
        planetCache[planet.file] = model.clone(recursive: true)

        model.components.set(OpacityComponent(opacity: 0))
        root.addChild(model)

        // Cross-fade: model fades in while placeholder fades out
        let duration: TimeInterval = 1.0
        model.fadeOpacity(from: 0, to: 1, over: duration)
        placeholder.fadeOpacity(from: 1, to: 0, over: duration)
        DispatchQueue.main.asyncAfter(deadline: .now() + duration) {
            placeholder.removeFromParent()
        }
    }
}
