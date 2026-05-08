package com.dcengineer.twolinks

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitView

@Composable
actual fun TwoLinksSceneView(viewModel: MainViewModel) {
    val provider = IosSceneRegistry.provider ?: return

    LaunchedEffect(Unit) {
        while (true) {
            withFrameNanos { frameTime ->
                viewModel.updateOnFrame(frameTime)
                provider.update()
            }
        }
    }

    UIKitView(
        factory = { provider.createView(viewModel) },
        modifier = Modifier.fillMaxSize()
    )
}
