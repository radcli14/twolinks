//
//  TwoLinks.swift
//  SwiftyTwoLinks
//
//  Created by Eliott Radcliffe on 5/9/22.
//

import Foundation
import CoreMotion
import simd
import SceneKit

func rk4(equation: (Double, simd_double4) -> simd_double4, t: Double, x: simd_double4, h: Double) -> simd_double4 {
    // Apply the 4th order Runge-Kutta integration routine
    let k1 = equation(t, x)
    let k2 = equation(t, x + 0.5 * h * k1)
    let k3 = equation(t, x + 0.5 * h * k2)
    let k4 = equation(t, x + h * k3)
    return x + 1.0/6.0 * h * (k1 + 2.0*k2 + 2.0*k3 + k4)
}

class TwoLinks {
    var length = [0.28, 0.23]
    var offset = [0.125, 0.1]
    var pivot = 0.11
    
    var height = [0.05, 0.05]
    var thickness = [0.0064, 0.0064]

    var density = [800.0, 800.0]
    var θ = [0.0, 0.0]
    var ω = [0.0, 0.0]
                     
    private var dt = 1.0 / 60.0
    private let grav = 9.8
    
    private let minLength = 0.12
    private let maxLength = 0.53
    private let minDistanceFromEdge = 0.03
    
    private let manager = CMMotionManager()

    private var _mass: [Double]? = nil
    private var _moi: [Double]? = nil
    private var _m11: Double? = nil
    private var _m22: Double? = nil
  
    var position: [SCNVector3] {
        get {
            return [SCNVector3(
                        offset[0] * cos(θ[0]),
                        offset[0] * sin(θ[0]),
                        thickness[0]
                    ),
                    SCNVector3(
                        pivot * cos(θ[0]) + offset[1] * cos(θ[1]),
                        pivot * sin(θ[0]) + offset[1] * sin(θ[1]),
                        thickness[0] + thickness[1]
                    )]
        }
    }
    
    var pivotPosition: SCNVector3 {
        get {
            return SCNVector3(pivot * cos(θ[0]), pivot * sin(θ[0]), thickness[0] + 0.5 * thickness[1])
        }
    }
    
    var orientation: [SCNQuaternion] {
        get {
            return [SCNQuaternion(0, 0, sin(0.5 * θ[0]), cos(0.5 * θ[0])),
                    SCNQuaternion(0, 0, sin(0.5 * θ[1]), cos(0.5 * θ[1]))]
        }
    }
    
    private let _linkOneGeometry = SCNBox(
        width: 0.28,
        height: 0.05,
        length: 0.0064,
        chamferRadius: 0.01
    )
    private let _linkTwoGeometry = SCNBox(
        width: 0.23,
        height: 0.05,
        length: 0.0064,
        chamferRadius: 0.01
    )
    var geometry: [SCNBox] {
        get {
            _linkOneGeometry.materials.first?.diffuse.contents = UIColor.brown
            _linkTwoGeometry.materials.first?.diffuse.contents = UIColor.systemBrown
            return [_linkOneGeometry, _linkTwoGeometry]
        }
    }
    
    var mass: [Double] {
        get {
            return _mass ?? [density[0] * length[0] * height[0] * thickness[0],
                             density[1] * length[1] * height[1] * thickness[1]]
        }
    }
    
    var moi: [Double] {
        get {
            return _moi ?? [1.0/12.0 * mass[0] * (pow(length[0], 2) + pow(height[0], 2)),
                            1.0/12.0 * mass[1] * (pow(length[1], 2) + pow(height[1], 2))]
        }
    }
    
    var m11: Double {
        get {
            return _m11 ?? moi[0] + mass[0] * pow(offset[0], 2) + mass[1] * pow(pivot, 2)
        }
    }
    
    var m22: Double {
        get {
            return _m22 ?? moi[1] + mass[1] * pow(offset[1], 2)
        }
    }
                     
    init() {
        // Make sure the accelerometer readings can be taken
        manager.startAccelerometerUpdates()
    }
    
    func m12(x: simd_double4) -> Double {
        return mass[1] * pivot * offset[1] * cos(x[0] - x[1])
    }
    
    func massMatrix(x: simd_double4) -> simd_double2x2 {
        let _m12 = m12(x: x)
        return simd_double2x2([simd_double2(m11, _m12), simd_double2(_m12, m22)])
    }
    
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
            return 100.0 * (length[0] - minLength) / (maxLength - minLength)
        }
    }
    
    func setLinkOneLengthFromNorm(value: Double) {
        // Get the norm values before adjusting, to avoid recursion
        let offsetNorm = linkOneOffsetNorm

        // Adjust the physical distances
        length[0] = minLength + 0.01 * value * (maxLength - minLength)
        _linkOneGeometry.width = length[0]
        offset[0] = (1.0 - 0.01 * offsetNorm) * (0.5 * length[0] - minDistanceFromEdge)
        pivot = min(pivot, maxPivot)
        
        // Call this to make sure the mass properties get re-calculated
        nilify()
    }
    
    var linkOneOffsetNorm: Double {
        get {
            let n = 1.0 - offset[0] / (0.5 * length[0] - minDistanceFromEdge)
            return min(100.0, max(0.0, 100.0 * n))
        }
    }
    
    func setLinkOneOffsetFromNorm(value: Double) {
        let n = 0.01 * value
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
            let m = pivot / maxPivot
            return min(100.0, max(0.0, 100.0 * m))
        }
    }
        
    func setPivotFromNorm(value: Double) {
        let m = 0.01 * value
        pivot = m * maxPivot
        
        // Call this to make sure the mass properties get re-calculated
        nilify()
    }
    
    var linkTwoLengthNorm: Double {
        get {
            return 100.0 * (length[1] - minLength) / (maxLength - minLength)
        }
    }
    
    func setLinkTwoLengthFromNorm(value: Double) {
        // Get the norm values before adjusting, to avoid recursion
        let offsetNorm = linkTwoOffsetNorm

        // Adjust the physical distances
        length[1] = minLength + 0.01 * value * (maxLength - minLength)
        _linkTwoGeometry.width = length[1]
        offset[1] = (1.0 - 0.01 * offsetNorm) * (0.5 * length[1] - minDistanceFromEdge)

        // Call this to make sure the mass properties get re-calculated
        nilify()
    }
    
    var linkTwoOffsetNorm: Double {
        get {
            let n = 1.0 - offset[1] / (0.5 * length[1] - minDistanceFromEdge)
            return min(100.0, max(0.0, 100.0 * n))
        }
    }
    
    func setLinkTwoOffsetFromNorm(value: Double) {
        let n = 0.01 * value
        offset[1] = (1.0 - n) * (0.5 * length[1] - minDistanceFromEdge)

        // Call this to make sure the mass properties get re-calculated
        nilify()
    }
}
