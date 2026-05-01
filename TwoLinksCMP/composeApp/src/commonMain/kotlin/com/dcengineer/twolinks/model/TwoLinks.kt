package com.dcengineer.twolinks.model

import com.dcengineer.twolinks.functions.invert2x2
import com.dcengineer.twolinks.functions.rk4
import dev.romainguy.kotlin.math.Float3
import dev.romainguy.kotlin.math.Float4
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sin

typealias Position = Float3

data class TwoLinks(
    var links: Array<Link> = arrayOf(Link.first, Link.second)
) {
    var pivot = 0.11f

    private var dt = 1.0f / 60.0f
    private val grav = 1.62f  //9.8f

    private var _m11: Float? = null
    private var _m22: Float? = null

    val pivotPosition: Float3
        get() = Float3(
                pivot * cos(links[0].theta),
                pivot * sin(links[0].theta),
                links[0].thickness + 0.5f * links[1].thickness
            )

    private val m11: Float
        get() = links[0].moi + links[0].moiRelOffset // _m11 ?: (moi[0] + mass[0] * offset[0].pow(2f) + mass[1] * pivot.pow(2f))

    private val m22: Float
        get() = links[1].moi + links[1].moiRelOffset //_m22 ?: (moi[1] + mass[1] * links[1].offset.pow(2f))

    private fun m12(x: Float4): Float {
        return links[1].mass * pivot * links[1].offset * cos(x[0] - x[1])
    }

    /**
     * Left hand side of the equation of motion is this times the angular accelerations
     */
    private fun massMatrix(x: Float4): Array<FloatArray> {
        val m12 = m12(x)
        return arrayOf(
            floatArrayOf(m11, m12),
            floatArrayOf(m12, m22)
        )
    }

    /**
     * Right hand side of the equation of motion
     */
    private fun forcing(x: Float4): FloatArray {
        val gx = 0.0f
        val gy = -grav * 1.0f
        return floatArrayOf(
            -pivot*links[1].offset*links[1].mass*x[3].pow(2f)*sin(x[0] - x[1])
                    - pivot*gx*links[1].mass*sin(x[0])
                    + pivot*gy*links[1].mass*cos(x[0])
                    - links[0].offset*gx*links[0].mass*sin(x[0])
                    + links[0].offset*gy*links[0].mass*cos(x[0]),
            links[1].offset*links[1].mass*(pivot*x[2].pow(2f)*sin(x[0] - x[1])
                    - gx*sin(x[1]) + gy*cos(x[1]))
        )
    }

    /**
     * Calculate the angular velocity and acceleration states given angle and angular velocity
     */
    private fun equationOfMotion(x: Float4): Float4 {
        // Inverse of the 2x2 mass matrix, left hand side of equation
        val invM = invert2x2(massMatrix(x))

        // Forcing vector, right hand side of the equation
        val f = forcing(x)

        // Derivatives of the angles and angular rates
        val dx = floatArrayOf(
            invM[0][0]*f[0] + invM[0][1]*f[1],
            invM[1][0]*f[0] + invM[1][1]*f[1]
        )

        // Derivatives of the state vector in 1st order form
        return Float4(x[2], x[3], dx[0], dx[1])
    }

    /**
     * Simulate a single time step, and update the theta and omega state variables
     */
    fun update(h: Float = dt) {
        // Calculate states at the next frame based on current states
        val priorState = Float4(links[0].theta, links[1].theta, links[0].omega, links[1].omega)
        val newState = rk4({x -> equationOfMotion(x)}, priorState, h)

        // Update the state variables
        links[0].updateState(newTheta = newState[0], newOmega = newState[2])
        links[1].updateState(newTheta = newState[1], newOmega = newState[3])
    }

    /**
     * When a specified value changes, this makes sure the properties below get re-calculated on next update
     */
    private fun nullify() {
        links[0].nullify()
        links[1].nullify()
        _m11 = null
        _m22 = null
    }

    /**
     * Given a normalized length (0f to 1f) update the link one length, offset, and pivot
     */
    fun setLinkOneLengthFromNorm(n: Float) {
        links[0].setLengthFromNorm(n)
        pivot = min(pivot, links[0].maxPivot)

        // Call this to make sure the mass properties get re-calculated
        nullify()
    }

    /**
     * Given a normalized offset (0f to 1f) update the link one offset
     */
    fun setLinkOneOffsetFromNorm(n: Float) {
        links[0].offset = (1.0f - n) * (0.5f * links[0].length - links[0].minDistanceFromEdge)
        pivot = min(pivot, links[0].maxPivot)

        // Call this to make sure the mass properties get re-calculated
        nullify()
    }

    val pivotNorm: Float
        get() = min(1.0f, max(0.0f, pivot / links[0].maxPivot))

    /**
     * Given a normalized pivot (0f to 1f) update the pivot location
     */
    fun setPivotFromNorm(m: Float) {
        pivot = m * links[0].maxPivot

        // Call this to make sure the mass properties get re-calculated
        nullify()
    }

    /**
     * Given a normalized length (0f to 1f) update the link two length and offset
     */
    fun setLinkTwoLengthFromNorm(n: Float) {
        links[1].setLengthFromNorm(n)

        // Call this to make sure the mass properties get re-calculated
        nullify()
    }

    /**
     * Given a normalized offset (0f to 1f) update the link two offset
     */
    fun setLinkTwoOffsetFromNorm(n: Float) {
        links[1].setOffsetFromNorm(n)

        // Call this to make sure the mass properties get re-calculated
        nullify()
    }
}
