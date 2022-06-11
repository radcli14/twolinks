package dcsimulationstudio.kotlyotlydobledoslinks

import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.filament.utils.HDRLoader
import dev.romainguy.kotlin.math.Float4
import io.github.sceneview.SceneView
import io.github.sceneview.environment.loadEnvironment
import io.github.sceneview.material.setBaseColor
import io.github.sceneview.material.setMetallicFactor
import io.github.sceneview.math.Position
import io.github.sceneview.math.Scale
import io.github.sceneview.node.ModelNode

const val TAG = "Main"

class MainActivity : AppCompatActivity() {
    private lateinit var sceneView: SceneView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Get the view model
        val viewModel: MainViewModel by viewModels()

        // Load the buttons that the user uses to control playback, or modify configuration
        val restartButton = findViewById<Button>(R.id.restart_button)
        val playButton = findViewById<Button>(R.id.play_button)
        val editButton = findViewById<Button>(R.id.edit_button)
        val colorButton = findViewById<Button>(R.id.color_button)

        // Set the listeners
        restartButton.setOnClickListener {
            viewModel.resetStates()
            Log.d(TAG, "restart!!! states = ${viewModel.twoLinks.θ}, $${viewModel.twoLinks.ω}")
        }
        playButton.setOnClickListener {
            viewModel.pause()
            val playIconId = if (viewModel.isPaused) R.drawable.pause else R.drawable.play
            playButton.setCompoundDrawablesWithIntrinsicBounds(playIconId, 0, 0, 0);
            Log.d(TAG, if (viewModel.isPaused) "pause..." else "play!!!")
            Log.d(TAG, "playIconId = $playIconId")
        }
        editButton.setOnClickListener { Log.d(TAG, "edit!!!") }
        colorButton.setOnClickListener { Log.d(TAG, "color???") }

        // Load the SceneView, used for 3D rendering
        sceneView = findViewById(R.id.sceneView)

        // Demo from github below this line
        sceneView.camera.position = Position(x = 0.0f, y = 0.0f, z = 2.5f)
        //sceneView.camera.rotation = Rotation(z = 180f)

        val moonNode = ModelNode()
        sceneView.addChild(moonNode)

        val doorNode = ModelNode()
        sceneView.addChild(doorNode)

        val linkOneNode = ModelNode()
        sceneView.addChild(linkOneNode)

        lifecycleScope.launchWhenCreated {
            sceneView.environment = HDRLoader.loadEnvironment(
                context = this@MainActivity,
                lifecycle = lifecycle,
                hdrFileLocation = "environments/studio_small_09_2k.hdr",
                //specularFilter = false,
                createSkybox = false
            )

            // Load the sphere model to form the moon part of the background
            moonNode.loadModel(
                context = this@MainActivity,
                lifecycle = lifecycle,
                glbFileLocation = "models/sphere.glb",
                centerOrigin = Position(x = 0.0f, y = 0.0f, z = 0.0f)
            )
            moonNode.scale = Scale(31.4f, 31.4f, 31.4f)
            moonNode.position = Position(0.0f, -32.415f, 0.0f)

            // Set the visual properties of the moon
            val moonMaterial = moonNode.modelInstance?.material?.filamentMaterialInstance
            moonMaterial?.setBaseColor(Float4(0.5f, 0.5f, 0.5f, 1.0f))

            // Load the box model to form the door part of the background
            doorNode.loadModel(
                context = this@MainActivity,
                lifecycle = lifecycle,
                glbFileLocation = "models/box.glb",
                centerOrigin = Position(x = 0.0f, y = 0.0f, z = 0.0f)
            )
            doorNode.position = Position(0.0f, 0.0f, 0.0f)
            doorNode.scale = Scale(0.91f, 2.03f, 0.035f)

            // Set the visual properties of the door
            val doorMat = doorNode.modelInstance?.material?.filamentMaterialInstance
            doorMat?.setBaseColor(Float4(0.0f, 0.0f, 0.0f, 1.0f))
            doorMat?.setMetallicFactor(1.0f)

            // Define the first pendulum link
            val pos = viewModel.twoLinks.position
            Log.d(TAG, "pos = $pos (${pos[0]}, ${pos[1]}, ${pos[2]})")
            linkOneNode.loadModel(
                context = this@MainActivity,
                lifecycle = lifecycle,
                glbFileLocation = "models/box.glb",
                centerOrigin = Position(x = 0.0f, y = 0.0f, z = 0.0f)
            )
            linkOneNode.position = Position(0.5f * viewModel.twoLinks.length[0], 0.0f, viewModel.twoLinks.thickness[0])
            linkOneNode.scale = Scale(viewModel.twoLinks.length[0], viewModel.twoLinks.height[0], viewModel.twoLinks.thickness[0])
            val linkOneMaterial = linkOneNode.modelInstance?.material?.filamentMaterialInstance
            linkOneMaterial?.setBaseColor(Float4(1.0f, 1.0f, 1.0f, 1.0f))
        }
    }
}