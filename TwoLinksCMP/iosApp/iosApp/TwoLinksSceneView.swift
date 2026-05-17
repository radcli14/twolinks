import SwiftUI
import SceneViewSwift
import RealityKit
import ComposeApp

struct TwoLinksSceneView: View {
    let viewModel: MainViewModel

    @State private var manager = SceneManager()
    @State private var sunLight = LightNode.directional(color: .warm, intensity: 10_000, castsShadow: true)

    var body: some View {
        let _ = sunLight.entity.look(at: .zero, from: Planet.companion.sun.position.asSIMD3, relativeTo: nil)
        return TimelineView(.animation) { context in
            SceneView { root in
                manager.buildScene(root: root, representing: viewModel)
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
                manager.applyTransforms(from: viewModel.twoLinks)
                manager.applyColors(from: viewModel.twoLinks)
            }
        }
    }
}

