package com.soshopay.domain.util

/**
 * Utility object for device-specific operations in the SoshoPay domain.
 *
 * This expect object provides platform-specific implementations for generating a unique device ID
 * and retrieving the platform name. Actual implementations should be provided in each platform source set
 * (e.g., Android, iOS) to ensure correct behavior across different environments.
 *
 * Usage:
 * - Use [generateDeviceId] to obtain a unique identifier for the current device.
 * - Use [getPlatformName] to retrieve the name of the platform (e.g., "Android", "iOS").
 */
expect object DeviceUtils {
    /**
     * Generates a unique identifier for the current device.
     * The implementation is platform-specific and may use hardware, OS, or app-specific information.
     *
     * @return A unique device ID as a [String].
     */
    fun generateDeviceId(): String

    /**
     * Returns the name of the current platform (e.g., "Android", "iOS").
     *
     * @return The platform name as a [String].
     */
    fun getPlatformName(): String
}
