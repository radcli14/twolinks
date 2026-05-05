@file:OptIn(ExperimentalWasmJsInterop::class)
package com.dcengineer.twolinks

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import com.dcengineer.twolinks.functions.fileLocation
import com.dcengineer.twolinks.functions.resolveEnvironmentPath
import com.dcengineer.twolinks.model.Planet
import com.dcengineer.twolinks.model.center
import com.dcengineer.twolinks.model.size
import dev.romainguy.kotlin.math.*
import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.HTMLCanvasElement
import kotlin.js.ExperimentalWasmJsInterop

// Note, can run using ./gradlew clean && ./gradlew :composeApp:wasmJsBrowserDevelopmentRun`

@OptIn(ExperimentalWasmJsInterop::class)
@Composable
actual fun TwoLinksSceneView(viewModel: MainViewModel) {
    val state by viewModel.twoLinksState.collectAsState()
    
    // Prepend "./" for browser fetch() — Android's AssetManager doesn't need it
    val moonPath = "./${fileLocation(Planet.moon)}"
    val earthPath = "./${fileLocation(Planet.earth)}"
    val environmentPath = "./${resolveEnvironmentPath("NightSkyHDRI008_10K_HDR_ibl.ktx")}"
    val skyboxPath = "./${resolveEnvironmentPath("NightSkyHDRI008_10K_HDR_skybox.ktx")}"

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
            loadEnvironment(svRef, environmentPath, 40000f)
            loadSkybox(svRef, skyboxPath)

            // Add lights
            addDirectionalLight(svRef, 100000f, 0f, -1f, -0.5f)

            // Create Primitives
            val doorEntity = createBox(svRef, viewModel.doorSize.x, viewModel.doorSize.y, viewModel.doorSize.z, 0.5f, 0.5f, 0.5f)
            val pivot1Entity = createCylinder(svRef, 0.01f, 0.015f, 0.5f, 0.5f, 0.5f)
            val pivot2Entity = createCylinder(svRef, 0.01f, 0.015f, 0.5f, 0.5f, 0.5f)
            
            val link1Entity = createBox(svRef, state.links[0].size.x, state.links[0].size.y, state.links[0].size.z, state.links[0].color.x, state.links[0].color.y, state.links[0].color.z)
            val link2Entity = createBox(svRef, state.links[1].size.x, state.links[1].size.y, state.links[1].size.z, state.links[1].color.x, state.links[1].color.y, state.links[1].color.z)

            // Load Planets asynchronously
            // entityRef[0] = root entity, entityRef[1] = uniform scale factor (from bounding box)
            val moonEntityRef = arrayOf<JsAny?>(null, null)
            val earthEntityRef = arrayOf<JsAny?>(null, null)
            
            loadModelWithScaleAsync(svRef, moonPath, Planet.moon.scale) { entity, scaleFactor ->
                moonEntityRef[0] = entity
                moonEntityRef[1] = scaleFactor
            }
            loadModelWithScaleAsync(svRef, earthPath, Planet.earth.scale) { entity, scaleFactor ->
                earthEntityRef[0] = entity
                earthEntityRef[1] = scaleFactor
            }

            // Setup the render loop
            fun renderLoop(timeMs: Double) {
                if (!renderLoopActive) return
                
                val timeNs = (timeMs * 1_000_000.0).toLong()
                viewModel.updateOnFrame(timeNs)
                
                // Re-read state after update
                val currentState = viewModel.twoLinksState.value
                
                // Calculate Door Transform
                val doorPos = Float3(0f, 0f, -0.5f * viewModel.doorSize.z)
                val doorOriginT = translation(doorPos)
                setEntityTransform(svRef, doorEntity, doorOriginT)
                
                // Pivot 1 Transform
                val pivot1Rot = Float3(90f, 0f, 0f)
                val pivot1T = doorOriginT * translation(Float3()) * rotation(pivot1Rot)
                setEntityTransform(svRef, pivot1Entity, pivot1T)
                
                // Link 1 Transform
                val link1OriginT = doorOriginT * translation(Float3()) * rotation(viewModel.linkOneRotation)
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
                val moonEntity = moonEntityRef[0]
                val moonScaleFactor = (moonEntityRef[1] as? JsNumber)?.toDouble()?.toFloat() ?: 1f
                if (moonEntity != null) {
                    val moonScale = scale(Float3(moonScaleFactor))
                    val moonT = translation(Planet.moon.position) * rotation(Planet.moon.rotation) * moonScale
                    setEntityTransform(svRef, moonEntity, moonT)
                }

                // Earth Transform — scaleToUnits equivalent via bounding box
                val earthEntity = earthEntityRef[0]
                val earthScaleFactor = (earthEntityRef[1] as? JsNumber)?.toDouble()?.toFloat() ?: 1f
                if (earthEntity != null) {
                    val earthScale = scale(Float3(earthScaleFactor))
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

fun setEntityTransform(svRef: JsAny, entity: JsAny, mat: Mat4) {
    setEntityTransformJs(svRef, entity,
        mat.x.x, mat.x.y, mat.x.z, mat.x.w,
        mat.y.x, mat.y.y, mat.y.z, mat.y.w,
        mat.z.x, mat.z.y, mat.z.z, mat.z.w,
        mat.w.x, mat.w.y, mat.w.z, mat.w.w
    )
}

@OptIn(ExperimentalWasmJsInterop::class)
@JsFun("""
    (canvas, onReady) => {
        SceneView.create(canvas).then(sv => {
            onReady(sv);
        }).catch(err => console.error(err));
    }
""")
external fun initSceneViewAsync(canvas: HTMLCanvasElement, onReady: (JsAny) -> Unit)

@OptIn(ExperimentalWasmJsInterop::class)
@JsFun("""
    (sv, intensity, dx, dy, dz) => {
        sv.addLight({ type: "directional", intensity: intensity, direction: [dx, dy, dz] });
    }
""")
external fun addDirectionalLight(sv: JsAny, intensity: Float, dx: Float, dy: Float, dz: Float)

@OptIn(ExperimentalWasmJsInterop::class)
@JsFun("""
    (sv, sx, sy, sz, r, g, b) => {
        var asset = sv.createBox([0, 0, 0], [sx, sy, sz], [r, g, b]);
        return asset ? asset.getRoot() : null;
    }
""")
external fun createBox(sv: JsAny, sx: Float, sy: Float, sz: Float, r: Float, g: Float, b: Float): JsAny

@OptIn(ExperimentalWasmJsInterop::class)
@JsFun("""
    (sv, radius, height, r, g, b) => {
        var asset = sv.createCylinder([0, 0, 0], radius, height, [r, g, b]);
        return asset ? asset.getRoot() : null;
    }
""")
external fun createCylinder(sv: JsAny, radius: Float, height: Float, r: Float, g: Float, b: Float): JsAny

@OptIn(ExperimentalWasmJsInterop::class)
@JsFun("""
    (sv, url, onLoaded) => {
        fetch(url).then(r => r.arrayBuffer()).then(buffer => {
            var data = new Uint8Array(buffer);
            var asset = sv._loader.createAsset(data);
            if (asset) {
                // Initialize resources and add to the shared scene
                asset.loadResources();
                sv._scene.addEntity(asset.getRoot());
                sv._scene.addEntities(asset.getRenderableEntities());
                
                // Return the root entity
                onLoaded(asset.getRoot());
            }
        }).catch(err => console.error("Failed to fetch model", url, err));
    }
""")
external fun loadModelAsync(sv: JsAny, url: String, onLoaded: (JsAny) -> Unit)

@OptIn(ExperimentalWasmJsInterop::class)
@JsFun("""
    (sv, url, desiredRadius, onLoaded) => {
        fetch(url).then(r => r.arrayBuffer()).then(buffer => {
            var data = new Uint8Array(buffer);
            var asset = sv._loader.createAsset(data);
            if (asset) {
                asset.loadResources();
                sv._scene.addEntity(asset.getRoot());
                sv._scene.addEntities(asset.getRenderableEntities());
                
                // Compute scaleToUnits: find the natural radius from bounding box half-extents
                var bb = asset.getBoundingBox();
                var halfExtent = bb ? Math.max(
                    Math.abs(bb.max[0] - bb.min[0]),
                    Math.abs(bb.max[1] - bb.min[1]),
                    Math.abs(bb.max[2] - bb.min[2])
                ) / 2.0 : 1.0;
                var scaleFactor = halfExtent > 0 ? 0.5 * desiredRadius / halfExtent : 1.0;
                
                console.log('Model loaded:', url);
                console.log('  Natural size (diameter) ~', halfExtent * 2);
                console.log('  Scale factor for radius', desiredRadius, ':', scaleFactor);

                onLoaded(asset.getRoot(), scaleFactor);
            }
        }).catch(err => console.error("Failed to fetch model", url, err));
    }
""")
external fun loadModelWithScaleAsync(sv: JsAny, url: String, desiredRadius: Float, onLoaded: (JsAny, JsNumber) -> Unit)

@OptIn(ExperimentalWasmJsInterop::class)
@JsFun("""
    (sv, entity, m00, m01, m02, m03, m10, m11, m12, m13, m20, m21, m22, m23, m30, m31, m32, m33) => {
        if (!entity) return;
        var tcm = sv._engine.getTransformManager();
        var inst = tcm.getInstance(entity);
        if (inst != 0) {
            var mat = [m00, m01, m02, m03, m10, m11, m12, m13, m20, m21, m22, m23, m30, m31, m32, m33];
            tcm.setTransform(inst, mat);
        }
    }
""")
external fun setEntityTransformJs(sv: JsAny, entity: JsAny, m00: Float, m01: Float, m02: Float, m03: Float, m10: Float, m11: Float, m12: Float, m13: Float, m20: Float, m21: Float, m22: Float, m23: Float, m30: Float, m31: Float, m32: Float, m33: Float)

@OptIn(ExperimentalWasmJsInterop::class)
@JsFun("""
    (sv, url, intensity) => {
        sv.loadEnvironment(url, intensity);
    }
""")
external fun loadEnvironment(sv: JsAny, url: String, intensity: Float)

@OptIn(ExperimentalWasmJsInterop::class)
@JsFun("""
    (sv, url) => {
        fetch(url).then(r => r.arrayBuffer()).then(buffer => {
            try {
                var skybox = sv._engine.createSkyFromKtx1(new Uint8Array(buffer));
                sv._scene.setSkybox(skybox);
                console.log('SceneView: Skybox loaded');
            } catch (e) {
                console.warn('SceneView: loadSkybox failed', e);
            }
        }).catch(err => console.error("Failed to fetch skybox", url, err));
    }
""")
external fun loadSkybox(sv: JsAny, url: String)