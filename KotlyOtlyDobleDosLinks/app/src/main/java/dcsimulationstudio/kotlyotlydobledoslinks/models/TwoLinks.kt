package dcsimulationstudio.kotlyotlydobledoslinks.models

import com.google.android.filament.utils.Float3
import dev.romainguy.kotlin.math.Float4
import dev.romainguy.kotlin.math.pow
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

fun rk4(equation: (Float, Float4) -> Float4, t: Float, x: Float4, h: Float): Float4 {
    // Apply the 4th order Runge-Kutta integration routine
    val k1 = equation(t, x)
    val k2 = equation(t, rk4xPlusHK(x, 0.5f*h, k1))  // x + 0.5 * h * k1)
    val k3 = equation(t, rk4xPlusHK(x, 0.5f*h, k2))  // x + 0.5 * h * k2)
    val k4 = equation(t, rk4xPlusHK(x, h, k3))  // x + h * k3)
    //return x + 1.0/6.0 * h * (k1 + 2.0*k2 + 2.0*k3 + k4)
    val oneSixth = 1.0f / 6.0f
    return Float4(
        x[0] + oneSixth * h * (k1[0] + 2.0f*k2[0] + 2.0f*k3[0] + k4[0]),
        x[1] + oneSixth * h * (k1[1] + 2.0f*k2[1] + 2.0f*k3[1] + k4[1]),
        x[2] + oneSixth * h * (k1[2] + 2.0f*k2[2] + 2.0f*k3[2] + k4[2]),
        x[3] + oneSixth * h * (k1[3] + 2.0f*k2[3] + 2.0f*k3[3] + k4[3]),
    )
}

fun rk4xPlusHK(x: Float4, h: Float, k: Float4): Float4 {
    return Float4(
        x[0] + h * k[0],
        x[1] + h * k[1],
        x[2] + h * k[2],
        x[3] + h * k[3]
    )
}

fun invert2x2(matrix: Array<FloatArray>): Array<FloatArray> {
    val a = matrix[0][0]
    val b = matrix[0][1]
    val c = matrix[1][0]
    val d = matrix[1][1]
    val det = 1.0f / (a*d - b*c)
    return arrayOf(
        floatArrayOf(d * det, -b * det),
        floatArrayOf(-c * det, a * det)
    )
}

class TwoLinks {
    var length = floatArrayOf(0.28f, 0.23f)
    var offset = floatArrayOf(0.125f, 0.1f)
    var pivot = 0.11f

    var height = floatArrayOf(0.05f, 0.05f)
    var thickness = floatArrayOf(0.0064f, 0.0064f)

    var density = floatArrayOf(800.0f, 800.0f)
    //var dampingRatio = 0.0f
    var θ = floatArrayOf(0.0f, 0.0f)
    var ω = floatArrayOf(0.0f, 0.0f)

    private var dt = 1.0f / 60.0f
    private val grav = 1.62f  //9.8f

    private val minLength = 0.12f
    private val maxLength = 0.53f
    private val minDistanceFromEdge = 0.03f

    //private val manager = CMMotionManager()

    private var _mass: FloatArray? = null
    private var _moi: FloatArray? = null
    private var _m11: Float? = null
    private var _m22: Float? = null

    val position: Array<Float3>
        get() = arrayOf(
                Float3(
                    offset[0] * cos(θ[0]),
                    offset[0] * sin(θ[0]),
                    thickness[0]
            ),
                Float3(
                    pivot * cos(θ[0]) + offset[1] * cos(θ[1]),
                    pivot * sin(θ[0]) + offset[1] * sin(θ[1]),
                    thickness[0] + thickness[1]
                )
            )

    val pivotPosition: Float3
        get() = Float3(
                pivot * cos(θ[0]),
                pivot * sin(θ[0]),
                thickness[0] + 0.5f * thickness[1]
            )

    val orientation: Array<Float4>
        get() = arrayOf(
                Float4(0f, 0f, sin(0.5f * θ[0]), cos(0.5f * θ[0])),
                Float4(0f, 0f, sin(0.5f * θ[1]), cos(0.5f * θ[1]))
            )

    val mass: FloatArray
        get() = _mass ?: floatArrayOf(
                density[0] * length[0] * height[0] * thickness[0],
                density[1] * length[1] * height[1] * thickness[1]
            )

    val moi: FloatArray
        get() = _moi ?: floatArrayOf(
                1.0f/12.0f * mass[0] * (pow(length[0], 2f) + pow(height[0], 2f)),
                1.0f/12.0f * mass[1] * (pow(length[1], 2f) + pow(height[1], 2f))
            )

    val m11: Float
        get() = _m11 ?: (moi[0] + mass[0] * pow(offset[0], 2f) + mass[1] * pow(pivot, 2f))

    val m22: Float
        get() = _m22 ?: (moi[1] + mass[1] * pow(offset[1], 2f))

