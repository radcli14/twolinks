import UIKit
import SwiftUI
import SceneViewSwift
import RealityKit
import ComposeApp

@objc class TwoLinksSceneDelegate: NSObject, IosSceneProvider {

    // Geometry constants matching Kotlin ViewModel
    private let doorSize = SIMD3<Float>(0.91, 2.03, 0.035)
    private let pivotRadius: Float = 0.01
    private let pivotHeight: Float = 0.015
    private let link1Thickness: Float = 0.0064

    // ViewModel reference stored by createView(viewModel:)
    private weak var viewModel: MainViewModel?

    // Dynamic entity references updated each frame
    private var link1AnchorEntity: Entity?
    private var link1Entity: ModelEntity?
    private var pivot2AnchorEntity: Entity?
    private var pivot2Entity: ModelEntity?
    private var link2AnchorEntity: Entity?
    private var link2Entity: ModelEntity?

    // Pending colors flushed after scene setup when entities become non-nil.
    private var pendingLink1Color = SIMD3<Float>(repeating: -1)
    private var pendingLink2Color = SIMD3<Float>(repeating: -1)
    private var lastLink1Color    = SIMD3<Float>(repeating: -1)
    private var lastLink2Color    = SIMD3<Float>(repeating: -1)

    private lazy var hostingController: UIHostingController<AnyView> = {
        let sceneView = SceneView { [weak self] root in
            guard let self else { return }

            // A 180° Y-rotation wrapper makes the camera at +Z face the mechanism
            // side of the door on launch, without touching DerivedData camera setup.
            let wrapper = Entity()
            wrapper.orientation = simd_quatf(angle: .pi, axis: [0, 1, 0])
            root.addChild(wrapper)

            // --- Door (static, metallic gray panel) ---
            let doorMesh = MeshResource.generateBox(size: self.doorSize)
            var doorMat = PhysicallyBasedMaterial()
            doorMat.baseColor = .init(tint: UIColor(white: 0.75, alpha: 1))
            doorMat.metallic = .init(floatLiteral: 0.97)
            doorMat.roughness = .init(floatLiteral: 0.05)
            let doorEntity = ModelEntity(mesh: doorMesh, materials: [doorMat])
            doorEntity.position = SIMD3<Float>(0, 0, 0.5 * self.doorSize.z)
            wrapper.addChild(doorEntity)

            // --- Pivot 1 (static cylinder at origin, lying along Z) ---
            let p1Mesh = MeshResource.generateCylinder(height: self.pivotHeight, radius: self.pivotRadius)
            let p1Mat = SimpleMaterial(color: UIColor(white: 0.69, alpha: 1), isMetallic: false)
            let pivot1 = ModelEntity(mesh: p1Mesh, materials: [p1Mat])
            pivot1.orientation = simd_quatf(angle: .pi / 2, axis: [1, 0, 0])
            pivot1.position = SIMD3<Float>(0, 0, -self.link1Thickness)
            wrapper.addChild(pivot1)

            // --- Link 1 anchor (rotates around Z, carries link1 and pivot2 hierarchy) ---
            let link1Anchor = Entity()
            wrapper.addChild(link1Anchor)
            self.link1AnchorEntity = link1Anchor

            // Link 1 visual (unit cube scaled per-frame to link dimensions)
            let link1Mesh = MeshResource.generateBox(size: 1.0)
            let link1 = ModelEntity(mesh: link1Mesh, materials: [SimpleMaterial(color: .red, isMetallic: true)])
            link1Anchor.addChild(link1)
            self.link1Entity = link1

            // --- Pivot 2 anchor (translated along link1 to the hinge point) ---
            let pivot2Anchor = Entity()
            link1Anchor.addChild(pivot2Anchor)
            self.pivot2AnchorEntity = pivot2Anchor

            // Pivot 2 cylinder (lying along Z, same orientation as pivot 1)
            let p2Mesh = MeshResource.generateCylinder(height: self.pivotHeight, radius: self.pivotRadius)
            let p2Mat = SimpleMaterial(color: UIColor(white: 0.5, alpha: 1), isMetallic: false)
            let pivot2 = ModelEntity(mesh: p2Mesh, materials: [p2Mat])
            pivot2.orientation = simd_quatf(angle: .pi / 2, axis: [1, 0, 0])
            pivot2Anchor.addChild(pivot2)
            self.pivot2Entity = pivot2

            // --- Link 2 anchor (rotates around Z relative to pivot2) ---
            let link2Anchor = Entity()
            pivot2Anchor.addChild(link2Anchor)
            self.link2AnchorEntity = link2Anchor

            // Link 2 visual (unit cube scaled per-frame)
            let link2Mesh = MeshResource.generateBox(size: 1.0)
            let link2 = ModelEntity(mesh: link2Mesh, materials: [SimpleMaterial(color: .blue, isMetallic: true)])
            link2Anchor.addChild(link2)
            self.link2Entity = link2

            // Flush any colors that arrived before entities were ready.
            self.applyPendingColors()

            // --- Async planet loading ---
            // Planets go directly under root (not the wrapper) so their world-space
            // positions are unchanged by the 180° wrapper flip.
            Task { @MainActor [weak root] in
                guard let root else { return }
                await Self.loadPlanet(
                    named: "moon",
                    scale: 27,
                    position: SIMD3<Float>(0, -14.515, 0),
                    xRotationDeg: 69,
                    yRotationDeg: 0,
                    fallbackColor: UIColor(white: 0.55, alpha: 1),
                    into: root
                )
                await Self.loadPlanet(
                    named: "earth",
                    scale: 100,
                    position: SIMD3<Float>(31.4, -15.7, -314),
                    xRotationDeg: 22,
                    yRotationDeg: 90,
                    fallbackColor: UIColor(red: 0.2, green: 0.4, blue: 0.8, alpha: 1),
                    into: root
                )
            }
        }
        .cameraControls(.orbit)
        .environment(.custom(name: "NightSky", hdrFile: "NightSky"))
        .edgesIgnoringSafeArea(.all)

        let hc = UIHostingController(rootView: AnyView(sceneView))
        hc.view.backgroundColor = .clear
        return hc
    }()

