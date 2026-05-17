//
//  Kotlin_math.swift
//  iosApp
//
//  Created by Eliott Radcliffe on 5/17/26.
//

import ComposeApp
import simd
import UIKit

extension Kotlin_mathFloat3 {
    var asSIMD3: SIMD3<Float> {
        .init(x, y, z)
    }
    
    /// Assuming this Kotlin `Float3` represents a XYZ rotation sequence with states in degrees, provide the corresponding quaternion
    var asQuatf: simd_quatf {
        .forXYZRotationInDegrees(x, y, z)
    }
}

extension Kotlin_mathFloat4 {
    var asUIColor: UIColor {
        UIColor(red: CGFloat(x), green: CGFloat(y), blue: CGFloat(z), alpha: CGFloat(w))
    }
}
