//
//  VisualControls.swift
//  SwiftyTwoLinks
//
//  Created by Eliott Radcliffe on 5/16/22.
//

import Foundation
import SwiftUI

struct LinkVisualControl: View {
    let heading: String
    @Binding var color: Color
    
    var body: some View {
        VStack {
            Text(heading)
            ColorPicker("Color", selection: $color)
        }
        .frame(width: 128)
        .background(Color.white.opacity(0.7))
        .clipShape(RoundedRectangle(cornerRadius: 8))
    }
}

struct VisualControls: View {
    @State var linkOneColor = Color.brown
    @State var linkTwoColor = Color.brown
    @State var monolithColor = Color.black
    @State var moonColor = Color.gray
    
    var body: some View {
        VStack {
            HStack {
                LinkVisualControl(heading: "Link One", color: $linkOneColor)
                LinkVisualControl(heading: "Link Two", color: $linkTwoColor)
            }
            HStack {
                LinkVisualControl(heading: "Monolith", color: $monolithColor)
                LinkVisualControl(heading: "Moon", color: $moonColor)
            }
        }
        .transition(.move(edge: .bottom))
        .zIndex(1)
        .padding(.bottom, 8)
    }
}

struct VisualControls_Previews: PreviewProvider {
    static var previews: some View {
        VisualControls()
    }
}
