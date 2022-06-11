package dcsimulationstudio.kotlyotlydobledoslinks

import androidx.lifecycle.ViewModel
import dcsimulationstudio.kotlyotlydobledoslinks.models.TwoLinks

class MainViewModel : ViewModel() {

    val twoLinks = TwoLinks()
    var isPaused = false

    var linkOnePosition = twoLinks.position[0]
    var linkTwoPosition = twoLinks.position[1]
    var linkOneAngle = 0.0f
    var linkTwoAngle = 0.0f

    fun resetStates() {
        twoLinks.θ = floatArrayOf(0.0f, 0.0f)
        twoLinks.ω = floatArrayOf(0.0f, 0.0f)
    }

    fun pause() {
        isPaused = !isPaused
    }

    fun update() {
        twoLinks.update()
        val pos = twoLinks.position
        linkOnePosition = pos[0]
        linkTwoPosition = pos[1]
        linkOneAngle = twoLinks.θ[0] * 180f / Math.PI.toFloat()
        linkTwoAngle = twoLinks.θ[1] * 180f / Math.PI.toFloat()
    }
}