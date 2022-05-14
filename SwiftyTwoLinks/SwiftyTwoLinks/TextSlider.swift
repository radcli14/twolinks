//
//  TextSlider.swift
//  SwiftyTwoLinks
//
//  Created by Eliott Radcliffe on 5/14/22.
//

import Foundation
import SwiftUI

struct TextSlider: View {
    let title: String
    @Binding public var sliderState: Double
    let onChangeFunction: (Double) -> Void
    let update: () -> Void
    
    var body: some View {
        Text(title)
            .font(.caption)
        Slider(value: $sliderState, in: 1...100)
            .onChange(of: sliderState) {newValue in
                onChangeFunction(sliderState)
                update()
            }
    }
}
