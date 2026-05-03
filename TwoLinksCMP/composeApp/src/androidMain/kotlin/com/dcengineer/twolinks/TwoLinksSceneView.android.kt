package com.dcengineer.twolinks

import android.graphics.Color
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.dcengineer.twolinks.model.Link
import com.dcengineer.twolinks.model.Planet
import com.dcengineer.twolinks.model.center
import com.dcengineer.twolinks.model.size
import dev.romainguy.kotlin.math.Float3
import io.github.sceneview.NodeScope
import io.github.sceneview.SceneScope
import io.github.sceneview.SceneView
import io.github.sceneview.model.ModelInstance

@Composable
actual fun TwoLinksSceneView(viewModel: MainViewModel) {
    val state by viewModel.twoLinksState.collectAsState()
    val context = LocalContext.current
    val manager = remember { SceneManager(context) }

    // Load assets when the view is ready
    LaunchedEffect(Unit) {
        manager.loadModels()
    }

    SceneView(
        modifier = Modifier.fillMaxSize(),
        engine = manager.engine,
        modelLoader = manager.modelLoader,
        cameraNode = manager.cameraNode,
        environment = manager.environment,
        mainLightNode = manager.mainLightNode,
        onFrame = viewModel::updateOnFrame,
    ) {

        // Base node is the door, the camera orbits around the door
        DoorNode(viewModel.doorSize) {

            // The center about which the first link rotates
            PivotNode()

            // The first link
            LinkNode(state.links[0], rotation = viewModel.linkOneRotation) {
                // The pivot about which the second link rotates
                PivotNode(position = state.pivotPosition)

                // The second link, rotates about the pivot position
                LinkNode(state.links[1], position = state.pivotPosition, rotation = viewModel.linkTwoRotation)
            }
        }

        PlanetNode(manager.moonInstance, planet = Planet.moon)
        PlanetNode(manager.earthInstance, planet = Planet.earth)
    }
}

/**
 * The representation of the door that the pendulum hangs from
 */
@Composable
fun SceneScope.DoorNode(
    size: Float3,
    content: @Composable (NodeScope.() -> Unit)? = null
) {
    CubeNode(
        size = size,
        // Offset backwards so the door surface is at zero
        center = Float3(0f, 0f, -0.5f * size.z),
        materialInstance = materialLoader.createColorInstance(
            color = Color.GRAY,
            roughness = 0f,
            metallic = 1f,
            reflectance = 1f
        ),
        apply = {
            isShadowCaster = true
            isShadowReceiver = true
        },
        content = content
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

/**
 * Creates a node representing one of the pendulum links
 */
@Composable
fun NodeScope.LinkNode(
    link: Link,
    position: Float3 = Float3(),
    rotation: Float3 = Float3(),
    content: @Composable (NodeScope.() -> Unit)? = null
) {
    CubeNode(
        size = link.size,
        center = link.center,
        position = position,
        rotation = rotation,
        materialInstance = materialLoader.createColorInstance(color = link.color),
        content = content
    )
}

/**
 * Creates a node representing a planet as soon as its Filament instance has loaded
 */
@Composable
fun SceneScope.PlanetNode(instance: ModelInstance?, planet: Planet) {
    instance?.let {
        ModelNode(
            modelInstance = it,
            position = planet.position,
            rotation = planet.rotation,
            scaleToUnits = planet.scale,
            apply = {
                isShadowReceiver = true
            }
        )
    }
}
