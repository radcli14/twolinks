package dcsimulationstudio.kotlyotlydobledoslinks

import android.os.Bundle
import android.util.Log
import android.view.Choreographer
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.filament.utils.HDRLoader
import com.google.android.material.bottomsheet.BottomSheetDialog
import dev.romainguy.kotlin.math.Float4
import io.github.sceneview.SceneView
import io.github.sceneview.environment.loadEnvironment
import io.github.sceneview.material.setBaseColor
import io.github.sceneview.material.setMetallicFactor
import io.github.sceneview.math.Position
import io.github.sceneview.math.Rotation
import io.github.sceneview.math.Scale
import io.github.sceneview.node.ModelNode
import kotlin.random.Random

const val TAG = "Main"

class MainActivity : AppCompatActivity() {
    private lateinit var sceneView: SceneView
    private lateinit var choreographer: Choreographer
    private lateinit var linkOne: ModelNode
    private lateinit var linkTwo: ModelNode
    private lateinit var pivot: ModelNode
    private lateinit var playButton: Button

    val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Get the choreographer, so that we can get callbacks each frame
        choreographer = Choreographer.getInstance()

        // The playButton must be persistent in the class so that we can modify its icon
        playButton = findViewById(R.id.play_button)

        // Set the listeners
        findViewById<Button>(R.id.restart_button).setOnClickListener { restart() }
        playButton.setOnClickListener { play() }
        findViewById<Button>(R.id.edit_button).setOnClickListener { edit() }
        findViewById<Button>(R.id.color_button).setOnClickListener { color() }

        // Load the SceneView, used for 3D rendering
        sceneView = findViewById(R.id.sceneView)

