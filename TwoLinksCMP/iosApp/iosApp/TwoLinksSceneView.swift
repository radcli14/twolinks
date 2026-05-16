import SwiftUI
import SceneViewSwift
import RealityKit

struct TwoLinksSceneView: View {
    @Environment(TwoLinksSceneState.self) private var state

    private let doorSize    = SIMD3<Float>(0.91, 2.03, 0.035)
    private let pivotRadius: Float = 0.01
    private let pivotHeight: Float = 0.015
    private let link1Thickness: Float = 0.0064

    var body: some View {
        SceneView { root in
            buildScene(root: root)
        }
        .cameraControls(.orbit)
        .environment(.custom(name: "NightSky", hdrFile: "NightSky"))
        .edgesIgnoringSafeArea(.all)
    }

    private func buildScene(root: Entity) {
        // 180° Y-rotation wrapper so the camera at +Z faces the mechanism side
        let wrapper = Entity()
        wrapper.orientation = simd_quatf(angle: .pi, axis: [0, 1, 0])
        root.addChild(wrapper)

        // Door (static, metallic gray panel)
        let doorMesh = MeshResource.generateBox(size: doorSize)
        var doorMat = PhysicallyBasedMaterial()
        doorMat.baseColor = .init(tint: UIColor(white: 0.75, alpha: 1))
        doorMat.metallic = .init(floatLiteral: 0.97)
        doorMat.roughness = .init(floatLiteral: 0.05)
        let doorEntity = ModelEntity(mesh: doorMesh, materials: [doorMat])
        doorEntity.position = SIMD3<Float>(0, 0, 0.5 * doorSize.z)
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
        state.link1AnchorEntity = link1Anchor

        // Link 1 visual (unit cube scaled each frame to link dimensions)
        let link1Mesh = MeshResource.generateBox(size: 1.0)
        let link1 = ModelEntity(mesh: link1Mesh, materials: [SimpleMaterial(color: .red, isMetallic: true)])
        link1Anchor.addChild(link1)
        state.link1Entity = link1

        // Pivot 2 anchor (translated along link1 to the hinge point each frame)
        let pivot2Anchor = Entity()
        link1Anchor.addChild(pivot2Anchor)
        state.pivot2AnchorEntity = pivot2Anchor

        // Pivot 2 cylinder (lying along Z)
        let p2Mesh = MeshResource.generateCylinder(height: pivotHeight, radius: pivotRadius)
        let p2Mat = SimpleMaterial(color: UIColor(white: 0.5, alpha: 1), isMetallic: false)
        let pivot2 = ModelEntity(mesh: p2Mesh, materials: [p2Mat])
        pivot2.orientation = simd_quatf(angle: .pi / 2, axis: [1, 0, 0])
        pivot2Anchor.addChild(pivot2)

        // Link 2 anchor (rotates around Z relative to pivot2)
        let link2Anchor = Entity()
        pivot2Anchor.addChild(link2Anchor)
        state.link2AnchorEntity = link2Anchor

        // Link 2 visual (unit cube scaled each frame)
        let link2Mesh = MeshResource.generateBox(size: 1.0)
        let link2 = ModelEntity(mesh: link2Mesh, materials: [SimpleMaterial(color: .blue, isMetallic: true)])
        link2Anchor.addChild(link2)
        state.link2Entity = link2

        // Async planet loading — placed under root (not wrapper) to keep world-space positions
        Task { @MainActor [weak root] in
            guard let root else { return }
            await Self.loadPlanet(
                named: "moon",
                scale: 27,
                position: SIMD3<Float>(0, -14.515, 0),
                xRotationDeg: 69, yRotationDeg: 0,
                fallbackColor: UIColor(white: 0.55, alpha: 1),
                into: root
            )
            await Self.loadPlanet(
                named: "earth",
                scale: 100,
                position: SIMD3<Float>(31.4, -15.7, -314),
                xRotationDeg: 22, yRotationDeg: 90,
                fallbackColor: UIColor(red: 0.2, green: 0.4, blue: 0.8, alpha: 1),
                into: root
            )
        }
    }

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
        entity.position = position
        entity.orientation = simd_quatf(angle: xRotationDeg * .pi / 180, axis: [1, 0, 0])
            * simd_quatf(angle: yRotationDeg * .pi / 180, axis: [0, 1, 0])
        root.addChild(entity)
    }
}
