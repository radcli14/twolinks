package com.dcengineer.twolinks

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.sceneview.SceneView
import io.github.sceneview.rememberModelInstance

@Composable
actual fun TwoLinksSceneView() {
    val assetFileLocation = "composeResources/twolinkscmp.composeapp.generated.resources/files/models/moon.glb"

    SceneView(modifier = Modifier.fillMaxSize()) {
        rememberModelInstance(modelLoader, assetFileLocation)?.let {
            ModelNode(modelInstance = it, scaleToUnits = 1.0f, autoAnimate = true)
        }
    }
}