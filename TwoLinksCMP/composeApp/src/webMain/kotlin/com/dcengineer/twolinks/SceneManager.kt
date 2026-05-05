@file:OptIn(ExperimentalWasmJsInterop::class)
package com.dcengineer.twolinks

import com.dcengineer.twolinks.functions.fileLocation
import com.dcengineer.twolinks.functions.resolveEnvironmentPath
import com.dcengineer.twolinks.model.Planet
import dev.romainguy.kotlin.math.Float3
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

class SceneManager {
    // Prepend "./" for browser fetch() — Android's AssetManager doesn't need it
    val moonPath = "./${fileLocation(Planet.moon)}"
    val earthPath = "./${fileLocation(Planet.earth)}"
    val environmentPath = "./${resolveEnvironmentPath("NightSkyHDRI008_4K_HDR_ibl.ktx")}"
    val skyboxPath = "./${resolveEnvironmentPath("NightSkyHDRI008_4K_HDR_skybox.ktx")}"

    val canvas: HTMLCanvasElement = document.createElement("canvas") as HTMLCanvasElement

    init {
        canvas.width = window.innerWidth
        canvas.height = window.innerHeight
        canvas.style.width = "100%"
        canvas.style.height = "100%"
        canvas.style.display = "block"
    }
}

data class PlanetEntityRef(
    val planet: Planet,
    var entity: JsAny? = null,
    var scaleFactor: JsAny? = null
)

fun PlanetEntityRef.setup(entity: JsAny?, scaleFactor: JsAny?, svRef: JsAny) {
    this.entity = entity
    this.scaleFactor = scaleFactor

    // Apply Transform
    if (entity != null) {
        val scale = scale(Float3(scaleFactorValue))
        val transform = translation(planet.position) * rotation(planet.rotation) * scale
        setEntityTransform(svRef, entity, transform)
    }
}

val PlanetEntityRef.scaleFactorValue: Float
    get() = (scaleFactor as? JsNumber)?.toDouble()?.toFloat() ?: 1f


fun SceneManager.initSceneViewAsync(onReady: (JsAny) -> Unit) {
    initSceneViewAsync(canvas) { svRef ->
        prepareSceneEnvironments(svRef)
        onReady(svRef)
    }
}

fun SceneManager.prepareSceneEnvironments(svRef: JsAny) {
    // Load KTX1 IBL environment and skybox (generated from HDR via cmgen)
    loadEnvironment(svRef, environmentPath, 40000f)
    loadSkybox(svRef, skyboxPath)

    // Add lights
    addDirectionalLight(svRef, 100000f, 0f, -1f, -0.5f)

    // Load Planets asynchronously
    val moonEntityRef = PlanetEntityRef(Planet.moon)
    val earthEntityRef = PlanetEntityRef(Planet.earth)

    loadModelWithScaleAsync(svRef, moonPath, Planet.moon.scale) { entity, scaleFactor ->
        moonEntityRef.setup(entity, scaleFactor, svRef)
    }
    loadModelWithScaleAsync(svRef, earthPath, Planet.earth.scale) { entity, scaleFactor ->
        earthEntityRef.setup(entity, scaleFactor, svRef)
    }
}

// - scenemanager.js functions

@JsFun("initSceneViewAsync")
external fun initSceneViewAsync(canvas: HTMLCanvasElement, onReady: (JsAny) -> Unit)

@JsFun("addDirectionalLight")
external fun addDirectionalLight(sv: JsAny, intensity: Float, dx: Float, dy: Float, dz: Float)

@JsFun("createBox")
external fun createBox(sv: JsAny, sx: Float, sy: Float, sz: Float, r: Float, g: Float, b: Float): JsAny

@JsFun("createCylinder")
external fun createCylinder(sv: JsAny, radius: Float, height: Float, r: Float, g: Float, b: Float): JsAny

@JsFun("loadModelAsync")
external fun loadModelAsync(sv: JsAny, url: String, onLoaded: (JsAny) -> Unit)

@JsFun("loadModelWithScaleAsync")
external fun loadModelWithScaleAsync(sv: JsAny, url: String, desiredRadius: Float, onLoaded: (JsAny, JsNumber) -> Unit)

@JsFun("setEntityTransform")
external fun setEntityTransformJs(sv: JsAny, entity: JsAny, m00: Float, m01: Float, m02: Float, m03: Float, m10: Float, m11: Float, m12: Float, m13: Float, m20: Float, m21: Float, m22: Float, m23: Float, m30: Float, m31: Float, m32: Float, m33: Float)

@JsFun("loadEnvironment")
external fun loadEnvironment(sv: JsAny, url: String, intensity: Float)

@JsFun("loadSkybox")
external fun loadSkybox(sv: JsAny, url: String)
