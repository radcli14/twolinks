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

/**
 From the content view, if the user drags the view will rotate around the double pendulum
 */
func onDragToRotate(_ viewController: ContentViewController) -> _EndedGesture<_ChangedGesture<DragGesture>> {
    let drag = DragGesture()
        .onChanged { gesture in
            // Determine the total number of pixels that the user has dragged, relate it to an angle that the camera will traverse
            let xDrag = Float(gesture.translation.width)
            let yDrag = Float(gesture.translation.height)
            let nPixels = sqrt(xDrag*xDrag + yDrag*yDrag)
            let angle = 0.00314 * nPixels
            
            // From the position of the camera prior to initiating the drag, get the radius
            // in the horizontal plane, `xzDist`, and the `radius` in spherical coords.
            let position = viewController.cameraPosBefore
            let xzDist = sqrt(position.x*position.x + position.z*position.z)
            let radius = position.length
            
            // Calculate the azimuthal and elevation angle sines and cosines
            let azSin = position.x / xzDist
            let azCos = position.z / xzDist
            let elSin = position.y / radius
            let elCos = xzDist / radius
            
            // The axis of rotation is set in accordance to how much distance the user has
            // dragged in the left-right axis `xDrag`, which will result in rotation about
            // the local up-axis, and the up-down axis `yDrag` which will result in rotation
            // about the local horizontal axis. The components are determined based on a
            // 2-component Euler sequence.
            let axis = -simd_float3(
                xDrag*azSin*elSin + yDrag*azCos,
                xDrag*elCos,
                xDrag*elSin*azCos - yDrag*azSin
            )

            // Pan the camera around the target
            viewController.cameraNode.position = viewController
                .cameraPosBefore
                .rotatedVector(
                    axis: axis,
                    angle: angle
                )
            
            /*
            let halfSine = 0.314 * sin(0.5 * angle)
            let quaternion = SCNQuaternion(
                halfSine * axis[0] / nPixels,
                halfSine * axis[1] / nPixels,
                halfSine * axis[2] / nPixels,
                cos(0.5 * angle)
            )
            viewController.cameraNode.rotate(
                by: quaternion,
                aroundTarget: viewController.originNode.position
            )
             */
        }
        .onEnded { _ in
            viewController.cameraPosBefore = viewController.cameraNode.position
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
