package com.dcengineer.twolinks.model

import com.dcengineer.twolinks.functions.rad2deg
import dev.romainguy.kotlin.math.Float3
import dev.romainguy.kotlin.math.Float4
import kotlin.collections.get
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sin

/**
 * The data model responsible for managing a single link's physical properties
 */
data class Link(
    var length: Float,
    var offset: Float,
    var height: Float = 0.05f,
    var thickness: Float = 0.0064f,
    var density: Float = 800f,
    var color: Float4 = Float4(1f, 0f, 0f, 1f),
    //var theta: Float = 0f,
    //var omega: Float = 0f
) {
    internal val minLength = 0.12f
    internal val maxLength = 0.53f
    internal val minDistanceFromEdge = 0.03f

    internal var _mass: Float? = null
    internal var _moi: Float? = null
    internal var _moiRelOffset: Float? = null

    companion object {
        val first: Link
            get() = Link(length = 0.28f, offset = 0.125f)

        val second: Link
            get() = Link(length = 0.23f, offset = 0.1f)
    }

}

val Link.size: Float3
    get() = Float3(length, height, thickness)

val Link.mass: Float
    get() = _mass ?: (density * length * height * thickness)

val Link.moi: Float
    get() = _moi ?: (1.0f / 12.0f * mass * (length.pow(2f) + height.pow(2f)))

val Link.moiRelOffset: Float
    get() = _moiRelOffset ?: (mass * offset.pow(2f))

val Link.lengthNorm: Float get() = (length - minLength) / (maxLength - minLength)

val Link.offsetNorm: Float
    get() {
        val n = 1.0f - offset / (0.5f * length - minDistanceFromEdge)
        return min(1.0f, max(0.0f, n))
    }

val Link.maxPivot: Float
    get() = 0.5f * length - minDistanceFromEdge + offset

val Link.center: Float3
    get() = Float3(offset, 0f, 0.5f * thickness)

fun Link.setLengthFromNorm(n: Float) {
    // Get the norm values before adjusting, to avoid recursion
    val offsetNorm = offsetNorm

    // Adjust the physical distances
    length = minLength + n * (maxLength - minLength)
    offset = (1.0f - offsetNorm) * (0.5f * length - minDistanceFromEdge)
}

fun Link.setOffsetFromNorm(n: Float) {
    offset = (1.0f - n) * (0.5f * length - minDistanceFromEdge)
}

fun Link.nullify() {
    _mass = null
    _moi = null
}