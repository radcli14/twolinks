package com.dcengineer.twolinks

import android.graphics.Color
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.dcengineer.twolinks.model.Planet
import com.dcengineer.twolinks.model.center
import com.dcengineer.twolinks.model.size
import dev.romainguy.kotlin.math.Float3
import io.github.sceneview.NodeScope
import io.github.sceneview.SceneView
import io.github.sceneview.model.ModelInstance
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
        updateProjection(far = 1000f)
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

    // Define states for the GLB formatted models
    var moonInstance by remember { mutableStateOf<ModelInstance?>(null) }
    var earthInstance by remember { mutableStateOf<ModelInstance?>(null) }

    // Load the Moon first
    LaunchedEffect(Unit) {
        moonInstance = modelLoader.loadModelInstance(viewModel.fileLocation(Planet.moon))
    }

    // Load the Earth ONLY after the Moon is ready
    LaunchedEffect(moonInstance) {
        if (moonInstance != null) {
            earthInstance = modelLoader.loadModelInstance(viewModel.fileLocation(Planet.earth))
        }
    }

    SceneView(
        modifier = Modifier.fillMaxSize(),
        engine = engine,
        modelLoader = modelLoader,
        cameraNode = cameraNode,
        environment = environment,
        mainLightNode = mainLightNode,
        onFrame = viewModel::updateOnFrame,
    ) {

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

            // The center about which the first link rotates
            PivotNode()

            // The first link
            CubeNode(
                size = state.links[0].size,
                center = state.links[0].center,
                rotation = viewModel.linkOneRotation,
                materialInstance = materialLoader.createColorInstance(color = state.links[0].color)
            ) {
                // The pivot about which the second link rotates
                PivotNode(position = state.pivotPosition)

                // The second link, rotates about the pivot position
                CubeNode(
                    size = state.links[1].size,
                    center = state.links[1].center,
                    position = state.pivotPosition,
                    rotation = viewModel.linkTwoRotation,
                    materialInstance = materialLoader.createColorInstance(color = state.links[1].color)
                )
            }
        }

        moonInstance?.let {
            // Moon node is offset downward so its surface matches the door base
            ModelNode(
                modelInstance = it,
                position = Planet.moon.position,
                rotation = Planet.moon.rotation,
                scaleToUnits = Planet.moon.scale,
                apply = {
                    isShadowReceiver = true
                }
            )
        }

        earthInstance?.let {
            ModelNode(
                modelInstance = it,
                position = Planet.earth.position,
                rotation = Planet.earth.rotation,
                scaleToUnits = Planet.earth.scale,
                apply = {
                    isShadowReceiver = true
                }
            )
        }
    }
}

@Composable
fun NodeScope.PlanetNode(instance: ModelInstance, planet: Planet) {
    ModelNode(
        modelInstance = instance,
        position = planet.position,
        rotation = planet.rotation,
        scaleToUnits = planet.scale,
        apply = {
            isShadowReceiver = true
        }
    )
}


/**
 * A simple cylindrical node used to represent the hinge that a link rotates around.
 */
@Composable
fun NodeScope.PivotNode(
    radius: Float = 0.01f,
    height: Float = 0.015f,
    position: Float3 = Float3(),
    color: Int = Color.GRAY
) {
    CylinderNode(
        radius = radius,
        height = height,
        position = position,
        rotation = Float3(90f, 0f, 0f),
        materialInstance = materialLoader.createColorInstance(color = color)
    )
}