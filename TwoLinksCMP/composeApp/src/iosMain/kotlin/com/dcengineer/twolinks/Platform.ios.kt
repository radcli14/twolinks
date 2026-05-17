package com.dcengineer.twolinks

import platform.ARKit.ARWorldTrackingConfiguration
import platform.UIKit.UIDevice

class IOSPlatform: Platform {
    override val name: String = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
    override val isARAvailable: Boolean = ARWorldTrackingConfiguration.isSupported
}

actual fun getPlatform(): Platform = IOSPlatform()