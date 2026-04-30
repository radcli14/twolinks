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
import io.github.sceneview.model.ModelInstance
import io.github.sceneview.model.model
import io.github.sceneview.node.ModelNode
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberModelInstance
import io.github.sceneview.rememberModelLoader
import io.github.sceneview.rememberScene
import kotlin.math.sin

@Composable
actual fun TwoLinksSceneView(viewModel: MainViewModel) {
    val engine = rememberEngine()
    val modelLoader = rememberModelLoader(engine)

    val moonInstance = rememberModelInstance(modelLoader, viewModel.moonFileLocation)

    val moonNode = remember(moonInstance) {
        moonInstance?.let {
            ModelNode(modelInstance = it, scaleToUnits = 0.27f)
        }
    }

    var scale by remember { mutableStateOf(1f) }

    SceneView(
        modifier = Modifier.fillMaxSize(),
        engine = engine,
        modelLoader = modelLoader,
        onFrame = {
            viewModel.updateOnFrame(it)
            scale = sin(viewModel.elapsedTime)
        },
    ) {
        moonInstance?.let {
            ModelNode(modelInstance = it, scaleToUnits = scale)
        }
    }
}