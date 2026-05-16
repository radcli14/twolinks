package com.dcengineer.twolinks

import platform.UIKit.UIViewController

object IosSceneRegistry {
    var viewController: UIViewController? = null
    var onUpdate: ((MainViewModel) -> Unit)? = null
}
