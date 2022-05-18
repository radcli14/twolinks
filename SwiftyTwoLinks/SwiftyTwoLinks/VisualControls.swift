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
    var onChange: () -> Void
    
    var body: some View {
        VStack {
            Text(heading)
            ColorPicker("Color", selection: $color).padding(.horizontal, 12)
        }
        .frame(width: 128)
        .background(Color.white.opacity(0.7))
        .clipShape(RoundedRectangle(cornerRadius: 8))
        .onChange(of: color) { _ in
            onChange()
        }
    }
}

struct VisualControls: View {
    let viewController: ContentViewController
    @State var linkOneColor = Color.gray
    @State var linkTwoColor = Color.gray
    @State var monolithColor = Color.black
    @State var moonColor = Color.gray
    
    var body: some View {
        VStack {
            HStack {
                // Control visuals for the first link
                LinkVisualControl(heading: "Link One", color: $linkOneColor) {
                    viewController.twoLinks.linkOneColor = linkOneColor.uiColor()
                }
                
                // Control visuals for the second link
                LinkVisualControl(heading: "Link Two", color: $linkTwoColor) {
                    viewController.twoLinks.linkTwoColor = linkTwoColor.uiColor()
                }
            }
            HStack {
                // Control visuals for the big black door-looking thing
                LinkVisualControl(heading: "Monolith", color: $monolithColor) { }
                
                // Control visuals for the ground
                LinkVisualControl(heading: "Moon", color: $moonColor) { }
            }
        }
        .transition(.move(edge: .bottom))
        .zIndex(1)
        .padding(.bottom, 8)
    }
}

/**
 I didn't make this ... not sure I trust it
 */
extension Color {
 
    func uiColor() -> UIColor {

        if #available(iOS 14.0, *) {
            return UIColor(self)
        }

        let components = self.components()
        return UIColor(red: components.r, green: components.g, blue: components.b, alpha: components.a)
    }

    private func components() -> (r: CGFloat, g: CGFloat, b: CGFloat, a: CGFloat) {

        let scanner = Scanner(string: self.description.trimmingCharacters(in: CharacterSet.alphanumerics.inverted))
        var hexNumber: UInt64 = 0
        var r: CGFloat = 0.0, g: CGFloat = 0.0, b: CGFloat = 0.0, a: CGFloat = 0.0

        let result = scanner.scanHexInt64(&hexNumber)
        if result {
            r = CGFloat((hexNumber & 0xff000000) >> 24) / 255
            g = CGFloat((hexNumber & 0x00ff0000) >> 16) / 255
            b = CGFloat((hexNumber & 0x0000ff00) >> 8) / 255
            a = CGFloat(hexNumber & 0x000000ff) / 255
        }
        return (r, g, b, a)
    }
}

/*struct VisualControls_Previews: PreviewProvider {
    static var previews: some View {
        VisualControls()
    }
}*/
