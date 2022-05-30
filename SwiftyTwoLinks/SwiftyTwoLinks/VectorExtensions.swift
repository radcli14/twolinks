//
//  VectorExtensions.swift
//  SwiftyTwoLinks
//
//  Created by Eliott Radcliffe on 5/30/22.
//

import Foundation
import SceneKit

extension SCNVector3 {
    var length: Float {
        get {
            return sqrt(x*x + y*y + z*z)
        }
    }
    
    func rotatedVector(axis: simd_float3, angle: Float) -> SCNVector3 {
        /// create quaternion with angle in radians and your axis
        let q = simd_quatf(angle: angle, axis: simd_normalize(axis))
        
        /// use ACT method of quaternion
        let simdVector = q.act(simd_float3(self))
        
        return SCNVector3(simdVector)
    }
}
