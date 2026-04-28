package com.dcengineer.twolinks

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform