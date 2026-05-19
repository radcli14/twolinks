import ARKit
import RealityKit
import SceneViewSwift
import SwiftUI
import ComposeApp

struct TwoLinksARSceneView: View {
    let viewModel: MainViewModel
    let manager: SceneManager

    @State private var container: Entity?
    @State private var scale: Entity?
    @State private var dragStartPosition: SIMD3<Float>?
    @State private var dragStartUnproject: SIMD3<Float>?
    @State private var scaleStart: Float = 0.314
    @State private var yawStart: Float?
    
    var body: some View {
        RealityView { content in
            content.camera = .spatialTracking
            let anchor = AnchorEntity(plane: .horizontal, minimumBounds: [0.1, 0.1])
            content.add(anchor)
            
            // Container handles the user translate or rotate
            container = Entity()
            container?.setParent(anchor)

            // Scale entity: child of container, has a fixed vertical offset, and receives pinch-to-scale changes.
            scale = Entity()
            scale?.position = SIMD3(0, StaticObjects.Door.shared.offset, 0)
            scale?.scale = SIMD3(repeating: scaleStart)
            scale?.setParent(container)

            // Fixed-position entity: lifts the scene so the door-bottom/moon-top join
            // using half of the door height in the scene space.
            let offset = Entity()
            offset.position = [0, 0.5 * StaticObjects.Door.shared.height, 0]
            scale?.addChild(offset)

            manager.buildScene(root: offset, representing: viewModel, includeParticles: false)

            // Invisible hitbox sized to cover the ground surrounding the scene.
            // InputTargetComponent + CollisionComponent are required for targetedToAnyEntity().
            let hitbox = Entity()
            hitbox.components.set(InputTargetComponent())
            hitbox.components.set(CollisionComponent(shapes: [.generateBox(size: [2, 0.1, 2])]))
            container?.addChild(hitbox)
        }
        .gesture(
            DragGesture()
                .targetedToAnyEntity()
                .onChanged(handleDragChanged)
                .onEnded(handleDragEnded)
        )
        .gesture(
            MagnifyGesture()
                .simultaneously(with: RotationGesture())
                .onChanged { value in
                    if let mag = value.first { handleMagnifyChanged(mag) }
                    if let rot = value.second { handleRotateChanged(rot) }
                }
                .onEnded { _ in
                    //scaleStart = nil
                    yawStart = nil
                }
        )
        .edgesIgnoringSafeArea(.all)
    }
    
    // MARK: - Drag Gesture
    
    private func handleDragChanged(_ value: EntityTargetValue<DragGesture.Value>) {
        guard let container, let worldPos = value.unproject(value.location, from: .global, to: .scene) else { return }
        if dragStartPosition == nil {
            dragStartPosition = container.position(relativeTo: nil)
            dragStartUnproject = worldPos
        }
        guard let dragStartPosition, let dragStartUnproject else { return }
        let dx = worldPos.x - dragStartUnproject.x
        let dz = worldPos.z - dragStartUnproject.z
        let newContainerPos = dragStartPosition + SIMD3(dx, 0, dz)
        container.setPosition(newContainerPos, relativeTo: nil)
    }
    
    private func handleDragEnded(_ value: EntityTargetValue<DragGesture.Value>) {
        dragStartPosition = nil
        dragStartUnproject = nil
    }
    
    // MARK: - Magnify and Rotate Gestures

    private func handleMagnifyChanged(_ value: MagnifyGesture.Value) {
        guard let scale else { return }
        let newScale = max(0.01, min(1.0, scaleStart * Float(value.magnification)))
        scale.scale = SIMD3(repeating: newScale)
    }

    private func handleRotateChanged(_ value: RotationGesture.Value) {
        guard let container else { return }
        if yawStart == nil {
            // Extract current yaw from the Y-axis rotation component of the orientation
            let q = container.orientation(relativeTo: nil)
            yawStart = 2.0 * atan2(q.imag.y, q.real)
        }
        guard let yawStart else { return }
        let yaw = yawStart - Float(value.radians)
        container.setOrientation(simd_quatf(angle: yaw, axis: [0, 1, 0]), relativeTo: nil)
    }
}

/*
// ARSceneView-based implementation (SceneViewSwift). Camera feed not visible when embedded
// in the UIKitViewController/UIHostingController/Compose hierarchy — raised with SceneViewSwift maintainers.
struct TwoLinksARSceneViewLegacy: View {
    let viewModel: MainViewModel
    let manager: SceneManager

    @State private var gestureHandler = ARGestureHandler()

    var body: some View {
        ARSceneView(planeDetection: .horizontal, showPlaneOverlay: false, showCoachingOverlay: true)
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
*/
