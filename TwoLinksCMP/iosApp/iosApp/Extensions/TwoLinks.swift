//
//  TwoLinks.swift
//  iosApp
//
//  Created by Eliott Radcliffe on 5/17/26.
//

import ComposeApp
import simd

extension TwoLinks {
    
    // MARK: - Link One
    
    var linkOneOrientation: simd_quatf {
        .forZRotationInRadians(-simulationState.x)
    }
    
    var linkOnePosition: SIMD3<Float> {
        -links[0].center.asSIMD3
    }
    
    var linkOneScale: SIMD3<Float> {
        links[0].size.asSIMD3
    }
    
    // MARK: - Link Two
    
    var linkTwoOrientation: simd_quatf {
        .forZRotationInRadians(simulationState.x - simulationState.y)
    }
    
    var linkTwoPosition: SIMD3<Float> {
        -links[1].center.asSIMD3
    }
    
    var linkTwoScale: SIMD3<Float> {
        links[1].size.asSIMD3
    }
}
