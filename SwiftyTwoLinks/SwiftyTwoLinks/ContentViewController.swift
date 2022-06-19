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
    
    //let moonRadius = 31.4
    var moon: Planet!
    var moonColor: UIColor {
        get {
            return moon.geometry.materials.first?.emission.contents as! UIColor
        }
        set {
            moon.geometry.materials.first?.emission.contents = newValue
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
            UIImage(named: "px"),
            UIImage(named: "nx"),
            UIImage(named: "py"),
            UIImage(named: "ny"),
            UIImage(named: "pz"),
            UIImage(named: "nz")
        ]

        // Light is provided through sparks of energy of the mind that travel in rhyme form
        let lightNode = SCNNode()
        lightNode.light = SCNLight()
        lightNode.light!.type = .omni
        lightNode.light!.zFar = 100000.0
        lightNode.light!.intensity = 2000
        lightNode.position = SCNVector3(x: 360.0, y: 360.0, z: 1080.0)
        lightNode.light?.castsShadow = true
        scene.rootNode.addChildNode(lightNode)
        
        // create and add an ambient light to the scene
        let ambientLightNode = SCNNode()
        ambientLightNode.light = SCNLight()
        ambientLightNode.light!.type = .ambient
        ambientLightNode.light!.intensity = 15.7
        ambientLightNode.light!.color = UIColor.systemGreen
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
        moon = Planet(
            radius: 31.4,
            y: -32.415,
            xAngle: 0.6 * Double.pi,
            image: "moonmap4k",
            segmentCount: 512
        )
        
        // Define an Earth in the background
        let earth = Planet(
            radius: 3.66 * moon.radius,
            x: 6.28 * moon.radius,
            y: -2.0 * moon.radius,
            z: -22.0 * moon.radius,
            xAngle: 0.4,
            yAngle: 0.5,
            image: "earthmap1k"//,
            //specular: "earthspec1k"
        )
        
        // Define a door that is behind the pendulum
        doorGeometry = SCNBox(
            width: 0.91,
            height: 2.03,
            length: 0.035,
            chamferRadius: 0.01
        )
        doorColor = UIColor.black
        //doorGeometry.materials.first?.lightingModel = SCNMaterial.LightingModel.physicallyBased
        //doorGeometry.materials.first?.metalness = 1.0
        //doorGeometry.materials.first?.shininess = 1.0
        doorGeometry.materials.first?.specular.contents = UIColor.lightGray
        let doorNode = SCNNode(geometry: doorGeometry)
        doorNode.position = SCNVector3(0, 0, -0.015)
        doorNode.castsShadow = true
        
        // Add the parts into the scene
        scene.rootNode.addChildNode(moon.node)
        scene.rootNode.addChildNode(doorNode)
        scene.rootNode.addChildNode(earth.node)
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
    
    func shuffle() {
        twoLinks.setLinkOneLengthFromNorm(value: Double.random(in: 0...1))
        twoLinks.setLinkTwoLengthFromNorm(n: Double.random(in: 0...1))
        twoLinks.setPivotFromNorm(m: Double.random(in: 0...1))
        twoLinks.setLinkOneOffsetFromNorm(n: Double.random(in: 0...1))
        twoLinks.setLinkTwoOffsetFromNorm(n: Double.random(in: 0...1))
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
