package com.dcengineer.twolinks

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.google.android.filament.Filament
import com.google.android.filament.gltfio.Gltfio
import io.github.sceneview.SceneView
import io.github.sceneview.rememberModelInstance

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // Important to initialize here so that the SceneManager doesn't crash on startup
        Filament.init()
        Gltfio.init()

        setContent {
            val viewModel = MainViewModel()
            App(viewModel)
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}