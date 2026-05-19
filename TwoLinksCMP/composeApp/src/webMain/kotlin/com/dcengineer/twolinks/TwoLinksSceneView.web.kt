@file:OptIn(ExperimentalWasmJsInterop::class)
package com.dcengineer.twolinks

import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import com.dcengineer.twolinks.model.center
import com.dcengineer.twolinks.model.size
import dev.romainguy.kotlin.math.Float3
import dev.romainguy.kotlin.math.Mat4
import dev.romainguy.kotlin.math.rotation
import dev.romainguy.kotlin.math.scale
import dev.romainguy.kotlin.math.translation
import kotlinx.browser.document
import kotlinx.browser.window
import kotlin.js.ExperimentalWasmJsInterop
import kotlin.js.JsAny

// Note, can run using ./gradlew clean && ./gradlew :composeApp:wasmJsBrowserDevelopmentRun`

@OptIn(ExperimentalWasmJsInterop::class)
@Composable
actual fun TwoLinksSceneView(viewModel: MainViewModel) {
    val state by viewModel.twoLinksState.collectAsState()

    val sceneManager = remember { SceneManager() }

    // Punch a transparent hole through the Skiko canvas so the Filament 3D scene shows through.
    // Drag gestures on the transparent scene area orbit the camera; pinch zooms.
    // UI elements drawn above this Box (buttons, sheets) consume their own events first.
    Box(modifier = Modifier
        .fillMaxSize()
        .drawBehind {
            drawRect(color = Color.Transparent, size = size, blendMode = BlendMode.Clear)
        }
        .pointerInput(Unit) {
            detectTransformGestures { _, pan, _, _ ->
                orbitScene(pan.x, pan.y)
            }
        }
    )

    DisposableEffect(Unit) {
        val container = document.getElementById("scene-target")
        container?.appendChild(sceneManager.canvas)
        
        var renderLoopActive = true
        
        sceneManager.initSceneViewAsync { svRef ->
            if (!renderLoopActive) return@initSceneViewAsync

            // Create static primitives, and assign transforms that are permanent
            val doorEntity = createBox(svRef, StaticObjects.Door.width, StaticObjects.Door.height, StaticObjects.Door.thickness, 0.75f, 0.75f, 0.75f)
            val doorPos = Float3(0f, 0f, -0.5f * StaticObjects.Door.thickness)
            val doorOriginT = translation(doorPos)
            setEntityTransform(svRef, doorEntity, doorOriginT)
            setEntityMaterialProperties(svRef, doorEntity, metallic = 0.97f, roughness = 0.05f, reflectance = 0.95f)

            val pivot1Entity = createCylinder(svRef, StaticObjects.Pivot.radius, StaticObjects.Pivot.height, 0.69f, 0.69f, 0.69f)
            val pivotRotation = rotation(Float3(90f, 0f, 0f))
            val pivot1T = translation(Float3(0f, 0f, state.links[0].thickness)) * pivotRotation
            setEntityTransform(svRef, pivot1Entity, pivot1T)

            // Create dynamic primitives, but don't assign transforms until the update loop
            val pivot2Entity = createCylinder(svRef, StaticObjects.Pivot.radius, StaticObjects.Pivot.height, 0.5f, 0.5f, 0.5f)
            
            var lastLink1Color = state.links[0].color
            var lastLink2Color = state.links[1].color
            
            val link1Entity = createBox(svRef, 1f, 1f, 1f, lastLink1Color.x, lastLink1Color.y, lastLink1Color.z)
            val link2Entity = createBox(svRef, 1f, 1f, 1f, lastLink2Color.x, lastLink2Color.y, lastLink2Color.z)

            // Setup the render loop
            fun renderLoop(timeMs: Double) {
                if (!renderLoopActive) return
                
                val timeNs = (timeMs * 1_000_000.0).toLong()
                viewModel.updateOnFrame(timeNs)
                
                // Re-read state after update
                val currentState = viewModel.twoLinksState.value
                
                // Check and update colors
                if (lastLink1Color != currentState.links[0].color) {
                    lastLink1Color = currentState.links[0].color
                    setEntityColor(svRef, link1Entity, lastLink1Color.x, lastLink1Color.y, lastLink1Color.z, 1f)
                }
                if (lastLink2Color != currentState.links[1].color) {
                    lastLink2Color = currentState.links[1].color
                    setEntityColor(svRef, link2Entity, lastLink2Color.x, lastLink2Color.y, lastLink2Color.z, 1f)
                }
                
                // Link 1 Transform
                val link1OriginT = translation(Float3()) * rotation(viewModel.linkOneRotation)
                val link1GeomT = link1OriginT * translation(currentState.links[0].center) * scale(currentState.links[0].size)
                setEntityTransform(svRef, link1Entity, link1GeomT)
                
                // Pivot 2 Transform
                val pivot2T = link1OriginT * translation(currentState.pivotPosition) * rotation(Float3(90f, 0f, 0f))
                setEntityTransform(svRef, pivot2Entity, pivot2T)
                
                // Link 2 Transform
                val link2OriginT = link1OriginT * translation(currentState.pivotPosition) * rotation(viewModel.linkTwoRotation)
                val link2GeomT = link2OriginT * translation(currentState.links[1].center) * scale(currentState.links[1].size)
                setEntityTransform(svRef, link2Entity, link2GeomT)
                
                window.requestAnimationFrame(::renderLoop)
            }
            
            window.requestAnimationFrame(::renderLoop)
        }

        onDispose {
            renderLoopActive = false
            sceneManager.canvas.remove()
        }
    }
}

fun setEntityTransform(svRef: JsAny, entity: JsAny, mat: Mat4) {
    setEntityTransformJs(svRef, entity,
        mat.x.x, mat.x.y, mat.x.z, mat.x.w,
        mat.y.x, mat.y.y, mat.y.z, mat.y.w,
        mat.z.x, mat.z.y, mat.z.z, mat.z.w,
        mat.w.x, mat.w.y, mat.w.z, mat.w.w
    )
}
