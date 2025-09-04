package com.soshopay.platform

expect interface Platform {
    val name: String
}

expect fun getPlatform(): Platform