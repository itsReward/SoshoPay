package com.soshopay.domain.util

import android.content.Context
import android.os.Build
import android.provider.Settings

actual object DeviceUtils {
    private lateinit var appContext: Context

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    /**
     * Generates a unique device ID using Android's ANDROID_ID.
     * @param context The Android context required to access the ContentResolver.
     * @return A string representing the device ID.
     */
    actual fun generateDeviceId(): String = Settings.Secure.getString(appContext.contentResolver, Settings.Secure.ANDROID_ID)

    /**
     * Returns the platform name and version.
     * @return A string representing the Android platform and SDK version.
     */
    actual fun getPlatformName(): String = "Android ${Build.VERSION.SDK_INT}"
}
