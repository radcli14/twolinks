@file:OptIn(ExperimentalWasmJsInterop::class)
package com.dcengineer.twolinks

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import com.dcengineer.twolinks.model.center
import com.dcengineer.twolinks.model.size
import dev.romainguy.kotlin.math.Float3
import dev.romainguy.kotlin.math.Mat4
import dev.romainguy.kotlin.math.rotation
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

    val sceneManager = SceneManager()

    // Punch a transparent hole through the Skiko canvas so the Filament 3D scene shows through
    Box(modifier = Modifier.fillMaxSize().drawBehind {
        drawRect(
            color = Color.Transparent,
            size = size,
            blendMode = BlendMode.Clear
        )
    })

    DisposableEffect(Unit) {
        val container = document.getElementById("scene-target")
        container?.appendChild(sceneManager.canvas)
        
        var renderLoopActive = true
        
        sceneManager.initSceneViewAsync { svRef ->
            if (!renderLoopActive) return@initSceneViewAsync

            // Create static primitives, and assign transforms that are permanent
            val doorEntity = createBox(svRef, viewModel.doorSize.x, viewModel.doorSize.y, viewModel.doorSize.z, 0.157f, 0.157f, 0.157f)
            val doorPos = Float3(0f, 0f, -0.5f * viewModel.doorSize.z)
            val doorOriginT = translation(doorPos)
            setEntityTransform(svRef, doorEntity, doorOriginT)

            val pivot1Entity = createCylinder(svRef, 0.01f, 0.015f, 0.69f, 0.69f, 0.69f)
            val pivotRotation = rotation(Float3(90f, 0f, 0f))
            val pivot1T = translation(Float3(0f, 0f, state.links[0].thickness)) * pivotRotation
            setEntityTransform(svRef, pivot1Entity, pivot1T)

            // Create dynamic primitives, but don't assign transforms until the update loop
            val pivot2Entity = createCylinder(svRef, 0.01f, 0.015f, 0.5f, 0.5f, 0.5f)
            val link1Entity = createBox(svRef, state.links[0].size.x, state.links[0].size.y, state.links[0].size.z, state.links[0].color.x, state.links[0].color.y, state.links[0].color.z)
            val link2Entity = createBox(svRef, state.links[1].size.x, state.links[1].size.y, state.links[1].size.z, state.links[1].color.x, state.links[1].color.y, state.links[1].color.z)

            // Setup the render loop
            fun renderLoop(timeMs: Double) {
                if (!renderLoopActive) return
                
                val timeNs = (timeMs * 1_000_000.0).toLong()
                viewModel.updateOnFrame(timeNs)
                
                // Re-read state after update
                val currentState = viewModel.twoLinksState.value
                
                // Link 1 Transform
                val link1OriginT = translation(Float3()) * rotation(viewModel.linkOneRotation)
                val link1GeomT = link1OriginT * translation(currentState.links[0].center)
                setEntityTransform(svRef, link1Entity, link1GeomT)
                
                // Pivot 2 Transform
                val pivot2T = link1OriginT * translation(currentState.pivotPosition) * rotation(Float3(90f, 0f, 0f))
                setEntityTransform(svRef, pivot2Entity, pivot2T)
                
                // Link 2 Transform
                val link2OriginT = link1OriginT * translation(currentState.pivotPosition) * rotation(viewModel.linkTwoRotation)
                val link2GeomT = link2OriginT * translation(currentState.links[1].center)
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