    @objc func createView(viewModel: MainViewModel) -> UIView {
        self.viewModel = viewModel
        return hostingController.view
    }

    @objc func update() {
        guard let vm = viewModel else { return }
        
        let state = vm.twoLinks
        let l0 = state.links[0]
        let l1 = state.links[1]

        // center and size are Kotlin extension properties — compute inline
        let l0Center = SIMD3<Float>(l0.offset, 0, 0.5 * l0.thickness)
        let l0Size   = SIMD3<Float>(l0.length, l0.height, l0.thickness)
        let l1Center = SIMD3<Float>(l1.offset, 0, 0.5 * l1.thickness)
        let l1Size   = SIMD3<Float>(l1.length, l1.height, l1.thickness)
        let pivot    = state.pivotPosition

        // The 180° Y wrapper negates world X and Z; negate angles and X/Z to compensate.
        link1AnchorEntity?.orientation = simd_quatf(angle: -vm.linkOneRotation.z * .pi / 180, axis: [0, 0, 1])
        link1Entity?.position = SIMD3<Float>(-l0Center.x, l0Center.y, -l0Center.z)
        link1Entity?.scale    = l0Size

        pivot2AnchorEntity?.position = SIMD3<Float>(-pivot.x, pivot.y, -pivot.z)

        link2AnchorEntity?.orientation = simd_quatf(angle: -vm.linkTwoRotation.z * .pi / 180, axis: [0, 0, 1])
        link2Entity?.position = SIMD3<Float>(-l1Center.x, l1Center.y, -l1Center.z)
        link2Entity?.scale    = l1Size

        let c0 = l0.color
        let c1 = l1.color
        pendingLink1Color = SIMD3<Float>(c0.x, c0.y, c0.z)
        pendingLink2Color = SIMD3<Float>(c1.x, c1.y, c1.z)
        applyPendingColors()
    }

    private func applyPendingColors() {
        if pendingLink1Color != lastLink1Color, let entity = link1Entity {
            lastLink1Color = pendingLink1Color
            var mat = SimpleMaterial()
            mat.color    = .init(tint: UIColor(red:   CGFloat(pendingLink1Color.x),
                                               green: CGFloat(pendingLink1Color.y),
                                               blue:  CGFloat(pendingLink1Color.z),
                                               alpha: 1))
            mat.metallic  = .init(floatLiteral: 0.5)
            mat.roughness = .init(floatLiteral: 0.4)
            entity.model?.materials = [mat]
        }

        if pendingLink2Color != lastLink2Color, let entity = link2Entity {
            lastLink2Color = pendingLink2Color
            var mat = SimpleMaterial()
            mat.color    = .init(tint: UIColor(red:   CGFloat(pendingLink2Color.x),
                                               green: CGFloat(pendingLink2Color.y),
                                               blue:  CGFloat(pendingLink2Color.z),
                                               alpha: 1))
            mat.metallic  = .init(floatLiteral: 0.5)
            mat.roughness = .init(floatLiteral: 0.4)
            entity.model?.materials = [mat]
        }
    }

    // MARK: - Planet loading

    @MainActor
    private static func loadPlanet(
        named name: String,
        scale: Float,
        position: SIMD3<Float>,
        xRotationDeg: Float,
        yRotationDeg: Float,
        fallbackColor: UIColor,
        into root: Entity
    ) async {
        let entity: Entity
        if let node = try? await ModelNode.load("\(name).usdz") {
            node.scaleToUnits(scale)
            entity = node.entity
        } else {
            let sphere = GeometryNode.sphere(radius: scale * 0.5, color: fallbackColor)
            entity = sphere.entity
        }
        entity.position    = position
        entity.orientation = simd_quatf(angle: xRotationDeg * .pi / 180, axis: [1, 0, 0]) * simd_quatf(angle: yRotationDeg * .pi / 180, axis: [0, 1, 0])
        root.addChild(entity)
    }
}
