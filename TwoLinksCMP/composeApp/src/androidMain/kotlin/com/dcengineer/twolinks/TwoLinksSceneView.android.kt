package com.dcengineer.twolinks

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import dev.romainguy.kotlin.math.Float3
import io.github.sceneview.SceneView
import io.github.sceneview.rememberModelInstance
import kotlin.math.sin

@Composable
actual fun TwoLinksSceneView(viewModel: MainViewModel) {
    var scale by remember { mutableStateOf(1f) }

    SceneView(
        modifier = Modifier.fillMaxSize(),
        onFrame = {
            viewModel.updateOnFrame(it)
            scale = sin(viewModel.elapsedTime)
        },
    ) {
        rememberModelInstance(modelLoader, viewModel.moonFileLocation)?.let {
            ModelNode(
                modelInstance = it,
                scale = Float3(scale) // note: don't try to update scaleToUnits, this conflicts, and doesn't allow updates on each frame
            )
        }
    }
}