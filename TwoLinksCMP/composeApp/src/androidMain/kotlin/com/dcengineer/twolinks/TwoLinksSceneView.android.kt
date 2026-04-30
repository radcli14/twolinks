package com.dcengineer.twolinks

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.dcengineer.twolinks.model.Planet
import com.google.android.filament.Skybox
import dev.romainguy.kotlin.math.Float3
import io.github.sceneview.SceneView
import io.github.sceneview.createEnvironment
import io.github.sceneview.rememberCameraNode
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberEnvironment
import io.github.sceneview.rememberEnvironmentLoader
import io.github.sceneview.rememberMainLightNode
import io.github.sceneview.rememberModelInstance
import io.github.sceneview.rememberModelLoader
import java.io.File

@Composable
actual fun TwoLinksSceneView(viewModel: MainViewModel) {
    val engine = rememberEngine()
    val modelLoader = rememberModelLoader(engine)
    val cameraNode = rememberCameraNode(engine) {
        position = Float3(0f, 0f, 5f)
    }

    // TODO: trying to get directional light up and running
    /*val mainLightNode = rememberMainLightNode(engine) {
        isShadowCaster = true
        rotation = Float3(x = 45f, y = 0f, z = 45f)
    }
    val environmentLoader = rememberEnvironmentLoader(engine)
    val environment = rememberEnvironment(environmentLoader) {
        environmentLoader.createHDREnvironment(
            file = File("composeResources/twolinkscmp.composeapp.generated.resources/files/environments/golden_gate_hills_1k.hdr")
        )!!
        //createEnvironment(engine, indirectLight = null, skybox = Skybox.Builder().color(0f, 0f, 0f, 0f).intensity(0f).build(engine))
    }
    environment.indirectLight?.intensity = 0f
    //environment.skybox?.intensity = 0f
    println("environment ${environment.indirectLight?.intensity}, ${environment.skybox?.intensity}")
    */

    SceneView(
        modifier = Modifier.fillMaxSize(),
        engine = engine,
        modelLoader = modelLoader,
        cameraNode = cameraNode,
        //mainLightNode = mainLightNode,
        onFrame = {
            viewModel.updateOnFrame(it)
        },
    ) {
        rememberModelInstance(modelLoader, viewModel.fileLocation(planet = Planet.moon))?.let {
            // Base node is the door, the camera orbits around the door
            CubeNode(
                size = viewModel.doorSize,
                apply = {
                    isShadowCaster = true
                    isShadowReceiver = true
                }
            ) {
                // Moon node is offset downward so its surface matches the door base
                ModelNode(
                    modelInstance = it,
                    position = Planet.moon.position,
                    rotation = Planet.moon.rotation,
                    scaleToUnits = Planet.moon.scale,
                    apply = {
                        isShadowCaster = true
                        isShadowReceiver = true
                    }
                )
            }
        }
    }
}
