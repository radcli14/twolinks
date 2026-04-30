package com.dcengineer.twolinks

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.sceneview.SceneView
import io.github.sceneview.rememberModelInstance

@Composable
actual fun TwoLinksSceneView(viewModel: MainViewModel) {
    SceneView(
        modifier = Modifier.fillMaxSize(),
        onFrame = viewModel::updateOnFrame,
    ) {
        rememberModelInstance(modelLoader, viewModel.moonFileLocation)?.let {
            ModelNode(modelInstance = it, scaleToUnits = 0.27f, autoAnimate = true)
        }
    }
}