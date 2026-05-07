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

    // Dynamic entity references updated each frame
    private var link1AnchorEntity: Entity?
    private var link1Entity: ModelEntity?
    private var pivot2AnchorEntity: Entity?
    private var pivot2Entity: ModelEntity?
    private var link2AnchorEntity: Entity?
    private var link2Entity: ModelEntity?

    // Color cache to avoid material recreation every frame
    private var lastLink1Color = SIMD3<Float>(repeating: -1)
    private var lastLink2Color = SIMD3<Float>(repeating: -1)

    private lazy var hostingController: UIHostingController<AnyView> = {
        let sceneView = SceneView { [weak self] root in
            guard let self else { return }

            // --- Door (static, metallic gray panel) ---
            let doorMesh = MeshResource.generateBox(size: self.doorSize)
            var doorMat = PhysicallyBasedMaterial()
            doorMat.baseColor = .init(tint: UIColor(white: 0.75, alpha: 1))
            doorMat.metallic = .init(floatLiteral: 0.97)
            doorMat.roughness = .init(floatLiteral: 0.05)
            let doorEntity = ModelEntity(mesh: doorMesh, materials: [doorMat])
            // Offset back so the door surface sits at z=0
            doorEntity.position = SIMD3<Float>(0, 0, -0.5 * self.doorSize.z)
            root.addChild(doorEntity)

            // --- Pivot 1 (static cylinder at origin, lying along Z) ---
            let p1Mesh = MeshResource.generateCylinder(height: self.pivotHeight, radius: self.pivotRadius)
            let p1Mat = SimpleMaterial(color: UIColor(white: 0.69, alpha: 1), isMetallic: false)
            let pivot1 = ModelEntity(mesh: p1Mesh, materials: [p1Mat])
            pivot1.orientation = simd_quatf(angle: .pi / 2, axis: [1, 0, 0])
            pivot1.position = SIMD3<Float>(0, 0, self.link1Thickness)
            root.addChild(pivot1)

            // --- Link 1 anchor (rotates around Z, carries link1 and pivot2 hierarchy) ---
            let link1Anchor = Entity()
            root.addChild(link1Anchor)
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
        }
        .cameraControls(.orbit)

        let hc = UIHostingController(rootView: AnyView(sceneView))
        hc.view.backgroundColor = .clear
        return hc
    }()

    @objc func createView() -> UIView {
        hostingController.view
    }

    @objc func updateTransforms(l1Deg: Float, l2Deg: Float,
                                 pivotX: Float, pivotY: Float, pivotZ: Float,
                                 l1CenterX: Float, l1CenterY: Float, l1CenterZ: Float,
                                 l1SizeX: Float, l1SizeY: Float, l1SizeZ: Float,
                                 l2CenterX: Float, l2CenterY: Float, l2CenterZ: Float,
                                 l2SizeX: Float, l2SizeY: Float, l2SizeZ: Float) {
        // Link 1 rotation (around Z axis)
        link1AnchorEntity?.orientation = simd_quatf(angle: l1Deg * .pi / 180, axis: [0, 0, 1])

        // Link 1 visual: position = center offset, scale = physical dimensions
        link1Entity?.position = SIMD3<Float>(l1CenterX, l1CenterY, l1CenterZ)
        link1Entity?.scale    = SIMD3<Float>(l1SizeX,   l1SizeY,   l1SizeZ)

        // Pivot 2 position along link1 (in link1's local space)
        pivot2AnchorEntity?.position = SIMD3<Float>(pivotX, pivotY, pivotZ)

        // Link 2 rotation relative to the pivot2 anchor
        link2AnchorEntity?.orientation = simd_quatf(angle: l2Deg * .pi / 180, axis: [0, 0, 1])

        // Link 2 visual: position = center offset, scale = physical dimensions
        link2Entity?.position = SIMD3<Float>(l2CenterX, l2CenterY, l2CenterZ)
        link2Entity?.scale    = SIMD3<Float>(l2SizeX,   l2SizeY,   l2SizeZ)
    }

    @objc func updateColors(r1: Float, g1: Float, b1: Float, r2: Float, g2: Float, b2: Float) {
        let newL1 = SIMD3<Float>(r1, g1, b1)
        if newL1 != lastLink1Color {
            lastLink1Color = newL1
            var mat = SimpleMaterial()
            mat.color    = .init(tint: UIColor(red: CGFloat(r1), green: CGFloat(g1), blue: CGFloat(b1), alpha: 1))
            mat.metallic  = .init(floatLiteral: 0.5)
            mat.roughness = .init(floatLiteral: 0.4)
            link1Entity?.model?.materials = [mat]
        }

        let newL2 = SIMD3<Float>(r2, g2, b2)
        if newL2 != lastLink2Color {
            lastLink2Color = newL2
            var mat = SimpleMaterial()
            mat.color    = .init(tint: UIColor(red: CGFloat(r2), green: CGFloat(g2), blue: CGFloat(b2), alpha: 1))
            mat.metallic  = .init(floatLiteral: 0.5)
            mat.roughness = .init(floatLiteral: 0.4)
            link2Entity?.model?.materials = [mat]
        }
    }
}
