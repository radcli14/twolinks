package com.dcengineer.twolinks

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitView
import com.dcengineer.twolinks.model.center
import com.dcengineer.twolinks.model.size

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
            val l0 = state.links[0]
            val l1 = state.links[1]
            provider.updateTransforms(
                viewModel.linkOneRotation.z, viewModel.linkTwoRotation.z,
                state.pivotPosition.x, state.pivotPosition.y, state.pivotPosition.z,
                l0.center.x, l0.center.y, l0.center.z,
                l0.size.x, l0.size.y, l0.size.z,
                l1.center.x, l1.center.y, l1.center.z,
                l1.size.x, l1.size.y, l1.size.z
            )
            val c0 = l0.color
            val c1 = l1.color
            provider.updateColors(c0.x, c0.y, c0.z, c1.x, c1.y, c1.z)
        },
        modifier = Modifier.fillMaxSize()
    )
}
