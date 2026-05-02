package com.dcengineer.twolinks

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.dcengineer.twolinks.functions.rad2deg
import com.dcengineer.twolinks.model.Planet
import com.dcengineer.twolinks.model.TwoLinks
import dev.romainguy.kotlin.math.Float2
import dev.romainguy.kotlin.math.Float3
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.math.min
import kotlin.random.Random
import kotlin.time.Clock

class MainViewModel : ViewModel() {

    private val _twoLinksState = MutableStateFlow(TwoLinks())
    val twoLinksState = _twoLinksState.asStateFlow()
    val twoLinks: TwoLinks get() = twoLinksState.value

    var isPaused = mutableStateOf(false)

    var lastFrameTime: Long? = null

    private val _elapsedTimeState = MutableStateFlow(0f)
    val elapsedTimeState = _elapsedTimeState.asStateFlow()
    val elapsedTime: Float get() = elapsedTimeState.value
    val maxFrameTime = 0.03f  // Limit to prevent simulation crashing from over-stepping

    val doorSize = Float3(0.91f, 2.03f, 0.035f)

    val filesPath = "composeResources/twolinkscmp.composeapp.generated.resources/files"
    val environmentsPath = "$filesPath/environments"
    val modelPath = "$filesPath/models"
    fun fileLocation(planet: Planet): String {
        return "$modelPath/${planet.file}"
    }

    val anglesDegrees: Float2
        get() = Float2(twoLinksState.value.simulationState[0] * rad2deg, twoLinksState.value.simulationState[1] * rad2deg)

    val linkOneRotation: Float3
        get() = Float3(0f, 0f, anglesDegrees[0])

    val linkTwoRotation: Float3
        get() = Float3(0f, 0f, anglesDegrees[1] - anglesDegrees[0])

    init {
        shuffle()
    }

    /**
     * Reset the link angles and angular rates to zero
     */
    fun resetStates() {
        _elapsedTimeState.value = 0f
        lastFrameTime = null
        _twoLinksState.update { current ->
            current.copy(links = current.links.copyOf())
        }
    }

    /**
     * Pause the animation
     */
    fun pause() {
        lastFrameTime = null
        isPaused.value = !isPaused.value
    }

    /**
     * Update the link positions and rotations, with a differential time specified by h.
     */
    fun update(h: Float) {
        _twoLinksState.update { current ->
            current.update(h)
            current.copy(links = current.links.copyOf())
        }
        _elapsedTimeState.value += h
    }

    /**
     * Update the link positions and rotations, with an absolute unix time specified in the current frame
     */
    fun updateOnFrame(frameTime: Long) {
        lastFrameTime?.let {
            val deltaFrameTime: Float = min(maxFrameTime, (frameTime - it).toFloat() / 1_000_000_000)
            if (!isPaused.value) {
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
        twoLinks.links[0].color.x = random.nextFloat()
        twoLinks.links[0].color.y = random.nextFloat()
        twoLinks.links[0].color.z = random.nextFloat()
        twoLinks.links[1].color.x = random.nextFloat()
        twoLinks.links[1].color.y = random.nextFloat()
        twoLinks.links[1].color.z = random.nextFloat()
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