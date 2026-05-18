import SwiftUI
import SceneViewSwift
import RealityKit
import ComposeApp

struct TwoLinksSceneView: View {
    let viewModel: MainViewModel

    @State private var manager = SceneManager()
    @State private var sunLight = LightNode.directional(color: .warm, intensity: 10_000, castsShadow: true)
    @State private var isARMode = false

    var body: some View {
        let _ = sunLight.entity.look(at: .zero, from: Planet.companion.sun.position.asSIMD3, relativeTo: nil)
        return TimelineView(.animation) { context in
            Group {
                if isARMode {
                    TwoLinksARSceneView(viewModel: viewModel, manager: manager)
                } else {
                    SceneView { root in
                        manager.buildScene(root: root, representing: viewModel)
                    }
                    .cameraControls(.orbit)
                    .autoCenterContent(false)
                    .environment(.custom(name: "NightSky", hdrFile: "NightSky"))
                    .mainLight(.custom(sunLight))
                    .fillLight(.disabled)
                    .edgesIgnoringSafeArea(.all)
                }
            }
            .onChange(of: context.date) { _, _ in
                let newIsARMode = (viewModel.viewMode.value as? ViewMode) == .ar
                if isARMode != newIsARMode { isARMode = newIsARMode }
                manager.updateOnFrame(viewModel: viewModel)
            }
        }
    }
}

