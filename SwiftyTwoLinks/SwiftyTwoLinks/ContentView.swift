//
//  ContentView.swift
//  SwiftyTwoLinks
//
//  Created by Eliott Radcliffe on 5/8/22.
//

import SwiftUI
import SceneKit

struct ContentView: View {
    var viewController = ContentViewController()
    var sceneViewOptions = SceneView.Options()
    
    var body: some View {
        VStack {
            SceneView(
                scene: viewController.scene,
                pointOfView: viewController.cameraNode,
                options: [.allowsCameraControl, .rendersContinuously],
                delegate: viewController
            )
        }
        
    }
}

struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        ContentView()
    }
}
