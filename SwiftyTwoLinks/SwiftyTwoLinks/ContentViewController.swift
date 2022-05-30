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

class ContentViewController {

    //let manager = CMMotionManager()
    var scene: SCNScene!
    var overlaySKScene: SKScene!
    
    var cameraNode: SCNNode!
    var cameraPosBefore = SCNVector3(x: 0, y: 0, z: 2.5)
    
    var originNode: SCNNode!
    var linkOneNode: SCNNode!
    var pivotNode: SCNNode!
    var linkTwoNode: SCNNode!
    let twoLinks = TwoLinks()
    var isPaused: Bool = false
    
    var doorGeometry: SCNGeometry!
    var doorColor: UIColor {
        get {
            doorGeometry.materials.first?.diffuse.contents as! UIColor
        }
        set {
            doorGeometry.materials.first?.diffuse.contents = newValue
        }
    }
    
    let moonRadius = 31.4
    var floorGeometry: SCNSphere!
    var floorColor: UIColor {
        get {
            return floorGeometry.materials.first?.emission.contents as! UIColor
        }
        set {
            floorGeometry.materials.first?.emission.contents = newValue
        }
    }

    init() {
        setupScene()
        setupCamera()
        setupBackground()
        setupLinks()
    }

    func setupScene() {
        scene = SCNScene()
        
        // Create the background skybox
        scene.background.contents = [
            UIImage(named: "starry_right1"),
            UIImage(named: "starry_left2"),
            UIImage(named: "starry_top3"),
            UIImage(named: "starry_bottom4"),
            UIImage(named: "starry_back6"),
            UIImage(named: "starry_front5")
        ]

        // Light is provided through sparks of energy of the mind that travel in rhyme form
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
        cameraNode.camera?.zFar = 10000.0
        cameraNode.position = SCNVector3(x: 0, y: 0, z: 2.5)
        
        // Constrain the camera to look at the center of the door
        let lookAtConstraint = SCNLookAtConstraint(target: scene.rootNode)
        lookAtConstraint.isGimbalLockEnabled = true
        cameraNode.constraints = [lookAtConstraint]
        
        scene.rootNode.addChildNode(cameraNode)
    }
    
    func setupBackground() {
        let centerNode = SCNNode()
        centerNode.position = SCNVector3(0, 0, 0)
        scene.rootNode.addChildNode(centerNode)
        
        // Create the moon to define the ground and horizon
        floorGeometry = SCNSphere(radius: moonRadius)
        //floorGeometry.isGeodesic = true
        floorGeometry.segmentCount = 128
        floorGeometry.materials.first?.diffuse.contents = UIImage(named: "moonmap4k")
        floorGeometry.materials.first?.shininess = 0.25
        //floorColor = UIColor.gray
        let floorNode = SCNNode(geometry: floorGeometry)
        floorNode.position = SCNVector3(0, -1.015-moonRadius, 0)
        let moonAngle = 0.6 * Double.pi
        floorNode.orientation = SCNQuaternion(sin(0.5 * moonAngle), 0, 0, cos(0.5 * moonAngle))
        //let moon = Planet(radius: moonRadius, y: -1.015-moonRadius, image: "moonmap4k")
        
        // Define a door that is behind the pendulum
        doorGeometry = SCNBox(
            width: 0.91,
            height: 2.03,
            length: 0.035,
            chamferRadius: 0.01
        )
        doorColor = UIColor.black
        doorGeometry.materials.first?.shininess = 0.5
        doorGeometry.materials.first?.specular.contents = UIColor.white
        let doorNode = SCNNode(geometry: doorGeometry)
        doorNode.position = SCNVector3(0, 0, -0.015)
        doorNode.castsShadow = true
        
        // Define an Earth
        let earthGeometry = SCNSphere(radius: 3.66 * moonRadius)
        earthGeometry.materials.first?.diffuse.contents =  UIImage(named: "earthmap1k")
        let earthNode = SCNNode(geometry: earthGeometry)
        earthNode.position = SCNVector3(3.14*moonRadius, -moonRadius, -22*moonRadius)
        
        // Add the parts into the scene
        //scene.rootNode.addChildNode(moon.node)
        scene.rootNode.addChildNode(floorNode)
        scene.rootNode.addChildNode(doorNode)
        scene.rootNode.addChildNode(earthNode)
    }
    
    func setupLinks() {
        // Define a cylinder to represent the origin about which the first link rotates
        let originGeometry = SCNCylinder(
            radius: 0.007,
            height: 0.007
        )
        originGeometry.materials.first?.diffuse.contents = UIColor.darkGray
        originNode = SCNNode(geometry: originGeometry)
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
        
        // Specify initial colors of the links
        twoLinks.linkOneColor = UIColor.random()
        twoLinks.linkTwoColor = UIColor.random()
    }
    
    func resetStates() {
        twoLinks.θ = [0.0, 0.0]
        twoLinks.ω = [0.0, 0.0]
    }
    
    func pause() {
        isPaused.toggle()
    }
}

extension UIColor {
    static func random() -> UIColor {
        return UIColor(
           red:   .random(in: 0...1),
           green: .random(in: 0...1),
           blue:  .random(in: 0...1),
           alpha: 1.0
        )
    }
}
