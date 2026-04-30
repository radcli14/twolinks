package com.dcengineer.twolinks.functions

import dev.romainguy.kotlin.math.Float4


// Used in rk4, calculate once for efficiency
const val oneSixth = 1.0f / 6.0f

/**
 * Apply the 4th order Runge-Kutta integration routine for an equation that returns a Float4
 */
fun rk4(equation: (Float4) -> Float4, x: Float4, h: Float): Float4 {
    // Obtain the solution increments at partial time steps
    val k1 = equation(x)
    val k2 = equation(rk4xPlusHK(x, 0.5f*h, k1))
    val k3 = equation(rk4xPlusHK(x, 0.5f*h, k2))
    val k4 = equation(rk4xPlusHK(x, h, k3))

    // Calculate each state individually, and return as a Float4
    return Float4(
        x[0] + oneSixth * h * (k1[0] + 2.0f*k2[0] + 2.0f*k3[0] + k4[0]),
        x[1] + oneSixth * h * (k1[1] + 2.0f*k2[1] + 2.0f*k3[1] + k4[1]),
        x[2] + oneSixth * h * (k1[2] + 2.0f*k2[2] + 2.0f*k3[2] + k4[2]),
        x[3] + oneSixth * h * (k1[3] + 2.0f*k2[3] + 2.0f*k3[3] + k4[3]),
    )
}

/**
 * Utility function for the `rk4` implementation, to multiply the Float4 by a scalar
 */
fun rk4xPlusHK(x: Float4, h: Float, k: Float4): Float4 {
    return Float4(
        x[0] + h * k[0],
        x[1] + h * k[1],
        x[2] + h * k[2],
        x[3] + h * k[3]
    )
}

/**
 * Invert a 2x2 matrix as an array of float arrays
 */
fun invert2x2(matrix: Array<FloatArray>): Array<FloatArray> {
    // Get components of the matrix, short names
    val a = matrix[0][0]
    val b = matrix[0][1]
    val c = matrix[1][0]
    val d = matrix[1][1]

    // Determinant of the matrix
    val det = 1.0f / (a*d - b*c)

    // Inverse of the 2x2 matrix
    return arrayOf(
        floatArrayOf(d * det, -b * det),
        floatArrayOf(-c * det, a * det)
    )
}
