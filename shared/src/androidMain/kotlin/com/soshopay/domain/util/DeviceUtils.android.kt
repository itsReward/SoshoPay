package com.soshopay.domain.util

import android.os.Build
import java.util.UUID

actual object DeviceUtils {
    actual fun generateDeviceId(): String = "android-${UUID.randomUUID().toString().take(8)}"

    actual fun getPlatformName(): String = "Android ${Build.VERSION.SDK_INT}"
}
