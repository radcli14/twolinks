import ARKit
import RealityKit
import SceneViewSwift
import SwiftUI
import ComposeApp

struct TwoLinksARSceneView: View {
    let viewModel: MainViewModel
    let manager: SceneManager

    @State private var gestureHandler = ARGestureHandler()

    var body: some View {
        ARSceneView(planeDetection: .horizontal, showCoachingOverlay: true)
            .onSessionStarted { arView in
                let anchor = AnchorEntity(plane: .horizontal, minimumBounds: [0.1, 0.1])
                let container = Entity()
                container.position = [0, 0.1, 0]
                container.scale = SIMD3(repeating: 0.1)
                anchor.addChild(container)
                arView.scene.addAnchor(anchor)
                manager.buildScene(root: container, representing: viewModel)
                gestureHandler.setup(arView: arView, container: container)
            }
            .onFrame { _, _ in
                DispatchQueue.main.async {
                    manager.updateOnFrame(viewModel: viewModel)
                }
            }
            .edgesIgnoringSafeArea(.all)
    }
}

class ARGestureHandler: NSObject, UIGestureRecognizerDelegate {
    private weak var arView: ARView?
    private weak var container: Entity?
    private var initialScale: Float = 0.1
    private var initialOrientation = simd_quatf(angle: 0, axis: [0, 1, 0])

    func setup(arView: ARView, container: Entity) {
        self.arView = arView
        self.container = container

        let pan = UIPanGestureRecognizer(target: self, action: #selector(handlePan))
        pan.minimumNumberOfTouches = 1
        pan.maximumNumberOfTouches = 1
        pan.delegate = self
        arView.addGestureRecognizer(pan)

        let pinch = UIPinchGestureRecognizer(target: self, action: #selector(handlePinch))
        pinch.delegate = self
        arView.addGestureRecognizer(pinch)

        let rotation = UIRotationGestureRecognizer(target: self, action: #selector(handleRotation))
        rotation.delegate = self
        arView.addGestureRecognizer(rotation)
    }

    func gestureRecognizer(
        _ gr: UIGestureRecognizer,
        shouldRecognizeSimultaneouslyWith other: UIGestureRecognizer
    ) -> Bool { true }

    @objc func handlePan(_ gr: UIPanGestureRecognizer) {
        guard let arView, let container else { return }
        let location = gr.location(in: arView)
        let results = arView.raycast(from: location, allowing: .estimatedPlane, alignment: .horizontal)
        if let result = results.first {
            let col = result.worldTransform.columns.3
            let currentY = container.position(relativeTo: nil).y
            container.setPosition(SIMD3(col.x, currentY, col.z), relativeTo: nil)
        }
    }

    @objc func handlePinch(_ gr: UIPinchGestureRecognizer) {
        guard let container else { return }
        if gr.state == .began { initialScale = container.scale.x }
        let newScale = max(0.01, min(1.0, initialScale * Float(gr.scale)))
        container.scale = SIMD3(repeating: newScale)
    }

    @objc func handleRotation(_ gr: UIRotationGestureRecognizer) {
        guard let container else { return }
        if gr.state == .began { initialOrientation = container.orientation(relativeTo: nil) }
        let delta = simd_quatf(angle: -Float(gr.rotation), axis: [0, 1, 0])
        container.setOrientation(initialOrientation * delta, relativeTo: nil)
    }
}
