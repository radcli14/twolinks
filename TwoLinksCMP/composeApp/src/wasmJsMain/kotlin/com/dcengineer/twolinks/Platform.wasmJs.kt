@file:OptIn(ExperimentalWasmJsInterop::class)
package com.dcengineer.twolinks

import kotlin.js.ExperimentalWasmJsInterop
import kotlin.js.JsFun

@JsFun("() => { const ua = window.navigator.userAgent.toLowerCase(); return (ua.includes('iphone') || ua.includes('ipad') || ua.includes('ipod')) && ua.includes('safari') && !ua.includes('chrome') && !ua.includes('crios'); }")
external fun isIosSafari(): Boolean

class WasmPlatform: Platform {
    override val name: String = "Web with Kotlin/Wasm"
    override val hasBottomSafeArea: Boolean
        get() = isIosSafari()
}

actual fun getPlatform(): Platform = WasmPlatform()