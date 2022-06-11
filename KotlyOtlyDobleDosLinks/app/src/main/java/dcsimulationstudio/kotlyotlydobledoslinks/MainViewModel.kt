package dcsimulationstudio.kotlyotlydobledoslinks

import androidx.lifecycle.ViewModel
import dcsimulationstudio.kotlyotlydobledoslinks.models.TwoLinks
import io.github.sceneview.math.Rotation

class MainViewModel : ViewModel() {

    val twoLinks = TwoLinks()

    var isPaused = false

    var linkOnePosition = twoLinks.position[0]
    var linkTwoPosition = twoLinks.position[1]
    var linkOneRotation = Rotation(0.0f, 0.0f, 0.0f)
    var linkTwoRotation = Rotation(0.0f, 0.0f, 0.0f)

    /**
     * Reset the link angles and angular rates to zero
     */
    fun resetStates() {
        twoLinks.θ = floatArrayOf(0.0f, 0.0f)
        twoLinks.ω = floatArrayOf(0.0f, 0.0f)
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
        linkOneRotation.z = twoLinks.θ[0] * 180f / Math.PI.toFloat()
        linkTwoRotation.z = twoLinks.θ[1] * 180f / Math.PI.toFloat()
    }
}