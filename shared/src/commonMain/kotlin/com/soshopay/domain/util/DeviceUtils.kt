package com.soshopay.domain.util

expect object DeviceUtils {
    fun generateDeviceId(): String

    fun getPlatformName(): String
}
