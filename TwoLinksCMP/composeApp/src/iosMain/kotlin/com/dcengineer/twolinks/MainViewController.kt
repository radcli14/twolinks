package com.dcengineer.twolinks

import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIViewController

fun mainViewController(
    createSceneView: (MainViewModel) -> UIViewController
): UIViewController {
    sceneViewFactory = createSceneView
    return ComposeUIViewController { App() }
}