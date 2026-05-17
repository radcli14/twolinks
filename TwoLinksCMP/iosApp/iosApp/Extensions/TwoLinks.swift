//
//  TwoLinks.swift
//  iosApp
//
//  Created by Eliott Radcliffe on 5/17/26.
//

import ComposeApp
import simd

extension TwoLinks {
    var linkOneOrientation: simd_quatf {
        .forZRotationInRadians(-simulationState.x)
    }
    
    var linkTwoOrientation: simd_quatf {
        .forZRotationInRadians(simulationState.x - simulationState.y)
    }
}