        // Build the scene
        lifecycleScope.launchWhenCreated {
            // Create the camera, initially at a position in front of the door, orbits around it
            sceneView.camera.position = Position(x = 0.0f, y = 0.0f, z = 2.5f)
            //sceneView.camera.rotation = Rotation(z = 180f)

            // The environment defines the image based lighting
            sceneView.environment = HDRLoader.loadEnvironment(
                context = this@MainActivity,
                lifecycle = lifecycle,
                hdrFileLocation = "environments/studio_small_09_2k.hdr",
                //specularFilter = false,
                createSkybox = false
            )

            // Load the sphere model to form the moon part of the background
            val moon = makePlanet(
                y = -32.415f,
                radius = 31.4f,
                red = 0.5f,
                green = 0.5f,
                blue = 0.5f
            )
            sceneView.addChild(moon)

            // Load the box model to form the door part of the background
            val door = makeBox(
                z = -0.0175f,
                scaleX = 0.91f,
                scaleY = 2.03f,
                scaleZ = 0.035f,
                metallic = 1f
            )
            sceneView.addChild(door)

            // Load the hinges at the origin and pivot point
            val hinge = makeHinge(
                z = viewModel.twoLinks.thickness[0]
            )
            val pivotPosition = viewModel.twoLinks.pivotPosition
            pivot = makeHinge(
                length = 0.013f,
                x = pivotPosition.x,
                y = pivotPosition.y,
                z = pivotPosition.z
            )
            sceneView.addChild(hinge)
            sceneView.addChild(pivot)

            // Define the pendulum links
            linkOne = makeBox(
                scaleX = viewModel.twoLinks.length[0],
                scaleY = viewModel.twoLinks.height[0],
                scaleZ = viewModel.twoLinks.thickness[0],
                randomColor = true
            )
            linkTwo = makeBox(
                scaleX = viewModel.twoLinks.length[1],
                scaleY = viewModel.twoLinks.height[1],
                scaleZ = viewModel.twoLinks.thickness[1],
                randomColor = true
            )
            sceneView.addChild(linkOne)
            sceneView.addChild(linkTwo)

        }
    }

    private fun restart() {
        viewModel.resetStates()
        Log.d(TAG, "restart!!! states = ${viewModel.twoLinks.theta}, $${viewModel.twoLinks.omega}")
    }

    private fun play() {
        viewModel.pause()
        val playIconId = if (viewModel.isPaused) R.drawable.pause else R.drawable.play
        playButton.setCompoundDrawablesWithIntrinsicBounds(playIconId, 0, 0, 0)
        Log.d(TAG, if (viewModel.isPaused) "pause..." else "play!!!")
        Log.d(TAG, "playIconId = $playIconId")
    }

    private fun edit() {
        Log.d(TAG, "edit!!!")
        // Initialize the bottom dialog
        val dialog = BottomSheetDialog(this)
        val bottomLayout = layoutInflater.inflate(R.layout.bottom_layout, null)

        // Get the three lines, which are each linear layouts containing two sliders
        val lineOne = bottomLayout.findViewById<LinearLayout>(R.id.lineOne)
        val lineTwo = bottomLayout.findViewById<LinearLayout>(R.id.lineTwo)
        val lineThree = bottomLayout.findViewById<LinearLayout>(R.id.lineThree)

        // Create sliders for each of the dimensions
        val linkOneLengthSlider = View.inflate(this, R.layout.text_slider, null)
        val linkTwoLengthSlider = View.inflate(this, R.layout.text_slider, null)
        val linkOneOffsetSlider = View.inflate(this, R.layout.text_slider, null)
        val linkTwoOffsetSlider = View.inflate(this, R.layout.text_slider, null)
        val pivotSlider = View.inflate(this, R.layout.text_slider, null)

        // Add the text labels to the sliders
        linkOneLengthSlider.findViewById<TextView>(R.id.slider_text).text = getString(R.string.link_one_length)
        linkTwoLengthSlider.findViewById<TextView>(R.id.slider_text).text = getString(R.string.link_two_length)
        linkOneOffsetSlider.findViewById<TextView>(R.id.slider_text).text = getString(R.string.link_one_offset)
        linkTwoOffsetSlider.findViewById<TextView>(R.id.slider_text).text = getString(R.string.link_two_offset)
        pivotSlider.findViewById<TextView>(R.id.slider_text).text = getString(R.string.pivot)

        // Set sliders to the normalized values

        // Add the bindings to the sliders

        // Add the sliders to the view
        lineOne.addView(linkOneLengthSlider)
        lineOne.addView(linkTwoLengthSlider)
        lineTwo.addView(linkOneOffsetSlider)
        lineTwo.addView(linkTwoOffsetSlider)
        lineThree.addView(pivotSlider)

        // Show the bottom dialog
        dialog.setContentView(bottomLayout)
        dialog.show()
    }

    private fun color() {
        Log.d(TAG, "color???")
    }

    /**
     * Load a sphere representing a planet
     */
    private suspend fun makePlanet(x: Float = 0f, y: Float = 0f, z: Float = 0f,
                                   radius: Float = 1f,
                                   red: Float = 0f, green: Float = 0f, blue: Float = 0f
    ) : ModelNode {
        val node = ModelNode()
        node.loadModel(
            context = this@MainActivity,
            lifecycle = lifecycle,
            glbFileLocation = "models/sphere.glb",
            centerOrigin = Position(x = 0f, y = 0f, z = 0f)
        )
        node.scale = Scale(radius, radius, radius)
        node.position = Position(x, y, z)
        val material = node.modelInstance?.material?.filamentMaterialInstance
        material?.setBaseColor(Float4(red, green, blue, 1.0f))
        return node
    }

    /**
     * Load the box model for a pre-existing node
     */
    private suspend fun makeBox(x: Float = 0f, y: Float = 0f, z: Float = 0f,
                                scaleX: Float = 1f, scaleY: Float = 1f, scaleZ: Float = 1f,
                                red: Float = 0f, green: Float = 0f, blue: Float = 0f,
                                randomColor: Boolean = false, metallic: Float = 0f
    ) : ModelNode {
        val node = ModelNode()
        node.loadModel(
            context = this@MainActivity,
            lifecycle = lifecycle,
            glbFileLocation = "models/box.glb",
            centerOrigin = Position(x = 0.0f, y = 0.0f, z = 0.0f)
        )
        node.position = Position(x, y, z)
        node.scale = Scale(scaleX, scaleY, scaleZ)
        val material = node.modelInstance?.material?.filamentMaterialInstance
        val color = when(randomColor) {
            true -> Float4(Random.nextFloat(), Random.nextFloat(), Random.nextFloat(), 1f)
            false -> Float4(red, green, blue, 1.0f)
        }
        material?.setBaseColor(color)
        material?.setMetallicFactor(metallic)
        return node
    }

    /**
     * Load the cylinders representing the hinge points at the origin and pivot
     */
    private suspend fun makeHinge(x: Float = 0f, y: Float = 0f, z: Float = 0f,
                                  length: Float = 0.007f, radius: Float = 0.007f
    ) : ModelNode {
        val node = ModelNode()
        node.loadModel(
            context = this@MainActivity,
            lifecycle = lifecycle,
            glbFileLocation = "models/cylinder.glb",
            centerOrigin = Position(x = 0.0f, y = 0.0f, z = 0.0f)
        )
        node.position = Position(x, y, z)
        node.scale = Scale(length, radius, radius)
        node.rotation = Rotation(0f, 90f, 0f)
        val material = node.modelInstance?.material?.filamentMaterialInstance
        material?.setBaseColor(Float4(0.2f, 0.2f, 0.2f, 1.0f))
        return node
    }

    override fun onResume() {
        super.onResume()
        choreographer.postFrameCallback(frameCallback)
    }

    override fun onPause() {
        super.onPause()
        choreographer.removeFrameCallback(frameCallback)
    }

    // Float value for the time at the last frame, used to determine differential time
    var lastTime = 0f

    /**
     * Call once every frame, update the positions and orientations of the links
     */
    private val frameCallback = object : Choreographer.FrameCallback {
        override fun doFrame(currentTime: Long) {
            // Obtain the timing data, dt is differential time
            val currentSeconds = currentTime.toFloat() / 1_000_000_000
            val dt = if (lastTime == 0f) 0f else currentSeconds - lastTime

            // Make sure the scene is ready and not paused, then do the update
            if (sceneIsInitialized && !viewModel.isPaused) {
                viewModel.update(dt)
                linkOne.position = viewModel.linkOnePosition
                linkOne.rotation = viewModel.linkOneRotation
                linkTwo.position = viewModel.linkTwoPosition
                linkTwo.rotation = viewModel.linkTwoRotation
                pivot.position.x = viewModel.twoLinks.pivotPosition.x
                pivot.position.y = viewModel.twoLinks.pivotPosition.y
            }

            // Set up the next frame update
            lastTime = currentSeconds
            choreographer.postFrameCallback(this)
        }
    }

    /**
     * Check that all the lateinit variables have been initialized
     */
    private val sceneIsInitialized: Boolean
        get() {
            return this::sceneView.isInitialized &&
                    this::linkOne.isInitialized &&
                    this::linkTwo.isInitialized &&
                    this::pivot.isInitialized
        }
}