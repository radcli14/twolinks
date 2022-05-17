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
    @State public var linkTwoOffsetSliderVal: Double = 0.5
    
    var body: some View {
        ZStack(alignment: .bottom) {
            // Add the 3D rendering view
            SceneView(
                scene: viewController.scene,
                pointOfView: viewController.cameraNode,
                options: [.allowsCameraControl, .rendersContinuously],
                delegate: viewController
            ).gesture(
                TapGesture()
                    .onEnded { _ in
                        withAnimation {
                            dimensionSlidersVisible = false
                        }
                    }
            )
            
            // Add the user controls
            HStack {
                // Hold the controls on the left side to reset or pause motion
                VStack(alignment: .trailing) {
                    // Reset the pendulum to initial states
                    Button(action: {
                        viewController.resetStates()
                    }) {
                        Image(systemName: "arrow.uturn.left.circle.fill")
                            .resizable()
                            .foregroundColor(Color.gray)
                            .frame(width: iconSize, height: iconSize)
                    }
                    
                    // Pause the simulation
                    Button(action: {
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

                Spacer()
                
                // Hold the controls on the right side for link dimensions
                VStack {
                    // Pause the simulation
                    Button(action: {
                        withAnimation {
                            dimensionSlidersVisible.toggle()
                        }
                    }) {
                        Image(systemName: "scale.3d")
                            .resizable()
                            .foregroundColor(dimensionSlidersVisible ? .white : .gray)
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
            
            // Holds the controls to modify lengths and connection points, uses custom TextSlider view
            if dimensionSlidersVisible {
                VStack {
                    HStack {
                        VStack {
                            TextSlider(
                                title: "Link One Length",
                                sliderState: $linkOneLengthSliderVal,
                                onChangeFunction: viewController.twoLinks.setLinkOneLengthFromNorm,
                                update: updateSliders
                            )
                            
                            TextSlider(
                                title: "Link One Offset",
                                sliderState: $linkOneOffsetSliderVal,
                                onChangeFunction: viewController.twoLinks.setLinkOneOffsetFromNorm,
                                update: updateSliders
                            )
                            
                        }
                        
                        VStack {
                            TextSlider(
                                title: "Link Two Length",
                                sliderState: $linkTwoLengthSliderVal,
                                onChangeFunction: viewController.twoLinks.setLinkTwoLengthFromNorm,
                                update: updateSliders
                            )
                            
                            TextSlider(
                                title: "Link Two Offset",
                                sliderState: $linkTwoOffsetSliderVal,
                                onChangeFunction: viewController.twoLinks.setLinkTwoOffsetFromNorm,
                                update: updateSliders
                            )
                        }
                    }
                    
                    TextSlider(
                        title: "Pivot",
                        sliderState: $pivotSliderVal,
                        onChangeFunction: viewController.twoLinks.setPivotFromNorm,
                        update: updateSliders
                    )
                    .padding(.bottom, 8)
                }
                .transition(.move(edge: .bottom))
                .zIndex(1)
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
