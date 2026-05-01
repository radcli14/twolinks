package com.dcengineer.twolinks

import android.graphics.Color
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.dcengineer.twolinks.model.Planet
import com.dcengineer.twolinks.model.center
import com.dcengineer.twolinks.model.rotation
import com.dcengineer.twolinks.model.size
import dev.romainguy.kotlin.math.Float3
import io.github.sceneview.SceneView
import io.github.sceneview.rememberCameraNode
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberEnvironment
import io.github.sceneview.rememberEnvironmentLoader
import io.github.sceneview.rememberMainLightNode
import io.github.sceneview.rememberMaterialLoader
import io.github.sceneview.rememberModelInstance
import io.github.sceneview.rememberModelLoader

@Composable
actual fun TwoLinksSceneView(viewModel: MainViewModel) {
    val state by viewModel.twoLinksState.collectAsState()

    val engine = rememberEngine()
    val modelLoader = rememberModelLoader(engine)
    val cameraNode = rememberCameraNode(engine) {
        position = Float3(0f, 0f, 5f)
    }
    val materialLoader = rememberMaterialLoader(engine)

    val environmentLoader = rememberEnvironmentLoader(engine)
    val environment = rememberEnvironment(environmentLoader) {
        environmentLoader.createHDREnvironment(
            assetFileLocation = "${viewModel.environmentsPath}/NightSkyHDRI009_2K_HDR.hdr"
        ) ?: environmentLoader.createEnvironment()
    }
    val mainLightNode = rememberMainLightNode(engine) {
        isShadowCaster = true
        rotation = Float3(x = 45f, y = 0f, z = 45f)
    }

    SceneView(
        modifier = Modifier.fillMaxSize(),
        engine = engine,
        modelLoader = modelLoader,
        cameraNode = cameraNode,
        environment = environment,
        mainLightNode = mainLightNode,
        onFrame = {
            viewModel.updateOnFrame(it)
        },
    ) {
        rememberModelInstance(modelLoader, viewModel.fileLocation(planet = Planet.moon))?.let {
            // Base node is the door, the camera orbits around the door
            CubeNode(
                size = viewModel.doorSize,
                // Offset backwards so the door surface is at zero
                center = Float3(0f, 0f, -0.5f * viewModel.doorSize.z),
                materialInstance = materialLoader.createColorInstance(
                    color = Color.GRAY,
                    roughness = 0f,
                    metallic = 1f,
                    reflectance = 1f
                ),
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

                // The center about which the first link rotates
                CylinderNode(
                    radius = 0.01f,
                    height = 0.025f,
                    rotation = Float3(90f, 0f, 0f),
                    materialInstance = materialLoader.createColorInstance(color = Color.GRAY)
                )

                // The first link
                CubeNode(
                    size = state.links[0].size,
                    center = state.links[0].center,
                    rotation = state.links[0].rotation(),
                    materialInstance = materialLoader.createColorInstance(color = Color.RED)
                ) {
                    // The pivot
                    CylinderNode(
                        radius = 0.01f,
                        height = 0.025f,
                        position = state.pivotPosition,
                        rotation = Float3(90f, 0f, 0f),
                        materialInstance = materialLoader.createColorInstance(color = Color.GREEN)
                    )

                    // The second link, empty node parent used to hold the pivot translation, but not rotation
                    CubeNode(
                        size = Float3(0.001f), // Very small, doesn't need to be seen
                        position = state.pivotPosition
                    ) {
                        CubeNode(
                            size = state.links[1].size,
                            center = state.links[1].center,
                            rotation = state.links[1].rotation(relativeTo = state.links[0]),
                            materialInstance = materialLoader.createColorInstance(color = Color.BLUE)
                        )
                    }
                }
            }
        }
    }
}
