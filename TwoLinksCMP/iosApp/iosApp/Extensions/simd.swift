//
//  simd.swift
//  iosApp
//
//  Created by Eliott Radcliffe on 5/17/26.
//

import simd

extension simd_quatf {
    static func forXYZRotationInDegrees(_ x: Float, _ y: Float, _ z: Float) -> simd_quatf {
        simd_quatf(angle: x * .deg2rad, axis: [1, 0, 0]) *
        simd_quatf(angle: y * .deg2rad, axis: [0, 1, 0]) *
        simd_quatf(angle: z * .deg2rad, axis: [0, 0, 1])
    }
    
    static func forZRotationInDegrees(_ angle: Float) -> simd_quatf {
        .forZRotationInRadians(angle * .deg2rad)
    }
    
    static func forZRotationInRadians(_ angle: Float) -> simd_quatf {
        simd_quatf(angle: angle, axis: [0, 0, 1])
    }
}
