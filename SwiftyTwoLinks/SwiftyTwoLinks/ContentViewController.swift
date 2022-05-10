//
//  ContentViewController.swift
//  SwiftyTwoLinks
//
//  Created by Eliott Radcliffe on 5/9/22.
//

import Foundation
import UIKit
import QuartzCore
import SceneKit
import SpriteKit
import CoreMotion
import SwiftUI

class ContentViewController: NSObject, SCNSceneRendererDelegate {

    let manager = CMMotionManager()
    var scene: SCNScene!
    var overlaySKScene: SKScene!
    var cameraNode: SCNNode!
    var linkOneNode: SCNNode!
    var pivotNode: SCNNode!
    var linkTwoNode: SCNNode!
    let twoLinks = TwoLinks()
    var isPaused: Bool = false
    
    override init() {
        super.init()
        setupScene()
        setupCamera()
        setupBackground()
        setupLinks()
        
        /*
        // add a tap gesture recognizer
        let tapGesture = UITapGestureRecognizer(target: self, action: #selector(handleTap(_:)))
        scnView.addGestureRecognizer(tapGesture)
         */
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

    func setupScene() {
        scene = SCNScene()
        
        // Create the background color
        scene.background.contents = UIColor.black // UIImage(named: "art.scnassets/moonTexture.png")

        // create and add a light to the scene
        let lightNode = SCNNode()
        lightNode.light = SCNLight()
        lightNode.light!.type = .omni
        lightNode.position = SCNVector3(x: 6.28, y: 15, z: 25)
        lightNode.light?.castsShadow = true
        scene.rootNode.addChildNode(lightNode)
        
        // create and add an ambient light to the scene
        let ambientLightNode = SCNNode()
        ambientLightNode.light = SCNLight()
        ambientLightNode.light!.type = .ambient
        ambientLightNode.light!.color = UIColor.white
        scene.rootNode.addChildNode(ambientLightNode)
    }

    func setupCamera() {
      cameraNode = SCNNode()
      cameraNode.camera = SCNCamera()
        cameraNode.position = SCNVector3(x: 0, y: 0, z: 2.5)
      scene.rootNode.addChildNode(cameraNode)
    }
    
    func setupBackground() {
        // Create a floor to define the ground and horizon
        let floorGeometry = SCNSphere(radius: 100)
        floorGeometry.segmentCount = 128
        floorGeometry.materials.first?.diffuse.contents = UIImage(named: "moonTexture") // UIColor.darkGray
        floorGeometry.materials.first?.shininess = 0.25
        floorGeometry.materials.first?.specular.contents = UIColor.white
        let floorNode = SCNNode(geometry: floorGeometry)
        floorNode.position = SCNVector3(0, -1.015-100.0, 0)
        floorNode.orientation = SCNQuaternion(sin(Double.pi/8.0), 0, 0, cos(Double.pi/8.0))
        
        // Define a door that is behind the pendulum
        let doorGeometry = SCNBox(
            width: 0.91,
            height: 2.03,
            length: 0.035,
            chamferRadius: 0.01
        )
        doorGeometry.materials.first?.diffuse.contents = UIColor.black
        doorGeometry.materials.first?.shininess = 0.5
        doorGeometry.materials.first?.specular.contents = UIColor.systemGreen
        let doorNode = SCNNode(geometry: doorGeometry)
        doorNode.position = SCNVector3(0, 0, -0.015)
        doorNode.castsShadow = true
        let handleGeometry = SCNTorus(
            ringRadius: 0.02,
            pipeRadius: 0.0225
        )
        handleGeometry.materials.first?.diffuse.contents = UIColor.lightGray
        doorGeometry.materials.first?.shininess = 1.0
        doorGeometry.materials.first?.specular.contents = UIColor.white
        let handleNode = SCNNode(geometry: handleGeometry)
        handleNode.position = SCNVector3(0.375, 0, 0.02)
        handleNode.orientation = SCNQuaternion(sin(Double.pi/4.0), 0, 0, cos(Double.pi/4.0))
        
        // Add the parts into the scene
        scene.rootNode.addChildNode(floorNode)
        scene.rootNode.addChildNode(doorNode)
        scene.rootNode.addChildNode(handleNode)
    }
    
    func setupLinks() {
        // Define a cylinder to represent the origin about which the first link rotates
        let originGeometry = SCNCylinder(
            radius: 0.007,
            height: 0.007
        )
        originGeometry.materials.first?.diffuse.contents = UIColor.darkGray
        let originNode = SCNNode(geometry: originGeometry)
        originNode.position = SCNVector3(0, 0, twoLinks.thickness[0])
        originNode.orientation = SCNQuaternion(sin(Double.pi/4.0), 0, 0, cos(Double.pi/4.0))

        // Define the first pendulum link
        linkOneNode = SCNNode(geometry: twoLinks.geometry[0])
        
        // Define the pivot that connects the two links
        let pivotGeometry = SCNCylinder(
            radius: 0.007,
            height: 0.013
        )
        pivotGeometry.materials.first?.diffuse.contents = UIColor.darkGray
        pivotNode = SCNNode(geometry: pivotGeometry)
        pivotNode.orientation = SCNQuaternion(sin(Double.pi/4.0), 0, 0, cos(Double.pi/4.0))
        
        // Define the second pendulum link
        linkTwoNode = SCNNode(geometry: twoLinks.geometry[1])
        
        // Add the parts into the scene
        scene.rootNode.addChildNode(originNode)
        scene.rootNode.addChildNode(linkOneNode)
        scene.rootNode.addChildNode(pivotNode)
        scene.rootNode.addChildNode(linkTwoNode)
    }
    
    func renderer(_ renderer: SCNSceneRenderer, updateAtTime time: TimeInterval) {
        // Update the link position and orientation
        if (!isPaused) {
            twoLinks.update()
        }
        linkOneNode.orientation = twoLinks.orientation[0]
        linkOneNode.position = twoLinks.position[0]
        pivotNode.position = twoLinks.pivotPosition
        linkTwoNode.orientation = twoLinks.orientation[1]
        linkTwoNode.position = twoLinks.position[1]
    }
    
    func resetStates() {
        twoLinks.θ = [0.0, 0.0]
        twoLinks.ω = [0.0, 0.0]
    }
    
    func pause() {
        //isPaused.toggle()
        isPaused = !isPaused
    }
}

