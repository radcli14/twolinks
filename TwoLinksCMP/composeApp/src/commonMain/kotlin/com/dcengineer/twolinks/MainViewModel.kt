package com.dcengineer.twolinks

import androidx.lifecycle.ViewModel
import com.dcengineer.twolinks.model.Planet
import com.dcengineer.twolinks.model.Position
import com.dcengineer.twolinks.model.TwoLinks
import com.dcengineer.twolinks.model.lengthNorm
import com.dcengineer.twolinks.model.offsetNorm
import com.dcengineer.twolinks.model.position
import com.dcengineer.twolinks.model.updateState
import dev.romainguy.kotlin.math.Float3
import dev.romainguy.kotlin.math.Float4
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.math.PI
import kotlin.random.Random
import kotlin.time.Clock

class MainViewModel : ViewModel() {

    private val _twoLinksState = MutableStateFlow(TwoLinks())
    val twoLinksState = _twoLinksState.asStateFlow()
    val twoLinks: TwoLinks get() = twoLinksState.value

    var isPaused = false

    var lastFrameTime: Long? = null
    var elapsedTime: Float = 0f

    val doorSize = Float3(0.91f, 2.03f, 0.035f)

    val modelPath = "composeResources/twolinkscmp.composeapp.generated.resources/files/models"
    fun fileLocation(planet: Planet): String {
        return "$modelPath/${planet.file}"
    }

    /*val linkOnePosition: Position
        get() = twoLinks.links[0].position()
    val linkTwoPosition: Position
        get() = twoLinks.links[1].position(zOffset = twoLinks.links[0].thickness)
    val linkOneRotation: Float3
        get() = Float3(0.0f, 0.0f,  twoLinks.links[0].theta * 180f / PI.toFloat())
    val linkTwoRotation: Float3
        get() = Float3(0.0f, 0.0f, twoLinks.links[1].theta * 180f / PI.toFloat())*/

    var linkOneColor = Float4(1f, 0f, 0f, 1f)
    var linkTwoColor = Float4(0f, 1f, 0f, 1f)

    /*val linkOneLengthNorm: Int
        get() = (100f * twoLinks.links[0].lengthNorm).toInt()

    val linkTwoLengthNorm: Int
        get() = (100f * twoLinks.links[1].lengthNorm).toInt()

    val linkOneOffsetNorm: Int
        get() = (100f * twoLinks.links[0].offsetNorm).toInt()

    val linkTwoOffsetNorm: Int
        get() = (100f * twoLinks.links[1].offsetNorm).toInt()

    val pivotNorm: Int
        get() = (100f * twoLinks.pivotNorm).toInt()*/

    init {
        shuffle()
    }

    /**
     * Reset the link angles and angular rates to zero
     */
    fun resetStates() {
        elapsedTime = 0f
        lastFrameTime = null
        _twoLinksState.update { current ->
            current.links[0].updateState(newTheta = 0f, newOmega = 0f)
            current.links[1].updateState(newTheta = 0f, newOmega = 0f)
            current.copy(links = current.links.copyOf())
        }
    }

    /**
     * Pause the animation
     */
    fun pause() {
        lastFrameTime = null
        isPaused = !isPaused
    }

    /**
     * Update the link positions and rotations, with a differential time specified by h.
     */
    fun update(h: Float) {
        _twoLinksState.update { current ->
            current.update(h)
            current.copy(links = current.links.copyOf())
        }
        elapsedTime += h
    }

    /**
     * Update the link positions and rotations, with an absolute unix time specified in the current frame
     */
    fun updateOnFrame(frameTime: Long) {
        lastFrameTime?.let {
            val deltaFrameTime: Float = (frameTime - it).toFloat() / 1_000_000_000
            if (!isPaused) {
                update(h = deltaFrameTime)
            }
        }
        lastFrameTime = frameTime
    }

    /**
     * Randomize the initial dimensions and colors
     */
    private fun shuffle() {
        // Create a random number generator, seeded with the current time
        val now = Clock.System.now()
        val seed = now.toEpochMilliseconds()
        val random = Random(seed)

        // Create the randomized link dimensions
        twoLinks.setLinkOneLengthFromNorm(random.nextFloat())
        twoLinks.setLinkTwoLengthFromNorm(random.nextFloat())
        twoLinks.setLinkOneOffsetFromNorm(random.nextFloat())
        twoLinks.setLinkTwoOffsetFromNorm(random.nextFloat())
        twoLinks.setPivotFromNorm(random.nextFloat())

        // Create the randomized colors
        linkOneColor.x = random.nextFloat()
        linkOneColor.y = random.nextFloat()
        linkOneColor.z = random.nextFloat()
        linkTwoColor.x = random.nextFloat()
        linkTwoColor.y = random.nextFloat()
        linkTwoColor.z = random.nextFloat()
    }

    fun setLinkOneLengthFromNorm(n: Int) {
        twoLinks.setLinkOneLengthFromNorm(0.01f * n.toFloat())
    }

    fun setLinkTwoLengthFromNorm(n: Int) {
        twoLinks.setLinkTwoLengthFromNorm(0.01f * n.toFloat())
    }

    fun setLinkOneOffsetFromNorm(n: Int) {
        twoLinks.setLinkOneOffsetFromNorm(0.01f * n.toFloat())
    }

    fun setLinkTwoOffsetFromNorm(n: Int) {
        twoLinks.setLinkTwoOffsetFromNorm(0.01f * n.toFloat())
    }

    fun setPivotFromNorm(n: Int) {
        twoLinks.setPivotFromNorm(0.01f * n.toFloat())
    }
}