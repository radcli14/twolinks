package dcsimulationstudio.kotlyotlydobledoslinks

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.graphics.scaleMatrix
import androidx.lifecycle.lifecycleScope
import com.google.android.filament.utils.HDRLoader
import com.google.android.filament.utils.scale
import io.github.sceneview.SceneView
import io.github.sceneview.environment.loadEnvironment
import io.github.sceneview.math.Position
import io.github.sceneview.math.Rotation
import io.github.sceneview.math.Scale
import io.github.sceneview.node.ModelNode
import kotlinx.coroutines.delay

class MainActivity : AppCompatActivity() {
    private lateinit var sceneView: SceneView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Load the buttons that the user uses to control playback, or modify configuration
        val restartButton = findViewById<Button>(R.id.restart_button)
        val playButton = findViewById<Button>(R.id.play_button)
        val editButton = findViewById<Button>(R.id.edit_button)
        val colorButton = findViewById<Button>(R.id.color_button)

        // Set the listeners
        restartButton.setOnClickListener { println("restart!!!") }
        playButton.setOnClickListener { println("play!!!") }
        editButton.setOnClickListener { println("edit!!!") }
        colorButton.setOnClickListener { println("color???") }

        // Load the SceneView, used for 3D rendering
        sceneView = findViewById<SceneView>(R.id.sceneView)

        // Demo from github below this line
        sceneView.camera.position = Position(x = 0.0f, y = 0.0f, z = 2.5f)
        //sceneView.camera.rotation = Rotation(z = 180f)

        val moonNode = ModelNode()
        sceneView.addChild(moonNode)

        val doorNode = ModelNode()
        sceneView.addChild(doorNode)

        lifecycleScope.launchWhenCreated {
            sceneView.environment = HDRLoader.loadEnvironment(
                context = this@MainActivity,
                lifecycle = lifecycle,
                hdrFileLocation = "environments/studio_small_09_2k.hdr",
                //specularFilter = false,
                createSkybox = false
            )

            moonNode.loadModel(
                context = this@MainActivity,
                lifecycle = lifecycle,
                glbFileLocation = "models/point.glb",
                //centerOrigin = Position(x = 0.0f, y = 0.0f, z = 0.0f)
            )
            moonNode.position = Position(0.0f, -32.415f, 0.0f)
            moonNode.scale = Scale(31.4f, 31.4f, 31.4f)

            doorNode.loadModel(
                context = this@MainActivity,
                lifecycle = lifecycle,
                glbFileLocation = "models/box.glb",
                //centerOrigin = Position(x = 0.0f, y = 0.0f, z = 0.0f)
            )
            //doorNode.position = Position(0.0f, 0.0f, 0.0f)
            doorNode.scale = Scale(0.91f, 2.03f, 0.035f)
        }
    }
}