    fun m12(x: Float4): Float {
        return mass[1] * pivot * offset[1] * cos(x[0] - x[1])
    }

    fun massMatrix(x: Float4): Array<FloatArray> {
        val _m12 = m12(x)
        return arrayOf(
            floatArrayOf(m11, _m12),
            floatArrayOf(_m12, m22)
        )
    }

    fun forcing(x: Float4): FloatArray {
        val gx = 0.0f  // grav * (manager.accelerometerData?.acceleration.x ?? 0.0)
        val gy = -grav * 1.0f  // grav * (manager.accelerometerData?.acceleration.y ?? -1.0)
        val a = pivot
        val b = offset[0]
        val c = offset[1]
        val m0 = mass[0]
        val m1 = mass[1]
        val θ0 = x[0]
        val θ1 = x[1]
        val ω0 = x[2]
        val ω1 = x[3]
        return floatArrayOf(
            -a*c*m1*pow(ω1, 2f)*sin(θ0 - θ1) - a*gx*m1*sin(θ0) + a*gy*m1*cos(θ0) - b*gx*m0*sin(θ0) + b*gy*m0*cos(θ0),
             c*m1*(a*pow(ω0, 2f)*sin(θ0 - θ1) - gx*sin(θ1) + gy*cos(θ1))
        )
    }

    fun equationOfMotion(t: Float, x: Float4): Float4 {
        // Calculate the angular velocity and acceleration states given angle and angular velocity
        val invM = invert2x2(massMatrix(x))
        val f = forcing(x)
        val dx = floatArrayOf(
            invM[0][0]*f[0] + invM[0][1]*f[1],
            invM[1][0]*f[0] + invM[1][1]*f[1]
        )
        return Float4(x[2], x[3], dx[0], dx[1])
    }

    fun update() {
        // Calculate states at the next frame
        val priorState = Float4(θ[0], θ[1], ω[0], ω[1])
        val newState = rk4({t, x -> equationOfMotion(t, x)}, 0.0f, priorState, dt)
        θ = floatArrayOf(newState[0], newState[1])
        ω = floatArrayOf(newState[2], newState[3])
    }

    fun nullify() {
        // When a specified value changes, this makes sure the
        // properties below get re-calculated on next update
        _mass = null
        _moi = null
        _m11 = null
        _m22 = null
    }

    val linkOneLengthNorm: Float
        get() = (length[0] - minLength) / (maxLength - minLength)

    fun setLinkOneLengthFromNorm(value: Float) {
        // Get the norm values before adjusting, to avoid recursion
        val offsetNorm = linkOneOffsetNorm

        // Adjust the physical distances
        length[0] = minLength + value * (maxLength - minLength)
        //_linkOneGeometry.width = length[0]
        offset[0] = (1.0f - offsetNorm) * (0.5f * length[0] - minDistanceFromEdge)
        pivot = min(pivot, maxPivot)

        // Call this to make sure the mass properties get re-calculated
        nullify()
    }

    val linkOneOffsetNorm: Float
        get() {
            val n = 1.0f - offset[0] / (0.5f * length[0] - minDistanceFromEdge)
            return min(1.0f, max(0.0f, n))
        }

    fun setLinkOneOffsetFromNorm(n: Float) {
        offset[0] = (1.0f - n) * (0.5f * length[0] - minDistanceFromEdge)
        pivot = min(pivot, maxPivot)

        // Call this to make sure the mass properties get re-calculated
        nullify()
    }

    val maxPivot: Float
        get() = 0.5f * length[0] - minDistanceFromEdge + offset[0]

    val pivotNorm: Float
        get() = min(1.0f, max(0.0f, pivot / maxPivot))

    fun setPivotFromNorm(m: Float) {
        pivot = m * maxPivot

        // Call this to make sure the mass properties get re-calculated
        nullify()
    }

    val linkTwoLengthNorm: Float
        get() = (length[1] - minLength) / (maxLength - minLength)

    fun setLinkTwoLengthFromNorm(n: Float) {
        // Get the norm values before adjusting, to avoid recursion
        val offsetNorm = linkTwoOffsetNorm

        // Adjust the physical distances
        length[1] = minLength + n * (maxLength - minLength)
        //_linkTwoGeometry.width = length[1]
        offset[1] = (1.0f - offsetNorm) * (0.5f * length[1] - minDistanceFromEdge)

        // Call this to make sure the mass properties get re-calculated
        nullify()
    }

    val linkTwoOffsetNorm: Float
        get() {
            val n = 1.0f - offset[1] / (0.5f * length[1] - minDistanceFromEdge)
            return min(1.0f, max(0.0f, n))
        }

    fun setLinkTwoOffsetFromNorm(n: Float) {
        offset[1] = (1.0f - n) * (0.5f * length[1] - minDistanceFromEdge)

        // Call this to make sure the mass properties get re-calculated
        nullify()
    }
}