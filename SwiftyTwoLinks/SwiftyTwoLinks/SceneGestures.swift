//
//  SceneGestures.swift
//  SwiftyTwoLinks
//
//  Created by Eliott Radcliffe on 5/31/22.
//

import Foundation
import SwiftUI
import SceneKit

/**
 From the content view, if the user taps while controls are visible, this gesture removes them
 */
func onTap(_ removeControls:@escaping () -> Void) -> _EndedGesture<TapGesture> {
    let tap = TapGesture()
        .onEnded { _ in
            withAnimation {
                removeControls()
            }
        }
    return tap
}

// These variables are used from within the DragGesture to calculate the extent of rotation
var xDrag: Float = 0.0
var yDrag: Float = 0.0
var nPixels: Float = 0.0

/**
 From the content view, if the user drags the view will rotate around the double pendulum
 */
func onDragToRotate(_ viewController: ContentViewController) -> _EndedGesture<_ChangedGesture<DragGesture>> {
    let drag = DragGesture()
        .onChanged { gesture in
            // Determine the total number of pixels that the user has dragged, relate it to an angle that the camera will traverse
            let xDrag = 0.9*xDrag + 0.1*Float(gesture.translation.width)
            let yDrag = 0.9*yDrag + 0.1*Float(gesture.translation.height)
            nPixels = sqrt(xDrag*xDrag + yDrag*yDrag)
            let angle = 0.025 * nPixels
            
            // Pan the camera around the target
            viewController.cameraNode.position = viewController
                .cameraPosBefore
                .rotatedVector(
                    axis: simd_float3(-yDrag, -xDrag, 0),
                    angle: angle
                )
        }
        .onEnded { _ in
            viewController.cameraPosBefore = viewController.cameraNode.position
            nPixels = 0
            xDrag = 0
            yDrag = 0
        }
    
    return drag
}

/**
 From the content view, if the user pinches, this zooms in or out
 */
func onMagnify(_ viewController: ContentViewController) -> _EndedGesture<_ChangedGesture<MagnificationGesture>> {
    let magnify = MagnificationGesture()
        .onChanged { gesture in
            viewController.cameraNode.position = viewController
                .cameraPosBefore.scaledBy(factor: Float(1.0 / gesture.magnitude))
        }
        .onEnded { _ in
            viewController.cameraPosBefore = viewController.cameraNode.position
        }
    
    return magnify
}
