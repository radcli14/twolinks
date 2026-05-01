package com.dcengineer.twolinks

import android.graphics.Color
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ComposableInferredTarget
import androidx.compose.runtime.InternalComposeApi
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.dcengineer.twolinks.functions.rad2deg
import com.dcengineer.twolinks.model.Link
import com.dcengineer.twolinks.model.Planet
import com.dcengineer.twolinks.model.size
import com.google.android.filament.MaterialInstance
import com.google.android.filament.Skybox
import dev.romainguy.kotlin.math.Float3
import io.github.sceneview.SceneView
import io.github.sceneview.createEnvironment
import io.github.sceneview.loaders.MaterialLoader
import io.github.sceneview.rememberCameraNode
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberEnvironment
import io.github.sceneview.rememberEnvironmentLoader
import io.github.sceneview.rememberMainLightNode
import io.github.sceneview.rememberMaterialLoader
import io.github.sceneview.rememberModelInstance
import io.github.sceneview.rememberModelLoader
import java.io.File

@Composable
actual fun TwoLinksSceneView(viewModel: MainViewModel) {
    val state by viewModel.twoLinksState.collectAsState()

    val engine = rememberEngine()
    val modelLoader = rememberModelLoader(engine)
    val cameraNode = rememberCameraNode(engine) {
        position = Float3(0f, 0f, 5f)
    }
    val materialLoader = rememberMaterialLoader(engine)

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

                // The center
                CylinderNode(
                    radius = 0.01f,
                    height = 0.025f,
                    position = Float3(0f, 0f, 0.5f * viewModel.doorSize.z),
                    rotation = Float3(90f, 0f, 0f),
                    materialInstance = materialLoader.createColorInstance(color = Color.GRAY)
                )

                // The first link
                CubeNode(
                    size = state.links[0].size,
                    center = Float3(state.links[0].offset, 0f, 0f),
                    position = Float3(0f, 0f, 0.5f * (viewModel.doorSize.z + state.links[0].thickness)),
                    rotation = Float3(0f, 0f, state.links[0].theta * rad2deg),
                    materialInstance = materialLoader.createColorInstance(color = Color.RED)
                ) {
                    // The pivot
                    CylinderNode(
                        radius = 0.01f,
                        height = 0.025f,
                        position = Float3(state.pivot, 0f, 0.5f * state.links[0].thickness),
                        rotation = Float3(90f, 0f, 0f),
                        materialInstance = materialLoader.createColorInstance(color = Color.GREEN)
                    )

                    // The second link, empty node parent used to hold the pivot translation, but not rotation
                    CubeNode(
                        size = Float3(0.001f), // Very small, doesn't need to be seen
                        position = Float3(state.pivot, 0f, 0.5f * state.links[0].thickness)
                    ) {
                        CubeNode(
                            size = state.links[1].size,
                            center = Float3(state.links[1].offset, 0f, 0f),
                            position = Float3(0f, 0f,0.5f * state.links[1].thickness),
                            rotation = Float3(0f, 0f, state.links[1].theta * rad2deg),
                            materialInstance = materialLoader.createColorInstance(color = Color.BLUE)
                        )
                    }
                }
            }
        }
    }
}