package com.dcengineer.twolinks

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import kotlinx.browser.document
import org.w3c.dom.HTMLCanvasElement
import kotlin.js.ExperimentalWasmJsInterop

@OptIn(ExperimentalWasmJsInterop::class)
@Composable
actual fun TwoLinksSceneView() {
    // The path relative to the web server root
    val modelPath = "./composeResources/twolinkscmp.composeapp.generated.resources/files/models/moon.glb"

    DisposableEffect(Unit) {
        // Create the canvas element that we will put the SceneView inside
        val canvas = document.createElement("canvas") as HTMLCanvasElement
        canvas.style.width = "100%"
        canvas.style.height = "100%"
        canvas.style.display = "block"

        // Add the canvas to the scene-target we defined in index.html
        val container = document.getElementById("scene-target")
        container?.appendChild(canvas)

        // Call our top-level Wasm bridge
        initSceneView(canvas, modelPath)
        onDispose {
            // Cleanup: Remove the canvas when the Composable is destroyed
            canvas.remove()
        }
    }

    Text("This is compose")
}

@OptIn(ExperimentalWasmJsInterop::class)
@JsFun("""
    (canvas, modelUrl) => { 
        SceneView.modelViewer(canvas, modelUrl, { 
            autoAnimate: true, 
            cameraControls: true 
        });
    }
""")
external fun initSceneView(canvas: HTMLCanvasElement, modelUrl: String)