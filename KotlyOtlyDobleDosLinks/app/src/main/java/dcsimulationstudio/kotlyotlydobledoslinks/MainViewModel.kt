package dcsimulationstudio.kotlyotlydobledoslinks

import androidx.lifecycle.ViewModel
import dcsimulationstudio.kotlyotlydobledoslinks.models.TwoLinks

class MainViewModel : ViewModel() {

    val twoLinks = TwoLinks()
    var isPaused = false

    fun resetStates() {
        twoLinks.θ = floatArrayOf(0.0f, 0.0f)
        twoLinks.ω = floatArrayOf(0.0f, 0.0f)
    }

    fun pause() {
        isPaused = !isPaused
    }
}