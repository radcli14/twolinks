//
//  DimensionSliders.swift
//  SwiftyTwoLinks
//
//  Created by Eliott Radcliffe on 5/16/22.
//

import Foundation
import SwiftUI

struct DimensionSliders: View {
    let viewController: ContentViewController
    @State private var linkOneLengthSliderVal: Double = 0.5
    @State private var linkOneOffsetSliderVal: Double = 0.5
    @State private var pivotSliderVal: Double = 0.5
    @State private var linkTwoLengthSliderVal: Double = 0.5
    @State private var linkTwoOffsetSliderVal: Double = 0.5
    
    var body: some View {
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
        .onAppear {
            updateSliders()
        }
    }
    
    /**
     The normalized values for each slider state are updated based upon values calculated internally to the TwoLinks model. This gets called whenever the user controls one of the states, because as dimensions change the offsets and pivot points may need to be adjusted to assure that they stay within their constraints.
     */
    func updateSliders() {
        linkOneLengthSliderVal = viewController.twoLinks.linkOneLengthNorm
        linkOneOffsetSliderVal = viewController.twoLinks.linkOneOffsetNorm
        pivotSliderVal = viewController.twoLinks.pivotNorm
        linkTwoLengthSliderVal = viewController.twoLinks.linkTwoLengthNorm
        linkTwoOffsetSliderVal = viewController.twoLinks.linkTwoOffsetNorm
    }
}

/*struct DimensionSliders_Previews: PreviewProvider {
    let viewController = ContentViewController()
    static var previews: some View {
        DimensionSliders(viewController: ContentViewController)
    }
}*/
