package com.dcengineer.twolinks

interface Platform {
    val name: String
    val hasBottomSafeArea: Boolean get() = false
}

expect fun getPlatform(): Platform