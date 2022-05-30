//
//  Planets.swift
//  SwiftyTwoLinks
//
//  Created by Eliott Radcliffe on 5/27/22.
//

import Foundation
import SceneKit

/**
 Base struct for a planet, most importantly will provide a spherical geometry, and a SCNNode that can be added to the scene
 */
struct Planet {
    private var _radius = 31.4
    private var _x = 0.0
    private var _y = 0.0
    private var _z = 0.0
    private var _angle = 0.0
    private var _image: UIImage? = nil
    var isGeodesic = false
    var segmentCount: Int? = nil
    var shininess: Double? = nil
    
    let geometry: SCNSphere!
    let node: SCNNode!
    
    init(radius: Double = 31.4,
         x: Double = 0.0, y: Double = 0.0, z: Double = 0.0, angle: Double = 0.0,
         image: String? = nil
    ) {
        // Unpack the arguments
        _radius = radius
        _x = x
        _y = y
        _z = z
        _angle = angle
        if image != nil {
            _image = UIImage(named: image!)
        }
        
        // Create the spherical geometry, with optional image that gets wrapped around it, and shininess
        geometry = SCNSphere(radius: _radius)
        geometry.segmentCount = segmentCount ?? 0
        geometry.materials.first?.diffuse.contents = _image
        geometry.materials.first?.shininess = shininess ?? 0.0
        
        // Create the node that gets added to the scene
        node = SCNNode(geometry: geometry)
        node.position = SCNVector3(_x, _y, _z)
        node.orientation = SCNQuaternion(sin(0.5 * _angle), 0, 0, cos(0.5 * _angle))
        
        print(_radius)
        print(_image)
        print(node.position)
    }
}
