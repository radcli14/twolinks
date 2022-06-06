package dcsimulationstudio.kotlyotlydobledoslinks

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Load the buttons that the user uses to control playback, or modify configuration
        val restartButton = findViewById(R.id.restart_button)
        val playButton = findViewById(R.id.play_button)
        val editButton = findViewById(R.id.edit_button)
        val colorButton = findViewById(R.id.color_button)
    }
}