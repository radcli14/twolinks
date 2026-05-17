package com.dcengineer.twolinks.model

import com.dcengineer.twolinks.getPlatform
import dev.romainguy.kotlin.math.Float3
import dev.romainguy.kotlin.math.Float4

data class Planet(
    val name: String,
    val scale: Float = 1f,
    val position: Float3 = Float3(0f),
    val rotation: Float3 = Float3(0f), /* Degrees */
    val color: Float4 = Float4(1f)
) {
    private val fileExtension: String get() = if (getPlatform().name.startsWith("iOS")) "usdz" else "glb"
    val file: String get() = "$name.$fileExtension"

    companion object {
        private val moonScale = 27f
        private val earthScale = 100f

        val moon = Planet(
            name = "moon",
            scale = moonScale,
            position = Float3(0f, -0.5f * moonScale - 1.015f, 0f),
            rotation = Float3(69f, 0f, 0f),
            color = Float4(0.82f, 0.82f, 0.75f, 1f)
        )

        val earth = Planet(
            name = "earth",
            scale = earthScale,
            position = Float3(0.314f * earthScale, -0.157f * earthScale, -3.14f * earthScale),
            rotation = Float3(22f, 0f, 0f),
            color = Float4(0.576f, 0.803f, 0.965f, 1f)
        )

        val sun = Planet(
            name = "sun",
            scale = 700f,
            position = Float3(200f, 150f, 400f), 
            color = Float4(0.94f, 0.51f, 0.22f, 1f),
        )
    }
}