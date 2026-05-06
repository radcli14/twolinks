//
//  SwiftyTwoLinksApp.swift
//  SwiftyTwoLinks
//
//  Created by Eliott Radcliffe on 5/8/22.
//

import SwiftUI

@main
struct SwiftyTwoLinksApp: App {
    let viewController = ContentViewController()
    
    var body: some Scene {
        WindowGroup {
            ContentView(viewController: viewController)
        }
    }
}
