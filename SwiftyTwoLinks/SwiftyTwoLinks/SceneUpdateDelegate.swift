//
//  SceneUpdateDelegate.swift
//  SwiftyTwoLinks
//
//  Created by Eliott Radcliffe on 5/26/22.
//

import Foundation
import SceneKit

class SceneUpdateDelegate: NSObject, SCNSceneRendererDelegate {

    private let isPaused:  Bool
    private let twoLinks: TwoLinks
    private let linkOneNode: SCNNode
    private let pivotNode: SCNNode
    private let linkTwoNode: SCNNode
    
    init(viewController: ContentViewController) {
        isPaused = viewController.isPaused
        twoLinks = viewController.twoLinks
        linkOneNode = viewController.linkOneNode
        pivotNode = viewController.pivotNode
        linkTwoNode = viewController.linkTwoNode
    }
    
    func renderer(_ renderer: SCNSceneRenderer, updateAtTime time: TimeInterval) {
        // Update the link position and orientation
        if !isPaused {
            twoLinks.update()
        }
        linkOneNode.orientation = twoLinks.orientation[0]
        linkOneNode.position = twoLinks.position[0]
        pivotNode.position = twoLinks.pivotPosition
        linkTwoNode.orientation = twoLinks.orientation[1]
        linkTwoNode.position = twoLinks.position[1]
    }
}

/*
@objc
func handleTap(_ gestureRecognize: UIGestureRecognizer) {
    // retrieve the SCNView
    let scnView = self.view as! SCNView
    
    // check what nodes are tapped
    let p = gestureRecognize.location(in: scnView)
    let hitResults = scnView.hitTest(p, options: [:])
    // check that we clicked on at least one object
    if hitResults.count > 0 {
        // retrieved the first clicked object
        let result = hitResults[0]
        
        // get its material
        let material = result.node.geometry!.firstMaterial!
        
        // highlight it
        SCNTransaction.begin()
        SCNTransaction.animationDuration = 0.5
        
        // on completion - unhighlight
        SCNTransaction.completionBlock = {
            SCNTransaction.begin()
            SCNTransaction.animationDuration = 0.5
            
            material.emission.contents = UIColor.black
            
            SCNTransaction.commit()
        }
        
        material.emission.contents = UIColor.red
        
        SCNTransaction.commit()
    }
}
 */

/*
// add a tap gesture recognizer
let tapGesture = UITapGestureRecognizer(target: self, action: #selector(handleTap(_:)))
scnView.addGestureRecognizer(tapGesture)
 */
