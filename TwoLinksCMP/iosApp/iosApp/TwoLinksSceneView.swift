import SwiftUI
import SceneViewSwift
import RealityKit
import ComposeApp

struct TwoLinksSceneView: View {
    let viewModel: MainViewModel

    @State private var sceneState = TwoLinksSceneState()
    @State private var sunLight = LightNode.directional(color: .warm, intensity: 10_000, castsShadow: true)

    private let pivotRadius: Float = 0.01
    private let pivotHeight: Float = 0.015
    private let link1Thickness: Float = 0.0064

    var body: some View {
        let _ = sunLight.entity.look(at: .zero, from: Planet.companion.sun.position.asSIMD3, relativeTo: nil)
        return TimelineView(.animation) { context in
            SceneView { root in
                buildScene(root: root)
            }
            .cameraControls(.orbit)
            .autoCenterContent(false)
            .environment(.custom(name: "NightSky", hdrFile: "NightSky"))
            .mainLight(.custom(sunLight))
            .fillLight(.disabled)
            .edgesIgnoringSafeArea(.all)
            .onChange(of: context.date) { _, _ in
                let nanos = Int64(ProcessInfo.processInfo.systemUptime * 1_000_000_000)
                viewModel.updateOnFrame(frameTime: nanos)
                applyTransforms()
                applyColors()
            }
        }
    }

    private func buildScene(root: Entity) {
        // 180° Y-rotation wrapper so the camera at +Z faces the mechanism side
        let wrapper = Entity()
        wrapper.orientation = simd_quatf(angle: .pi, axis: [0, 1, 0])
        root.addChild(wrapper)

        // Door (static, metallic gray panel)
        let doorMesh = MeshResource.generateBox(size: viewModel.doorSize.asSIMD3)
        var doorMat = PhysicallyBasedMaterial()
        doorMat.baseColor = .init(tint: UIColor(white: 0.75, alpha: 1))
        doorMat.metallic = .init(floatLiteral: 0.97)
        doorMat.roughness = .init(floatLiteral: 0.05)
        let doorEntity = ModelEntity(mesh: doorMesh, materials: [doorMat])
        doorEntity.position = SIMD3<Float>(0, 0, 0.5 * viewModel.doorSize.z)
        wrapper.addChild(doorEntity)

        // Pivot 1 (static cylinder at origin, lying along Z)
        let p1Mesh = MeshResource.generateCylinder(height: pivotHeight, radius: pivotRadius)
        let p1Mat = SimpleMaterial(color: UIColor(white: 0.69, alpha: 1), isMetallic: false)
        let pivot1 = ModelEntity(mesh: p1Mesh, materials: [p1Mat])
        pivot1.orientation = simd_quatf(angle: .pi / 2, axis: [1, 0, 0])
        pivot1.position = SIMD3<Float>(0, 0, -link1Thickness)
        wrapper.addChild(pivot1)

        // Link 1 anchor (rotates around Z, carries link1 and pivot2 hierarchy)
        let link1Anchor = Entity()
        wrapper.addChild(link1Anchor)
        sceneState.link1AnchorEntity = link1Anchor

        // Link 1 visual (unit cube scaled each frame to link dimensions)
        let link1Mesh = MeshResource.generateBox(size: 1.0)
        let link1 = ModelEntity(mesh: link1Mesh, materials: [SimpleMaterial(color: .red, isMetallic: true)])
        link1Anchor.addChild(link1)
        sceneState.link1Entity = link1

        // Pivot 2 anchor (translated along link1 to the hinge point each frame)
        let pivot2Anchor = Entity()
        link1Anchor.addChild(pivot2Anchor)
        sceneState.pivot2AnchorEntity = pivot2Anchor

        // Pivot 2 cylinder (lying along Z)
        let p2Mesh = MeshResource.generateCylinder(height: pivotHeight, radius: pivotRadius)
        let p2Mat = SimpleMaterial(color: UIColor(white: 0.5, alpha: 1), isMetallic: false)
        let pivot2 = ModelEntity(mesh: p2Mesh, materials: [p2Mat])
        pivot2.orientation = simd_quatf(angle: .pi / 2, axis: [1, 0, 0])
        pivot2Anchor.addChild(pivot2)

        // Link 2 anchor (rotates around Z relative to pivot2)
        let link2Anchor = Entity()
        pivot2Anchor.addChild(link2Anchor)
        sceneState.link2AnchorEntity = link2Anchor

        // Link 2 visual (unit cube scaled each frame)
        let link2Mesh = MeshResource.generateBox(size: 1.0)
        let link2 = ModelEntity(mesh: link2Mesh, materials: [SimpleMaterial(color: .blue, isMetallic: true)])
        link2Anchor.addChild(link2)
        sceneState.link2Entity = link2

        // Async planet loading — placed under entities.root (root.parent), not contentRoot,
        // so the planets' large bounding boxes don't inflate the shadow frustum.
        Task { @MainActor [weak root] in
            guard let root else { return }
            await loadPlanets(into: root.parent ?? root)
        }
    }

    private func applyTransforms() {
        let twoLinks = viewModel.twoLinks

        // The 180° Y wrapper negates world X and Z; negate angles and positions to compensate
        sceneState.link1AnchorEntity?.orientation = twoLinks.linkOneOrientation
        sceneState.link1Entity?.position = twoLinks.linkOnePosition
        sceneState.link1Entity?.scale = twoLinks.linkOneScale

        sceneState.pivot2AnchorEntity?.position = -twoLinks.pivotPosition.asSIMD3

        sceneState.link2AnchorEntity?.orientation = twoLinks.linkTwoOrientation
        sceneState.link2Entity?.position = twoLinks.linkTwoPosition
        sceneState.link2Entity?.scale = twoLinks.linkTwoScale
    }

    private func applyColors() {
        let twoLinks = viewModel.twoLinks
        guard let l0 = twoLinks.links.first, let l1 = twoLinks.links.last else { return }

        let c0 = SIMD3<Float>(l0.color.x, l0.color.y, l0.color.z)
        let c1 = SIMD3<Float>(l1.color.x, l1.color.y, l1.color.z)

        if c0 != sceneState.lastLink0Color, let entity = sceneState.link1Entity {
            sceneState.lastLink0Color = c0
            applyColor(l0.color.asUIColor, to: entity)
        }

        if c1 != sceneState.lastLink1Color, let entity = sceneState.link2Entity {
            sceneState.lastLink1Color = c1
            applyColor(l1.color.asUIColor, to: entity)
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
    private func loadPlanets(into root: Entity) async {
        await Self.loadPlanet(representing: Planet.companion.moon, into: root)
        await Self.loadPlanet(representing: Planet.companion.earth, into: root)
        // TODO: there is an extra shadown being cast when I add the sun entity, it doesn't match the sun location or shape, but I can't trace the source. Omitting it for now.
        //await Self.loadPlanet(representing: Planet.companion.sun, into: root)
    }

    @MainActor
    private static func loadPlanet(
        representing planet: Planet,
        into root: Entity
    ) async {
        let entity: Entity
        if let node = try? await ModelNode.load(planet.file) {
            node.scaleToUnits(planet.scale)
            entity = node.entity
        } else {
            let sphere = GeometryNode.sphere(radius: planet.scale * 0.5, color: planet.color.asUIColor)
            entity = sphere.entity
        }
        entity.position = planet.position.asSIMD3
        entity.orientation = planet.rotation.asQuatf
        root.addChild(entity)
    }
}

extension Float {
    static let deg2rad: Float = .pi / 180
}
