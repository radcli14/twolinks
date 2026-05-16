package com.dcengineer.twolinks

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitViewController
import platform.UIKit.UIViewController

internal var sceneViewFactory: ((MainViewModel) -> UIViewController)? = null

@Composable
actual fun TwoLinksSceneView(viewModel: MainViewModel) {
    val factory = sceneViewFactory ?: return
    UIKitViewController(
        factory = { factory(viewModel) },
        update = { },
        modifier = Modifier.fillMaxSize()
    )
}
