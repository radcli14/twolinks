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
    let iconSize = 24.0
    
    var body: some View {
        ZStack {
            // Add the 3D rendering view
            SceneView(
                scene: viewController.scene,
                pointOfView: viewController.cameraNode,
                options: [.allowsCameraControl, .rendersContinuously],
                delegate: viewController
            )
            
            // Add the user controls
            HStack {
                VStack(alignment: .trailing) {
                    // Reset the pendulum to initial states
                    Button(action: {
                        print("Reset button tapped!")
                        viewController.resetStates()
                    }) {
                        Image(systemName: "arrow.uturn.left.circle.fill").resizable()
                            .foregroundColor(Color.gray)
                            .frame(width: iconSize, height: iconSize)
                    }
                    
                    // Pause the simulation
                    Button(action: {
                        print("Pause button tapped!")
                        viewController.pause()
                    }) {
                        Image(systemName: "pause.circle.fill").resizable()
                            .foregroundColor(Color.gray)
                            .frame(width: iconSize, height: iconSize)
                    }
                    
                    Spacer()
                }
                .padding(12)
                Spacer()
            }
        }
        
    }
}

struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        ContentView()
    }
}
