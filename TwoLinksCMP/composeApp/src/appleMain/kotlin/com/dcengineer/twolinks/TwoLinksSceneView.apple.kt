package com.dcengineer.twolinks

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitView

@Composable
actual fun TwoLinksSceneView(viewModel: MainViewModel) {
    val provider = IosSceneRegistry.provider ?: return
    val state by viewModel.twoLinksState.collectAsState()

    LaunchedEffect(Unit) {
        while (true) {
            withFrameNanos { frameTime -> viewModel.updateOnFrame(frameTime) }
        }
    }

    UIKitView(
        factory = { provider.createView() },
        update = {
            provider.updateTransforms(
                viewModel.linkOneRotation.z,
                viewModel.linkTwoRotation.z,
                state.pivotPosition.x, state.pivotPosition.y, state.pivotPosition.z
            )
            val c0 = state.links[0].color
            val c1 = state.links[1].color
            provider.updateColors(c0.x, c0.y, c0.z, c1.x, c1.y, c1.z)
        },
        modifier = Modifier.fillMaxSize()
    )
}
