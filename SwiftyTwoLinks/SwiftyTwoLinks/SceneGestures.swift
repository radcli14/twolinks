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


//var xDrag = 0.0
var yDrag = Float(0)
var lastYdrag: Float? = nil

/**
 From the content view, if the user drags the view will rotate around the double pendulum
 */
func onDragToRotate(_ viewController: ContentViewController) -> _EndedGesture<_ChangedGesture<DragGesture>> {
    let drag = DragGesture()
        .onChanged { gesture in

            // If this is the first touch, initialize the prior vertical position variable
            if (lastYdrag == nil) {
                lastYdrag = Float(gesture.translation.height)
            }
            
            // From the position of the camera prior to initiating the drag, get the radius
            // in the horizontal plane, `xzDist`, and the `radius` in spherical coords.
            let position = viewController.cameraPosBefore
            let xzDist = sqrt(position.x*position.x + position.z*position.z)

            // Calculate the azimuthal and elevation angle sines and cosines
            let azSin = position.x / xzDist
            let azCos = position.z / xzDist

            // The two error functions, verticalErf and moonErf, will downscale the amount of yDrag distance if the camera is too high or low
            // Determine if the camera is located above the door.
            let cameraPosition = viewController.cameraNode.position
            var verticalErf = erfc(cameraPosition.y / cameraPosition.length - 0.9)
            if (verticalErf.isNaN) {
                verticalErf = 1.0
            }
            
            // Determine distance from the moon, and whether the camera is touching it
            let moonPosition = viewController.moon.node.position
            let moonX = cameraPosition.x - moonPosition.x
            let moonY = cameraPosition.y - moonPosition.y
            let moonZ = cameraPosition.z - moonPosition.z
            let moonDistance = sqrt(moonX*moonX + moonY*moonY + moonZ*moonZ)
            let distFromSurface = moonDistance - Float(viewController.moon.geometry.radius)
            var moonErf = erfc(0.5 - distFromSurface)
            if (moonErf.isNaN) {
                moonErf = 1.0
            }
            
            // Determine the total number of pixels that the user has dragged, relate it to an angle that the camera will traverse. If camera is either touching the moon, or too close to vertical, then set the yDrag to zero.
            let xDrag = Float(gesture.translation.width)
            let dy = Float(gesture.translation.height) - (lastYdrag ?? Float(0))
            if (moonErf < 1.0) {
                yDrag += max(moonErf * dy, 0.0)
            } else if (verticalErf < 1.0) {
                yDrag += min(verticalErf * dy, 0.0)
            } else {
                yDrag += dy
            }
            let nPixels = sqrt(xDrag*xDrag + yDrag*yDrag)
            let angle = 0.00314 * nPixels

            // The axis of rotation is set in accordance to how much distance the user has
            // dragged in the left-right axis `xDrag`, which will result in rotation about
            // the local up-axis, and the up-down axis `yDrag` which will result in rotation
            // about the local horizontal axis.
            let axis = -simd_float3(
                yDrag*azCos,
                xDrag,
                -yDrag*azSin
            )
            
            // Pan the camera around the target
            viewController.cameraNode.position = viewController
                .cameraPosBefore
                .rotatedVector(
                    axis: axis,
                    angle: angle
                )

            // Set the last position where the user dragged in vertical direction
            lastYdrag = Float(gesture.translation.height)
        }
        .onEnded { _ in
            viewController.cameraPosBefore = viewController.cameraNode.position
            yDrag = Float(0)
            lastYdrag = nil
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
