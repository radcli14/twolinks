import UIKit
import SwiftUI
import RealityKit
import ComposeApp

class TwoLinksSceneViewController: UIHostingController<AnyView> {

    let sceneState = TwoLinksSceneState()

    init() {
        super.init(rootView: AnyView(EmptyView()))
        rootView = AnyView(TwoLinksSceneView().environment(sceneState))
        view.backgroundColor = .clear
    }

    @MainActor required dynamic init?(coder: NSCoder) { fatalError("init(coder:) not supported") }

    func update(viewModel: MainViewModel) {
        let twoLinks = viewModel.twoLinks
        let l0 = twoLinks.links[0] as! ComposeApp.Link
        let l1 = twoLinks.links[1] as! ComposeApp.Link
        let pivot = twoLinks.pivotPosition

        applyTransforms(
            link1AngleDeg: viewModel.linkOneRotation.z,
            link2AngleDeg: viewModel.linkTwoRotation.z,
            pivotX: -pivot.x, pivotY: pivot.y, pivotZ: -pivot.z,
            l0Center: SIMD3<Float>(-l0.offset, 0, -0.5 * l0.thickness),
            l0Size:   SIMD3<Float>(l0.length, l0.height, l0.thickness),
            l1Center: SIMD3<Float>(-l1.offset, 0, -0.5 * l1.thickness),
            l1Size:   SIMD3<Float>(l1.length, l1.height, l1.thickness)
        )
        applyColors(
            link0Color: SIMD3<Float>(l0.color.x, l0.color.y, l0.color.z),
            link1Color: SIMD3<Float>(l1.color.x, l1.color.y, l1.color.z)
        )
    }

    private func applyTransforms(
        link1AngleDeg: Float,
        link2AngleDeg: Float,
        pivotX: Float, pivotY: Float, pivotZ: Float,
        l0Center: SIMD3<Float>, l0Size: SIMD3<Float>,
        l1Center: SIMD3<Float>, l1Size: SIMD3<Float>
    ) {
        // The 180° Y wrapper negates world X and Z; negate angles to compensate
        sceneState.link1AnchorEntity?.orientation = simd_quatf(
            angle: -link1AngleDeg * .pi / 180, axis: [0, 0, 1]
        )
        sceneState.link1Entity?.position = l0Center
        sceneState.link1Entity?.scale    = l0Size

        sceneState.pivot2AnchorEntity?.position = SIMD3<Float>(pivotX, pivotY, pivotZ)

        sceneState.link2AnchorEntity?.orientation = simd_quatf(
            angle: -link2AngleDeg * .pi / 180, axis: [0, 0, 1]
        )
        sceneState.link2Entity?.position = l1Center
        sceneState.link2Entity?.scale    = l1Size
    }

    private func applyColors(link0Color: SIMD3<Float>, link1Color: SIMD3<Float>) {
        if link0Color != sceneState.lastLink0Color, let entity = sceneState.link1Entity {
            sceneState.lastLink0Color = link0Color
            var mat = SimpleMaterial()
            mat.color = .init(tint: UIColor(
                red: CGFloat(link0Color.x), green: CGFloat(link0Color.y),
                blue: CGFloat(link0Color.z), alpha: 1
            ))
            mat.metallic  = .init(floatLiteral: 0.5)
            mat.roughness = .init(floatLiteral: 0.4)
            entity.model?.materials = [mat]
        }

        if link1Color != sceneState.lastLink1Color, let entity = sceneState.link2Entity {
            sceneState.lastLink1Color = link1Color
            var mat = SimpleMaterial()
            mat.color = .init(tint: UIColor(
                red: CGFloat(link1Color.x), green: CGFloat(link1Color.y),
                blue: CGFloat(link1Color.z), alpha: 1
            ))
            mat.metallic  = .init(floatLiteral: 0.5)
            mat.roughness = .init(floatLiteral: 0.4)
            entity.model?.materials = [mat]
        }
    }
}
