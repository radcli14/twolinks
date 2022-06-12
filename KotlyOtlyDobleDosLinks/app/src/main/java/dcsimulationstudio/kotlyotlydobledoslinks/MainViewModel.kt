package dcsimulationstudio.kotlyotlydobledoslinks

import androidx.lifecycle.ViewModel
import dcsimulationstudio.kotlyotlydobledoslinks.models.TwoLinks
import io.github.sceneview.math.Rotation
import kotlin.random.Random

class MainViewModel : ViewModel() {

    val twoLinks = TwoLinks()

    var isPaused = false

    var linkOnePosition = twoLinks.position[0]
    var linkTwoPosition = twoLinks.position[1]
    var linkOneRotation = Rotation(0.0f, 0.0f, 0.0f)
    var linkTwoRotation = Rotation(0.0f, 0.0f, 0.0f)

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
        linkOneRotation.z = twoLinks.theta[0] * 180f / Math.PI.toFloat()
        linkTwoRotation.z = twoLinks.theta[1] * 180f / Math.PI.toFloat()
    }

    /**
     * Randomize the initial dimensions
     */
    private fun shuffle() {
        twoLinks.setLinkOneLengthFromNorm(Random.nextFloat())
        twoLinks.setLinkTwoLengthFromNorm(Random.nextFloat())
        twoLinks.setLinkOneOffsetFromNorm(Random.nextFloat())
        twoLinks.setLinkTwoOffsetFromNorm(Random.nextFloat())
        twoLinks.setPivotFromNorm(Random.nextFloat())
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