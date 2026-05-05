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
import com.dcengineer.twolinks.model.Planet
import com.dcengineer.twolinks.model.center
import com.dcengineer.twolinks.model.size
import dev.romainguy.kotlin.math.Float3
import dev.romainguy.kotlin.math.Mat4
import dev.romainguy.kotlin.math.rotation
import dev.romainguy.kotlin.math.scale
import dev.romainguy.kotlin.math.translation
import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.HTMLCanvasElement
import kotlin.js.ExperimentalWasmJsInterop
import kotlin.js.JsAny
import kotlin.js.JsNumber
import kotlin.js.toDouble

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
        val canvas = document.createElement("canvas") as HTMLCanvasElement
        canvas.width = window.innerWidth
        canvas.height = window.innerHeight
        canvas.style.width = "100%"
        canvas.style.height = "100%"
        canvas.style.display = "block"
        
        val container = document.getElementById("scene-target")
        container?.appendChild(canvas)
        
        var renderLoopActive = true
        
        initSceneViewAsync(canvas) { svRef ->
            if (!renderLoopActive) return@initSceneViewAsync

            // Load KTX1 IBL environment and skybox (generated from HDR via cmgen)
            loadEnvironment(svRef, sceneManager.environmentPath, 40000f)
            loadSkybox(svRef, sceneManager.skyboxPath)

            // Add lights
            addDirectionalLight(svRef, 100000f, 0f, -1f, -0.5f)

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

            // Load Planets asynchronously
            val moonEntityRef = EntityRef()
            val earthEntityRef = EntityRef()
            
            loadModelWithScaleAsync(svRef, sceneManager.moonPath, Planet.moon.scale) { entity, scaleFactor ->
                moonEntityRef.entity = entity
                moonEntityRef.scaleFactor = scaleFactor
            }
            loadModelWithScaleAsync(svRef, sceneManager.earthPath, Planet.earth.scale) { entity, scaleFactor ->
                earthEntityRef.entity = entity
                earthEntityRef.scaleFactor = scaleFactor
            }

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
                
                // Moon Transform
                val moonEntity = moonEntityRef.entity
                if (moonEntity != null) {
                    val moonScale = scale(Float3(moonEntityRef.scaleFactorValue))
                    val moonT = translation(Planet.moon.position) * rotation(Planet.moon.rotation) * moonScale
                    setEntityTransform(svRef, moonEntity, moonT)
                }

                // Earth Transform — scaleToUnits equivalent via bounding box
                val earthEntity = earthEntityRef.entity
                if (earthEntity != null) {
                    val earthScale = scale(Float3(earthEntityRef.scaleFactorValue))
                    val earthT = translation(Planet.earth.position) * rotation(Planet.earth.rotation) * earthScale
                    setEntityTransform(svRef, earthEntity, earthT)
                }
                
                window.requestAnimationFrame(::renderLoop)
            }
            
            window.requestAnimationFrame(::renderLoop)
        }

        onDispose {
            renderLoopActive = false
            canvas.remove()
        }
    }
}

data class EntityRef(
    var entity: JsAny? = null,
    var scaleFactor: JsAny? = null
)

val EntityRef.scaleFactorValue: Float
    get() = (scaleFactor as? JsNumber)?.toDouble()?.toFloat() ?: 1f


fun setEntityTransform(svRef: JsAny, entity: JsAny, mat: Mat4) {
    setEntityTransformJs(svRef, entity,
        mat.x.x, mat.x.y, mat.x.z, mat.x.w,
        mat.y.x, mat.y.y, mat.y.z, mat.y.w,
        mat.z.x, mat.z.y, mat.z.z, mat.z.w,
        mat.w.x, mat.w.y, mat.w.z, mat.w.w
    )
}

// - scenemanager.js functions

@OptIn(ExperimentalWasmJsInterop::class)
@JsFun("initSceneViewAsync")
external fun initSceneViewAsync(canvas: HTMLCanvasElement, onReady: (JsAny) -> Unit)

@OptIn(ExperimentalWasmJsInterop::class)
@JsFun("addDirectionalLight")
external fun addDirectionalLight(sv: JsAny, intensity: Float, dx: Float, dy: Float, dz: Float)

@OptIn(ExperimentalWasmJsInterop::class)
@JsFun("createBox")
external fun createBox(sv: JsAny, sx: Float, sy: Float, sz: Float, r: Float, g: Float, b: Float): JsAny

@OptIn(ExperimentalWasmJsInterop::class)
@JsFun("createCylinder")
external fun createCylinder(sv: JsAny, radius: Float, height: Float, r: Float, g: Float, b: Float): JsAny

@OptIn(ExperimentalWasmJsInterop::class)
@JsFun("loadModelAsync")
external fun loadModelAsync(sv: JsAny, url: String, onLoaded: (JsAny) -> Unit)

@OptIn(ExperimentalWasmJsInterop::class)
@JsFun("loadModelWithScaleAsync")
external fun loadModelWithScaleAsync(sv: JsAny, url: String, desiredRadius: Float, onLoaded: (JsAny, JsNumber) -> Unit)

@OptIn(ExperimentalWasmJsInterop::class)
@JsFun("setEntityTransform")
external fun setEntityTransformJs(sv: JsAny, entity: JsAny, m00: Float, m01: Float, m02: Float, m03: Float, m10: Float, m11: Float, m12: Float, m13: Float, m20: Float, m21: Float, m22: Float, m23: Float, m30: Float, m31: Float, m32: Float, m33: Float)

@OptIn(ExperimentalWasmJsInterop::class)
@JsFun("loadEnvironment")
external fun loadEnvironment(sv: JsAny, url: String, intensity: Float)

@OptIn(ExperimentalWasmJsInterop::class)
@JsFun("loadSkybox")
external fun loadSkybox(sv: JsAny, url: String)
