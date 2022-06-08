package dcsimulationstudio.kotlyotlydobledoslinks.models

import com.google.android.filament.utils.Float3
import dev.romainguy.kotlin.math.Float4
import dev.romainguy.kotlin.math.pow
import kotlin.math.cos
import kotlin.math.sin

/*
func rk4(equation: (Double, simd_double4) -> simd_double4, t: Double, x: simd_double4, h: Double) -> simd_double4 {
    // Apply the 4th order Runge-Kutta integration routine
    let k1 = equation(t, x)
    let k2 = equation(t, x + 0.5 * h * k1)
    let k3 = equation(t, x + 0.5 * h * k2)
    let k4 = equation(t, x + h * k3)
    return x + 1.0/6.0 * h * (k1 + 2.0*k2 + 2.0*k3 + k4)
}
 */

class TwoLinks {
    var length = floatArrayOf(0.28f, 0.23f)
    var offset = floatArrayOf(0.125f, 0.1f)
    var pivot = 0.11f

    var height = floatArrayOf(0.05f, 0.05f)
    var thickness = floatArrayOf(0.0064f, 0.0064f)

    var density = floatArrayOf(800.0f, 800.0f)
    var dampingRatio = 0.0f
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
        get() {
            return arrayOf(
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
        }

    val pivotPosition: Float3
        get() {
            return Float3(
                pivot * cos(θ[0]),
                pivot * sin(θ[0]),
                thickness[0] + 0.5f * thickness[1]
            )
        }

    val orientation: Array<Float4>
        get() {
            return arrayOf(
                Float4(0f, 0f, sin(0.5f * θ[0]), cos(0.5f * θ[0])),
                Float4(0f, 0f, sin(0.5f * θ[1]), cos(0.5f * θ[1]))
            )
        }

    val mass: FloatArray
        get() {
            return _mass ?: floatArrayOf(
                density[0] * length[0] * height[0] * thickness[0],
                density[1] * length[1] * height[1] * thickness[1]
            )
        }

    val moi: FloatArray
        get() {
            return _moi ?: floatArrayOf(
                1.0f/12.0f * mass[0] * (pow(length[0], 2f) + pow(height[0], 2f)),
                1.0f/12.0f * mass[1] * (pow(length[1], 2f) + pow(height[1], 2f))
            )
        }

    val m11: Float
        get() {
            return _m11 ?: (moi[0] + mass[0] * pow(offset[0], 2f) + mass[1] * pow(pivot, 2f))
        }

    val m22: Float
        get() {
            return _m22 ?: (moi[1] + mass[1] * pow(offset[1], 2f))
        }

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

    /*
    func forcing(x: simd_double4) -> simd_double2 {
        let g_x = grav * (manager.accelerometerData?.acceleration.x ?? 0.0)
        let g_y = grav * (manager.accelerometerData?.acceleration.y ?? -1.0)
        let a = pivot
        let b = offset[0]
        let c = offset[1]
        let m_0 = mass[0]
        let m_1 = mass[1]
        let θ0 = x[0]
        let θ1 = x[1]
        let ω0 = x[2]
        let ω1 = x[3]
        return simd_double2(
            -a*c*m_1*pow(ω1, 2)*sin(θ0 - θ1) - a*g_x*m_1*sin(θ0) + a*g_y*m_1*cos(θ0) - b*g_x*m_0*sin(θ0) + b*g_y*m_0*cos(θ0),
             c*m_1*(a*pow(ω0, 2)*sin(θ0 - θ1) - g_x*sin(θ1) + g_y*cos(θ1))
        )
    }

    func equationOfMotion(t: Double, x: simd_double4) -> simd_double4 {
        // Calculate the angular velocity and acceleration states given angle and angular velocity
        let invM = massMatrix(x: x).inverse
        let F = forcing(x: x)
        let dx = simd_mul(invM, F)
        return simd_double4(x[2], x[3], dx[0], dx[1])
    }

    func update() {
        // Calculate states at the next frame
        let priorState = simd_double4(θ[0], θ[1], ω[0], ω[1])
        let newState = rk4(equation: equationOfMotion, t: 0.0, x: priorState, h: dt)
        θ = [newState[0], newState[1]]
        ω = [newState[2], newState[3]]
    }

    func nilify() {
        // When a specified value changes, this makes sure the
        // properties below get re-calculated on next update
        _mass = nil
        _moi = nil
        _m11 = nil
        _m22 = nil
    }

    var linkOneLengthNorm: Double {
        get {
            return (length[0] - minLength) / (maxLength - minLength)
        }
    }

    func setLinkOneLengthFromNorm(value: Double) {
        // Get the norm values before adjusting, to avoid recursion
        let offsetNorm = linkOneOffsetNorm

        // Adjust the physical distances
        length[0] = minLength + value * (maxLength - minLength)
        _linkOneGeometry.width = length[0]
        offset[0] = (1.0 - offsetNorm) * (0.5 * length[0] - minDistanceFromEdge)
        pivot = min(pivot, maxPivot)

        // Call this to make sure the mass properties get re-calculated
        nilify()
    }

    var linkOneOffsetNorm: Double {
        get {
            let n = 1.0 - offset[0] / (0.5 * length[0] - minDistanceFromEdge)
            return min(1.0, max(0.0, n))
        }
    }

    func setLinkOneOffsetFromNorm(n: Double) {
        offset[0] = (1.0 - n) * (0.5 * length[0] - minDistanceFromEdge)
        pivot = min(pivot, maxPivot)

        // Call this to make sure the mass properties get re-calculated
        nilify()
    }

    var maxPivot: Double {
        get {
            return 0.5 * length[0] - minDistanceFromEdge + offset[0]
        }
    }

    var pivotNorm: Double {
        get {
            return min(1.0, max(0.0, pivot / maxPivot))
        }
    }

    func setPivotFromNorm(m: Double) {
        pivot = m * maxPivot

        // Call this to make sure the mass properties get re-calculated
        nilify()
    }

    var linkTwoLengthNorm: Double {
        get {
            return (length[1] - minLength) / (maxLength - minLength)
        }
    }

    func setLinkTwoLengthFromNorm(n: Double) {
        // Get the norm values before adjusting, to avoid recursion
        let offsetNorm = linkTwoOffsetNorm

        // Adjust the physical distances
        length[1] = minLength + n * (maxLength - minLength)
        _linkTwoGeometry.width = length[1]
        offset[1] = (1.0 - offsetNorm) * (0.5 * length[1] - minDistanceFromEdge)

        // Call this to make sure the mass properties get re-calculated
        nilify()
    }

    var linkTwoOffsetNorm: Double {
        get {
            let n = 1.0 - offset[1] / (0.5 * length[1] - minDistanceFromEdge)
            return min(1.0, max(0.0, n))
        }
    }

    func setLinkTwoOffsetFromNorm(n: Double) {
        offset[1] = (1.0 - n) * (0.5 * length[1] - minDistanceFromEdge)

        // Call this to make sure the mass properties get re-calculated
        nilify()
    }
     */
}