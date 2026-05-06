package com.dcengineer.twolinks

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.dcengineer.twolinks.functions.rad2deg
import com.dcengineer.twolinks.model.TwoLinks
import com.dcengineer.twolinks.model.maxPivot
import com.dcengineer.twolinks.model.offsetNorm
import dev.romainguy.kotlin.math.Float2
import dev.romainguy.kotlin.math.Float3
import dev.romainguy.kotlin.math.Float4
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

    var linkDimensionEditorIsVisible = mutableStateOf(false)
    var linkColorEditorIsVisible = mutableStateOf(false)

    var isPaused = mutableStateOf(false)

    var lastFrameTime: Long? = null

    private val _elapsedTimeState = MutableStateFlow(0f)
    val elapsedTimeState = _elapsedTimeState.asStateFlow()
    val elapsedTime: Float get() = elapsedTimeState.value
    val maxFrameTime = 0.1f  // Limit to prevent simulation crashing from over-stepping

    val doorSize = Float3(0.91f, 2.03f, 0.035f)

    val anglesDegrees: Float2
        get() = Float2(twoLinks.simulationState[0] * rad2deg, twoLinks.simulationState[1] * rad2deg)

    val linkOneRotation: Float3
        get() = Float3(0f, 0f, anglesDegrees[0])

    val linkTwoRotation: Float3
        get() = Float3(0f, 0f, anglesDegrees[1] - anglesDegrees[0])

    init {
        shuffle()
    }

    /**
     * Opens or closes the sheet used to edit the link dimensions
     */
    fun toggleLinkDimensionEditor() {
        linkDimensionEditorIsVisible.value = !linkDimensionEditorIsVisible.value
    }

    /**
     * Opens or closes the sheet used to edit the link colors
     */
    fun toggleLinkColorEditor() {
        linkColorEditorIsVisible.value = !linkColorEditorIsVisible.value
    }

    /**
     * Reset the link angles and angular rates to zero
     */
    fun resetStates() {
        _elapsedTimeState.value = 0f
        lastFrameTime = null
        _twoLinksState.update { current ->
            current.copy(
                simulationState = Float4(),
                links = current.links.toList()
            )
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
            val newState = current.getUpdatedState(h)
            current.copy(simulationState = newState)
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
    fun shuffle() {
        // Create a random number generator, seeded with the current time
        val now = Clock.System.now()
        val seed = now.toEpochMilliseconds()
        val random = Random(seed)

        // Create the randomized link dimensions
        setLinkOneLengthFromNorm(random.nextFloat())
        setLinkTwoLengthFromNorm(random.nextFloat())
        setLinkOneOffsetFromNorm(random.nextFloat())
        setLinkTwoOffsetFromNorm(random.nextFloat())
        setPivotFromNorm(random.nextFloat())

        // Create the randomized colors
        setLinkOneColor(Float4(random.nextFloat(), random.nextFloat(), random.nextFloat(), 1f))
        setLinkTwoColor(Float4(random.nextFloat(), random.nextFloat(), random.nextFloat(), 1f))
    }

    fun setLinkOneLengthFromNorm(n: Float) {
        _twoLinksState.update { current ->
            val link0 = current.links[0]
            val newLength = link0.minLength + n * (link0.maxLength - link0.minLength)
            val newOffset = (1.0f - link0.offsetNorm) * (0.5f * newLength - link0.minDistanceFromEdge)
            val newLink0 = link0.copy(length = newLength, offset = newOffset)
            val maxPivot = 0.5f * newLength - link0.minDistanceFromEdge + newOffset
            val newPivot = min(current.pivot, maxPivot)
            current.copy(links = listOf(newLink0, current.links[1]), pivot = newPivot)
        }
    }

    fun setLinkTwoLengthFromNorm(n: Float) {
        _twoLinksState.update { current ->
            val link1 = current.links[1]
            val newLength = link1.minLength + n * (link1.maxLength - link1.minLength)
            val newOffset = (1.0f - link1.offsetNorm) * (0.5f * newLength - link1.minDistanceFromEdge)
            val newLink1 = link1.copy(length = newLength, offset = newOffset)
            current.copy(links = listOf(current.links[0], newLink1))
        }
    }

    fun setLinkOneOffsetFromNorm(n: Float) {
        _twoLinksState.update { current ->
            val link0 = current.links[0]
            val newOffset = (1.0f - n) * (0.5f * link0.length - link0.minDistanceFromEdge)
            val newLink0 = link0.copy(offset = newOffset)
            val maxPivot = 0.5f * link0.length - link0.minDistanceFromEdge + newOffset
            val newPivot = min(current.pivot, maxPivot)
            current.copy(links = listOf(newLink0, current.links[1]), pivot = newPivot)
        }
    }

    fun setLinkTwoOffsetFromNorm(n: Float) {
        _twoLinksState.update { current ->
            val link1 = current.links[1]
            val newOffset = (1.0f - n) * (0.5f * link1.length - link1.minDistanceFromEdge)
            val newLink1 = link1.copy(offset = newOffset)
            current.copy(links = listOf(current.links[0], newLink1))
        }
    }

    fun setPivotFromNorm(n: Float) {
        _twoLinksState.update { current ->
            val maxPivot = current.links[0].maxPivot
            val newPivot = n * maxPivot
            current.copy(pivot = newPivot)
        }
    }

    fun setLinkOneColor(newColor: Float4) {
        _twoLinksState.update { current ->
            val newLink = current.links[0].copy(color = newColor)
            current.copy(links = listOf(newLink, current.links[1]))
        }
    }

    fun setLinkTwoColor(newColor: Float4) {
        _twoLinksState.update { current ->
            val newLink = current.links[1].copy(color = newColor)
            current.copy(links = listOf(current.links[0], newLink))
        }
    }
}