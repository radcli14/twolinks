package com.dcengineer.twolinks

import android.os.Build

class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
    override val isARAvailable: Boolean = true
}

actual fun getPlatform(): Platform = AndroidPlatform()