package com.dcengineer.twolinks.model

import dev.romainguy.kotlin.math.Float3

data class Planet(
    val file: String,
    val scale: Float = 1f,
    val position: Float3 = Float3(0f),
    val rotation: Float3 = Float3(0f) /* Degrees */
) {
    companion object {
        private val moonScale = 27f
        val moon = Planet(
            file = "moon.glb",
            scale = moonScale,
            position = Float3(0f, -0.5f * moonScale - 1.015f, 0f),
            rotation = Float3(69f, 0f, 0f)
        )
    }
}