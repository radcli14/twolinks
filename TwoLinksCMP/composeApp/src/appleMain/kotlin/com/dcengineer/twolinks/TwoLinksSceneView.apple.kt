package com.dcengineer.twolinks

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitViewController

@Composable
actual fun TwoLinksSceneView(viewModel: MainViewModel) {
    val controller = IosSceneRegistry.viewController ?: return

    LaunchedEffect(Unit) {
        while (true) {
            withFrameNanos { frameTime ->
                viewModel.updateOnFrame(frameTime)
                IosSceneRegistry.onUpdate?.invoke(viewModel)
            }
        }
    }

    UIKitViewController(
        factory = { controller },
        update = { },
        modifier = Modifier.fillMaxSize()
    )
}
