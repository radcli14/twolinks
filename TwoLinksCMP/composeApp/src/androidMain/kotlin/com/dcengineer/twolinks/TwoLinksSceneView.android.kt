package com.dcengineer.twolinks

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.dcengineer.twolinks.model.Link
import com.dcengineer.twolinks.model.StaticObjects
import com.dcengineer.twolinks.model.Planet
import com.dcengineer.twolinks.model.center
import com.dcengineer.twolinks.model.size
import dev.romainguy.kotlin.math.Float3
import dev.romainguy.kotlin.math.normalize
import com.google.android.filament.LightManager
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.input.pointer.pointerInput
import com.dcengineer.twolinks.model.ViewMode
import com.google.ar.core.Anchor
import com.google.ar.core.Config
import com.google.ar.core.Plane
import com.google.ar.core.Pose
import com.google.ar.core.TrackingState
import io.github.sceneview.NodeScope
import io.github.sceneview.SceneScope
import io.github.sceneview.SceneView
import io.github.sceneview.ar.ARSceneView
import io.github.sceneview.ar.arcore.getUpdatedPlanes
import io.github.sceneview.model.ModelInstance
import io.github.sceneview.rememberModelInstance
import com.dcengineer.twolinks.functions.fileLocation

@Composable
actual fun TwoLinksSceneView(viewModel: MainViewModel) {
    val state by viewModel.twoLinksState.collectAsState()
    val context = LocalContext.current
    val manager = remember { SceneManager(context) }
    val viewMode by viewModel.viewMode

    // Load assets when the view is ready
    LaunchedEffect(Unit) {
        manager.loadModels()
    }

    when (viewMode) {
        ViewMode.Standard -> SceneView(
            modifier = Modifier.fillMaxSize(),
            engine = manager.engine,
            modelLoader = manager.modelLoader,
            cameraNode = manager.cameraNode,
            environment = manager.environment,
            mainLightNode = manager.mainLightNode,
            onFrame = viewModel::updateOnFrame,
        ) {

            // Base node is the door, the camera orbits around the door
            DoorNode {

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
        ViewMode.AR -> TwoLinksARScene(viewModel)
    }
}

/**
 * The representation of the door that the pendulum hangs from
 */
@Composable
fun SceneScope.DoorNode(
    content: @Composable (NodeScope.() -> Unit)? = null
) {
    CubeNode(
        size = StaticObjects.Door.size,
        // Offset backwards so the door surface is at zero
        center = Float3(0f, 0f, -0.5f * StaticObjects.Door.thickness),
        materialInstance = materialLoader.createColorInstance(
            color = StaticObjects.Door.color,
            roughness = StaticObjects.Door.roughness,
            metallic = StaticObjects.Door.metallic,
            reflectance = StaticObjects.Door.reflectance
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
    position: Float3 = Float3()
) {
    CylinderNode(
        radius = StaticObjects.Pivot.radius,
        height = StaticObjects.Pivot.height,
        position = position,
        rotation = Float3(90f, 0f, 0f),
        materialInstance = materialLoader.createColorInstance(
            color = StaticObjects.Pivot.color,
            roughness = StaticObjects.Pivot.roughness,
            metallic = StaticObjects.Pivot.metallic,
            reflectance = StaticObjects.Pivot.reflectance
        )
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
    // Remember the material instance so it doesn't have to be regenerated each update
    val materialInstance = remember(link.color) {
        materialLoader.createColorInstance(color = link.color)
    }

    // We need to use runtime keying to force an identity reset when size or color changes
    androidx.compose.runtime.key(link.length, link.offset, link.color) {
        CubeNode(
            size = link.size,
            center = link.center,
            position = position,
            rotation = rotation,
            materialInstance = materialInstance,
            apply = {
                // Ensure the node is visible and updates its bounding box
                isShadowCaster = true
                isShadowReceiver = true
            },
            content = content
        )
    }
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

/**
 * AR scene: places the pendulum on a detected horizontal plane.
 * Pinch scales the scene; two-finger rotation rotates it around the Y-axis.
 */
@Composable
fun TwoLinksARScene(viewModel: MainViewModel) {
    val state by viewModel.twoLinksState.collectAsState()
    var anchor by remember { mutableStateOf<Anchor?>(null) }
    var sceneScale by remember { mutableStateOf(0.1f) }
    var sceneRotationY by remember { mutableStateOf(0f) }
    // Counter used to trigger the camera-forward fallback without causing recomposition each frame
    val trackingFrames = remember { intArrayOf(0) }

    ARSceneView(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTransformGestures { _, _, zoom, rotation ->
                    sceneScale = (sceneScale * zoom).coerceIn(0.01f, 1f)
                    sceneRotationY += rotation
                }
            },
        sessionConfiguration = { _, config ->
            config.planeFindingMode = Config.PlaneFindingMode.HORIZONTAL
            config.lightEstimationMode = Config.LightEstimationMode.ENVIRONMENTAL_HDR
        },
        onSessionUpdated = { session, frame ->
            viewModel.updateOnFrame(frame.timestamp)
            if (anchor == null) {
                anchor = frame.getUpdatedPlanes()
                    .firstOrNull {
                        it.type == Plane.Type.HORIZONTAL_UPWARD_FACING &&
                        it.trackingState == TrackingState.TRACKING
                    }
                    ?.let { runCatching { it.createAnchor(it.centerPose) }.getOrNull() }

                // Fall back to 1m in front of the camera after ~3 seconds with no plane
                if (anchor == null && frame.camera.trackingState == TrackingState.TRACKING) {
                    trackingFrames[0]++
                    if (trackingFrames[0] > 90) {
                        val forwardPose = frame.camera.pose.compose(Pose.makeTranslation(0f, 0f, -1f))
                        anchor = runCatching { session.createAnchor(forwardPose) }.getOrNull()
                    }
                }
            }
        }
    ) {
        val moonInstance = rememberModelInstance(modelLoader, fileLocation(Planet.moon))
        val earthInstance = rememberModelInstance(modelLoader, fileLocation(Planet.earth))
        LightNode(
            type = LightManager.Type.DIRECTIONAL,
            direction = normalize(-Planet.sun.position),
            apply = {
                color(Planet.sun.color.x, Planet.sun.color.y, Planet.sun.color.z)
                intensity(100_000f)
                castShadows(true)
            }
        )

        anchor?.let { a ->
            AnchorNode(anchor = a) {
                Node(
                    position = Float3(0f, 0.1f, 0f),
                    scale = Float3(sceneScale),
                    rotation = Float3(0f, sceneRotationY, 0f)
                ) {
                    DoorNode {
                        PivotNode()
                        LinkNode(state.links[0], rotation = viewModel.linkOneRotation) {
                            PivotNode(position = state.pivotPosition)
                            LinkNode(state.links[1], position = state.pivotPosition, rotation = viewModel.linkTwoRotation)
                        }
                    }
                    PlanetNode(moonInstance, planet = Planet.moon)
                    PlanetNode(earthInstance, planet = Planet.earth)
                }
            }
        }
    }
}
