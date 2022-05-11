//
//  ButtonSlider.swift
//  SwiftyTwoLinks
//
//  Created by Eliott Radcliffe on 5/10/22.
//

import SwiftUI
import Foundation

struct ButtonSlider: View {
    var text = "a"
    var sliderOpensOn = "left"

    @State var sliderVal: Double = 0
    @State private var isExpanded: Bool = false
    
    var body: some View {
        HStack {
            // Slider on left side
            if (sliderOpensOn == "left") {
                if (isExpanded) {
                    Slider(value: $sliderVal, in: 0...100)
                } else {
                    Spacer()
                }
            }
            
            // Button to show the slider
            Button(action: {
                print("\(text) ButtonSlider tapped!")
                isExpanded.toggle()
            }) {
                Text(text)
                    .font(.system(size: 22, design: .serif))
                    .fontWeight(.semibold)
                    .italic()
                    .padding([.bottom, .trailing], 2.0)
            }
            .buttonStyle(SliderButtonStyle())

            // Slider on right side
            if (sliderOpensOn == "right") {
                if (isExpanded) {
                    Slider(value: $sliderVal, in: 0...100)
                } else {
                    Spacer()
                }
            }
        }
    }
}

struct ButtonSlider_Previews: PreviewProvider {
    static var previews: some View {
        ButtonSlider()
    }
}
