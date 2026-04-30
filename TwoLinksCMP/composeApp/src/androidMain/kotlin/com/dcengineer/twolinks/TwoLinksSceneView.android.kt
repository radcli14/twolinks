package com.dcengineer.twolinks

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.dcengineer.twolinks.model.Planet
import dev.romainguy.kotlin.math.Float3
import io.github.sceneview.SceneView
import io.github.sceneview.loaders.ModelLoader
import io.github.sceneview.rememberCameraNode
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberMainLightNode
import io.github.sceneview.rememberModelInstance
import io.github.sceneview.rememberModelLoader

@Composable
actual fun TwoLinksSceneView(viewModel: MainViewModel) {
    val engine = rememberEngine()
    val modelLoader = rememberModelLoader(engine)
    val cameraNode = rememberCameraNode(engine)
    cameraNode.position = Float3(0f, 0f, 5f)

    SceneView(
        modifier = Modifier.fillMaxSize(),
        engine = engine,
        modelLoader = modelLoader,
        cameraNode = cameraNode,
        onFrame = {
            viewModel.updateOnFrame(it)
        },
    ) {
        rememberModelInstance(modelLoader, viewModel.fileLocation(planet = Planet.moon))?.let {
            // Base node is the door, the camera orbits around the door
            CubeNode(size = viewModel.doorSize) {
                // Moon node is offset downward so its surface matches the door base
                ModelNode(
                    modelInstance = it,
                    position = Planet.moon.position,
                    rotation = Planet.moon.rotation,
                    scaleToUnits = Planet.moon.scale,
                )
            }
        }
    }
}