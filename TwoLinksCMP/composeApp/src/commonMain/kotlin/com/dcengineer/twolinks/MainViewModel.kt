package com.dcengineer.twolinks

import androidx.lifecycle.ViewModel
import dev.romainguy.kotlin.math.Float3
import dev.romainguy.kotlin.math.Float4
import kotlin.math.PI
import kotlin.random.Random
import kotlin.time.Clock

class MainViewModel : ViewModel() {

    val twoLinks = TwoLinks()

    var isPaused = false

    var linkOnePosition = twoLinks.position[0]
    var linkTwoPosition = twoLinks.position[1]
    var linkOneRotation = Float3(0.0f, 0.0f, 0.0f)
    var linkTwoRotation = Float3(0.0f, 0.0f, 0.0f)

    var linkOneColor = Float4(1f, 0f, 0f, 1f)
    var linkTwoColor = Float4(0f, 1f, 0f, 1f)

    val linkOneLengthNorm: Int
        get() = (100f * twoLinks.linkOneLengthNorm).toInt()

    val linkTwoLengthNorm: Int
        get() = (100f * twoLinks.linkTwoLengthNorm).toInt()

    val linkOneOffsetNorm: Int
        get() = (100f * twoLinks.linkOneOffsetNorm).toInt()

    val linkTwoOffsetNorm: Int
        get() = (100f * twoLinks.linkTwoOffsetNorm).toInt()

    val pivotNorm: Int
        get() = (100f * twoLinks.pivotNorm).toInt()

    init {
        shuffle()
    }

    /**
     * Reset the link angles and angular rates to zero
     */
    fun resetStates() {
        twoLinks.theta = floatArrayOf(0.0f, 0.0f)
        twoLinks.omega = floatArrayOf(0.0f, 0.0f)
    }

    /**
     * Pause the animation
     */
    fun pause() {
        isPaused = !isPaused
    }

    /**
     * Update the link positions and rotations, with a differential time specified by h.
     */
    fun update(h: Float) {
        twoLinks.update(h)
        val pos = twoLinks.position
        linkOnePosition = pos[0]
        linkTwoPosition = pos[1]
        linkOneRotation.z = twoLinks.theta[0] * 180f / PI.toFloat()
        linkTwoRotation.z = twoLinks.theta[1] * 180f / PI.toFloat()
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