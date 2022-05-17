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
        VStack {
            Text(title)
                .font(.caption)
                .frame(height: 6)
                .padding(.top, 6)
            Slider(value: $sliderState, in: 0...1)
                .onChange(of: sliderState) {newValue in
                    onChangeFunction(sliderState)
                    update()
                }
        }
        .frame(width: 128)
        .background(Color.white.opacity(0.7))
        .clipShape(RoundedRectangle(cornerRadius: 8))
    }
}
