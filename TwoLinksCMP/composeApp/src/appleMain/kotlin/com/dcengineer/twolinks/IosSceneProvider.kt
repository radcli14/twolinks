package com.dcengineer.twolinks

import platform.UIKit.UIView

interface IosSceneProvider {
    fun createView(): UIView
    fun updateTransforms(
        l1Deg: Float, l2Deg: Float,
        pivotX: Float, pivotY: Float, pivotZ: Float,
        l1CenterX: Float, l1CenterY: Float, l1CenterZ: Float,
        l1SizeX: Float, l1SizeY: Float, l1SizeZ: Float,
        l2CenterX: Float, l2CenterY: Float, l2CenterZ: Float,
        l2SizeX: Float, l2SizeY: Float, l2SizeZ: Float
    )
    fun updateColors(r1: Float, g1: Float, b1: Float, r2: Float, g2: Float, b2: Float)
}
