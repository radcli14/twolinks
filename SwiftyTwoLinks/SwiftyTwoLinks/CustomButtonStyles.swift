//
//  CustomButtonStyles.swift
//  SwiftyTwoLinks
//
//  Created by Eliott Radcliffe on 5/10/22.
//

import Foundation
import SwiftUI

struct SliderButtonStyle: ButtonStyle {
    var iconSize: Double = 32.0
    
    func makeBody(configuration: Configuration) -> some View {
        configuration.label
            .frame(width: iconSize, height: iconSize)
            .background(.gray)
            .clipShape(Circle())
            .foregroundColor(.white)
    }
}
