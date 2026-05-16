import SwiftUI
import ComposeApp

@main
struct iOSApp: App {
    init() {
        let controller = TwoLinksSceneViewController()
        IosSceneRegistry.shared.viewController = controller
        IosSceneRegistry.shared.onUpdate = { vm in controller.update(viewModel: vm) }
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
