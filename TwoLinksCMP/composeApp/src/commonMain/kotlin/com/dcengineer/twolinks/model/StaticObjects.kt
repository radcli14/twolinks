package com.dcengineer.twolinks.model

import dev.romainguy.kotlin.math.Float3
import dev.romainguy.kotlin.math.Float4

object StaticObjects {
    object Door {
        val width = 0.91f
        val height = 2.03f
        val thickness = 0.035f
        val offset = 0.0628f  // In AR mode, the distance from a detected surface to the bottom of the door
        val color = Float4(0.69f, 0.69f, 0.69f, 1f)
        val roughness = 0.05f
        val metallic = 0.97f
        val reflectance = 1f
    }

    object Pivot {
        val radius = 0.01f
        val height = 0.015f
        val color = Float4(0.75f, 0.75f, 0.75f, 1f)
        val roughness = 0.157f
        val metallic = 1f
        val reflectance = 0.157f
    }
}

val StaticObjects.Door.size: Float3
    get() = Float3(width, height, thickness)
