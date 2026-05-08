package com.dcengineer.twolinks

import platform.UIKit.UIView

interface IosSceneProvider {
    fun createView(viewModel: MainViewModel): UIView
    fun update()
}
