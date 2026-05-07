import UIKit
import SwiftUI
import SceneViewSwift
import RealityKit
import ComposeApp

@objc class TwoLinksSceneDelegate: NSObject, IosSceneProvider {

    private var sphereEntity: ModelEntity?

    private lazy var hostingController: UIHostingController<AnyView> = {
        let sceneView = SceneView { [weak self] root in
            let sphere = GeometryNode.sphere(radius: 0.3, color: .blue)
            root.addChild(sphere.entity)
            self?.sphereEntity = sphere.entity
        }
        .cameraControls(.orbit)
        let hc = UIHostingController(rootView: AnyView(sceneView))
        hc.view.backgroundColor = .clear
        return hc
    }()

    @objc func createView() -> UIView {
        hostingController.view
    }

    @objc func updateTransforms(l1: Float, l2: Float, px: Float, py: Float, pz: Float) {
        // Phase 1: no-op — sphere is static
    }

    @objc func updateColors(r1: Float, g1: Float, b1: Float, r2: Float, g2: Float, b2: Float) {
        // Phase 1: no-op
    }
}
