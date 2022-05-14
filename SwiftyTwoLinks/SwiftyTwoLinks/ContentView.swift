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
    @State private var linkOneOffsetSliderVal: Double = 0.5
    @State private var pivotSliderVal: Double = 0.5
    @State private var linkTwoLengthSliderVal: Double = 0.5
    @State private var linkTwoOffsetSliderVal: Double = 0.5
    
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

                /*if (dimensionSlidersVisible) {
                    VStack {
                        //Spacer()
                        
                        Text("Link One Length")
                            .font(.caption)
                            .foregroundColor(Color.white)
                            //.frame(height: 6.0)
                        Slider(value: $linkOneLengthSliderVal, in: 1...100)
                            //.frame(height: 6.0)
                            .onChange(of: linkOneLengthSliderVal) {newValue in
                                viewController.twoLinks.setLinkOneLengthFromNorm(
                                    value: linkOneLengthSliderVal
                                )
                                updateSliders()
                            }
                        
                        Text("Link One Offset")
                            .font(.caption)
                            .foregroundColor(Color.white)
                            //.frame(height: 6.0)
                        Slider(value: $linkOneOffsetSliderVal, in: 1...100)
                            //.frame(height: 6.0)
                            .onChange(of: linkOneOffsetSliderVal) {newValue in
                                viewController.twoLinks.setLinkOneOffsetFromNorm(
                                    value: linkOneOffsetSliderVal
                                )
                                updateSliders()
                            }
                        
                        Text("Pivot")
                            .font(.caption)
                            .foregroundColor(Color.white)
                            //.frame(height: 6.0)
                        Slider(value: $pivotSliderVal, in: 1...100)
                            //.frame(height: 6.0)
                            .onChange(of: pivotSliderVal) {newValue in
                                viewController.twoLinks.setPivotFromNorm(
                                    value: pivotSliderVal
                                )
                                updateSliders()
                            }
                        
                        Text("Link Two Length")
                            .font(.caption)
                            .foregroundColor(Color.white)
                            //.frame(height: 6.0)
                        Slider(value: $linkTwoLengthSliderVal, in: 1...100)
                            //.frame(height: 6.0)
                            .onChange(of: linkTwoLengthSliderVal) {newValue in
                                viewController.twoLinks.setLinkTwoLengthFromNorm(
                                    value: linkTwoLengthSliderVal
                                )
                                updateSliders()
                            }
                        
                        Text("Link Two Offset")
                            .font(.caption)
                            .foregroundColor(Color.white)
                            //.frame(height: 6.0)
                        Slider(value: $linkTwoOffsetSliderVal, in: 1...100)
                            //.frame(height: 6.0)
                            .onChange(of: linkTwoOffsetSliderVal) {newValue in
                                viewController.twoLinks.setLinkTwoOffsetFromNorm(
                                    value: linkTwoOffsetSliderVal
                                )
                                updateSliders()
                            }
                        
                        //Spacer()
                    }
                    //.padding(12)
                    */
                //} else {
                    Spacer()
                //}
                
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
                            updateSliders()
                        }
                    }
                    Spacer()
                }

            }
            .padding(stackPadding)
            .sheet(isPresented: $dimensionSlidersVisible) {
                VStack {
                    //Spacer()
                    
                    Text("Link One Length")
                        .font(.caption)
                        //.foregroundColor(Color.white)
                        //.frame(height: 6.0)
                    Slider(value: $linkOneLengthSliderVal, in: 1...100)
                        //.frame(height: 6.0)
                        .onChange(of: linkOneLengthSliderVal) {newValue in
                            viewController.twoLinks.setLinkOneLengthFromNorm(
                                value: linkOneLengthSliderVal
                            )
                            updateSliders()
                        }
                    
                    Text("Link One Offset")
                        .font(.caption)
                        //.foregroundColor(Color.white)
                        //.frame(height: 6.0)
                    Slider(value: $linkOneOffsetSliderVal, in: 1...100)
                        //.frame(height: 6.0)
                        .onChange(of: linkOneOffsetSliderVal) {newValue in
                            viewController.twoLinks.setLinkOneOffsetFromNorm(
                                value: linkOneOffsetSliderVal
                            )
                            updateSliders()
                        }
                    
                    Text("Pivot")
                        .font(.caption)
                        //.foregroundColor(Color.white)
                        //.frame(height: 6.0)
                    Slider(value: $pivotSliderVal, in: 1...100)
                        //.frame(height: 6.0)
                        .onChange(of: pivotSliderVal) {newValue in
                            viewController.twoLinks.setPivotFromNorm(
                                value: pivotSliderVal
                            )
                            updateSliders()
                        }
                    
                    Text("Link Two Length")
                        .font(.caption)
                        //.foregroundColor(Color.white)
                        //.frame(height: 6.0)
                    Slider(value: $linkTwoLengthSliderVal, in: 1...100)
                        //.frame(height: 6.0)
                        .onChange(of: linkTwoLengthSliderVal) {newValue in
                            viewController.twoLinks.setLinkTwoLengthFromNorm(
                                value: linkTwoLengthSliderVal
                            )
                            updateSliders()
                        }
                    
                    Text("Link Two Offset")
                        .font(.caption)
                        //.foregroundColor(Color.white)
                        //.frame(height: 6.0)
                    Slider(value: $linkTwoOffsetSliderVal, in: 1...100)
                        //.frame(height: 6.0)
                        .onChange(of: linkTwoOffsetSliderVal) {newValue in
                            viewController.twoLinks.setLinkTwoOffsetFromNorm(
                                value: linkTwoOffsetSliderVal
                            )
                            updateSliders()
                        }
                    
                    //Spacer()
                }
            }
        }
    }
    
    func updateSliders() {
        linkOneLengthSliderVal = viewController.twoLinks.linkOneLengthNorm
        linkOneOffsetSliderVal = viewController.twoLinks.linkOneOffsetNorm
        pivotSliderVal = viewController.twoLinks.pivotNorm
        linkTwoLengthSliderVal = viewController.twoLinks.linkTwoLengthNorm
        linkTwoOffsetSliderVal = viewController.twoLinks.linkTwoOffsetNorm
    }
}

struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        ContentView()
    }
}
