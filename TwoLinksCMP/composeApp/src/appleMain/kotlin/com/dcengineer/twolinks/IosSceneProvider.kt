package com.dcengineer.twolinks

import platform.UIKit.UIView

interface IosSceneProvider {
    fun createView(): UIView
    fun updateTransforms(l1: Float, l2: Float, px: Float, py: Float, pz: Float)
    fun updateColors(r1: Float, g1: Float, b1: Float, r2: Float, g2: Float, b2: Float)
}
