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
    let iconSize = 32.0
    let stackPadding = 12.0
    
    @State private var isPaused: Bool = false
    @State private var dimensionSlidersVisible: Bool = false
    @State private var linkOneLengthSliderVal: Double = 0.5
    
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
                // Hold the controls on the left side to reset or pause motion
                VStack(alignment: .trailing) {
                    // Reset the pendulum to initial states
                    Button(action: {
                        print("Reset button tapped!")
                        viewController.resetStates()
                    }) {
                        Image(systemName: "arrow.uturn.left.circle.fill")
                            .resizable()
                            .foregroundColor(Color.gray)
                            .frame(width: iconSize, height: iconSize)
                    }
                    
                    // Pause the simulation
                    Button(action: {
                        print("Pause button tapped!")
                        viewController.pause()
                        isPaused = viewController.isPaused
                    }) {
                        Image(systemName: isPaused ? "play.circle.fill" : "pause.circle.fill")
                            .resizable()
                            .foregroundColor(Color.gray)
                            .frame(width: iconSize, height: iconSize)
                    }
                    
                    Spacer()
                    
                    
                }

                if (dimensionSlidersVisible) {
                    VStack {
                        Text("Link One Length")
                            .font(.caption)
                            .foregroundColor(Color.white)
                            .frame(height: 4.0)
                        Slider(value: $linkOneLengthSliderVal, in: 1...100)
                            .frame(height: 4.0)
                            .onChange(of: linkOneLengthSliderVal) {newValue in
                                viewController.twoLinks.setLinkOneLengthFromNorm(
                                    value: linkOneLengthSliderVal
                                )
                            }
                        Spacer()
                    }
                    .padding(12)
                } else {
                    Spacer()
                }
                
                // Hold the controls on the right side for link dimensions
                VStack {
                    // Pause the simulation
                    Button(action: {
                        print("Dimension button tapped!")
                        dimensionSlidersVisible.toggle()
                    }) {
                        Image(systemName: "scale.3d")
                            .resizable()
                            .foregroundColor(.gray)
                            .frame(width: iconSize, height: iconSize)
                    }.onChange(of: dimensionSlidersVisible) {newValue in
                        if (newValue) {
                            linkOneLengthSliderVal = viewController.twoLinks.linkOneLengthNorm
                        }
                    }
                    /*
                    ButtonSlider(text: "a")
                    ButtonSlider(text: "b")
                    ButtonSlider(text: "c")
                    ButtonSlider(text: "d")
                    ButtonSlider(text: "e")
                     */
                    Spacer()
                }

            }
            .padding(stackPadding)
        }
    }
}

struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        ContentView()
    }
}
