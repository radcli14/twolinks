package dcsimulationstudio.kotlyotlydobledoslinks

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.lifecycleScope
import com.google.android.filament.utils.HDRLoader
import io.github.sceneview.SceneView
import io.github.sceneview.environment.loadEnvironment
import io.github.sceneview.math.Position
import io.github.sceneview.math.Rotation
import io.github.sceneview.node.ModelNode
import kotlinx.coroutines.delay

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Load the buttons that the user uses to control playback, or modify configuration
        val restartButton = findViewById<Button>(R.id.restart_button)
        val playButton = findViewById<Button>(R.id.play_button)
        val editButton = findViewById<Button>(R.id.edit_button)
        val colorButton = findViewById<Button>(R.id.color_button)

        // Load the SceneView, used for 3D rendering
        val sceneView = findViewById<SceneView>(R.id.sceneView)

        // Demo from github below this line
        sceneView.camera.position = Position(x = 4.0f, y = -1.0f)
        sceneView.camera.rotation = Rotation(x = 0.0f, y = 80.0f)

        val modelNode = ModelNode()
        sceneView.addChild(modelNode)

        lifecycleScope.launchWhenCreated {
            sceneView.environment = HDRLoader.loadEnvironment(
                context = this@MainActivity,
                lifecycle = lifecycle,
                hdrFileLocation = "environments/studio_small_09_2k.hdr",
                specularFilter = false
            )

            modelNode.loadModel(
                context = this@MainActivity,
                lifecycle = lifecycle,
                glbFileLocation = "https://sceneview.github.io/assets/models/MaterialSuite.glb",
                autoAnimate = true,
                autoScale = true,
                centerOrigin = Position(x = 0.0f, y = 0.0f, z = 0.0f)
            )
            // We currently have an issue while the model render is not completely loaded
            delay(200)
            //isLoading = false
            sceneView.camera.smooth(
                position = Position(x = -1.0f, y = 1.5f, z = -3.5f),
                rotation = Rotation(x = -60.0f, y = -50.0f),
                speed = 0.5f
            )
        }
    }

}