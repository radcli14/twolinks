//
//  ContentView.swift
//  SwiftyTwoLinks
//
//  Created by Eliott Radcliffe on 5/8/22.
//

import SwiftUI
import SceneKit

struct ContentView: View {
    var viewController: ContentViewController
    let iconSize = 32.0
    let stackPadding = 12.0
    
    @State private var isPaused: Bool = false
    @State private var dimensionSlidersVisible: Bool = false
    @State private var visualControlsVisible: Bool = false

    var body: some View {
        ZStack(alignment: .bottom) {
            // Add the 3D rendering view
            SceneView(
                scene: viewController.scene,
                pointOfView: viewController.cameraNode,
                options: [.rendersContinuously],
                delegate: SceneUpdateDelegate(viewController: viewController)
            ).gesture(
                TapGesture()
                    .onEnded { _ in
                        withAnimation {
                            removeControls()
                        }
                    }
                
            ).gesture(
                DragGesture()
                    .onChanged { gesture in
                        // Determine the total number of pixels that the user has dragged, relate it to an angle that the camera will traverse
                        let x = Float(gesture.translation.width)
                        let y = Float(gesture.translation.height)
                        let nPixels = sqrt(x*x + y*y)
                        let angle = 0.01 * nPixels
                        
                        // Pan the camera around the target
                        viewController.cameraNode.position = viewController
                            .cameraPosBefore
                            .rotatedVector(
                                axis: simd_float3(-y, -x, 0),
                                angle: angle
                            )
                    }
                    .onEnded { _ in
                        viewController.cameraPosBefore = viewController.cameraNode.position
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
                
                // Hold the controls on the right side for link dimensions and visual properties
                VStack {
                    // Dimensions
                    Button(action: {
                        withAnimation {
                            visualControlsVisible = false
                            dimensionSlidersVisible.toggle()
                        }
                    }) {
                        Image(systemName: "scale.3d")
                            .resizable()
                            .foregroundColor(dimensionSlidersVisible ? .white : .gray)
                            .frame(width: iconSize, height: iconSize)
                    }
                    
                    // Visual characteristics
                    Button(action: {
                        withAnimation {
                            dimensionSlidersVisible = false
                            visualControlsVisible.toggle()
                        }
                    }) {
                        Image(systemName: "paintpalette")
                            .resizable()
                            .foregroundColor(visualControlsVisible ? .white : .gray)
                            .frame(width: iconSize, height: iconSize)
                    }
                    
                    Spacer()
                }

            }
            .padding(stackPadding)
            
            // Holds the controls to modify lengths and connection points, uses custom TextSlider view
            if dimensionSlidersVisible {
                DimensionSliders(viewController: viewController)
            }
            
            // Holds the controls to modify colors
            if visualControlsVisible {
                VisualControls(viewController: viewController)
            }
        }
    }
    
    func removeControls() {
        dimensionSlidersVisible = false
        visualControlsVisible = false
    }
}

struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        ContentView(viewController: ContentViewController())
    }
}
