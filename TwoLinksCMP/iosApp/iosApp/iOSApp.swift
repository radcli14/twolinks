import SwiftUI
import ComposeApp

@main
struct iOSApp: App {
    init() {
        let delegate = TwoLinksSceneDelegate()
        IosSceneRegistry.shared.provider = delegate
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
