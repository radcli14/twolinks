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
    // Radius of the planet
    var radius: Double {
        set {
            geometry.radius = newValue
        }
        get {
            return geometry.radius
        }
    }
    
    // Positions of the planet relative to its parent node
    private var _x = 0.0
    private var _y = 0.0
    private var _z = 0.0
    
    // Angle of the planet, for now measured in radians about the X-axis
    //private var _xAngle = 0.0
    //private var _yAngle = 0.0
    
    // Image that gets super-imposed on the planet, argument provides as a string file name
    private var _image: UIImage? = nil
    var isGeodesic = false
    var segmentCount: Int? = nil
    var shininess: Double? = nil
    
    let geometry: SCNSphere!
    let node: SCNNode!
    
    init(radius: Double = 31.4,
         x: Double = 0.0, y: Double = 0.0, z: Double = 0.0,
         xAngle: Double = 0.0, yAngle: Double = 0.0,
         image: String? = nil,
         isGeodesic: Bool = false, segmentCount: Int? = nil, shininess: Double? = nil
    ) {
        // Unpack the arguments
        //_radius = radius
        _x = x
        _y = y
        _z = z
        //_angle = angle
        if image != nil {
            _image = UIImage(named: image!)
        }
        
        // Create the spherical geometry, with optional image that gets wrapped around it, and shininess
        geometry = SCNSphere(radius: radius)
        geometry.isGeodesic = isGeodesic
        geometry.segmentCount = segmentCount ?? 128
        geometry.materials.first?.diffuse.contents = _image
        geometry.materials.first?.shininess = shininess ?? 0.0
        
        // Create the node that gets added to the scene
        node = SCNNode(geometry: geometry)
        node.position = SCNVector3(_x, _y, _z)
        let xQuat = simd_quatd(ix: sin(0.5 * xAngle), iy: 0, iz: 0, r: cos(0.5 * xAngle))
        let yQuat = simd_quatd(ix: 0, iy: sin(0.5 * yAngle), iz: 0, r: cos(0.5 * yAngle))
        let quat = xQuat * yQuat
        node.orientation = SCNQuaternion(quat.vector)  //quat.ix, quat.iy, quat.iz, quat.r)
    }
}
