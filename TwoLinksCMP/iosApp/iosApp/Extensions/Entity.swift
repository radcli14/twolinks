//
//  Entity.swift
//  iosApp
//
//  Created by Eliott Radcliffe on 5/17/26.
//

import Foundation
import RealityKit

extension Entity {
    func fadeOpacity(from initialOpacity: Float, to finalOpacity: Float, over duration: TimeInterval) {
        guard let animation = opacityAnimationResource(from: initialOpacity, to: finalOpacity, over: duration) else { return }
        playAnimation(animation)
    }
    
    private func opacityAnimationResource(from initialOpacity: Float, to finalOpacity: Float, over duration: TimeInterval) -> AnimationResource? {
        try? AnimationResource.generate(with: opacityAnimationDefinition(from: initialOpacity, to: finalOpacity, over: duration))
    }
    
    private func opacityAnimationDefinition(from initialOpacity: Float, to finalOpacity: Float, over duration: TimeInterval) -> AnimationDefinition {
        FromToByAnimation<Float>(
            from: initialOpacity,
            to: finalOpacity,
            duration: duration,
            bindTarget: .opacity
        )
    }
}
