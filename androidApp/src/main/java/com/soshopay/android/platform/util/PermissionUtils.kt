package com.soshopay.android.platform.util

import android.Manifest
import android.os.Build

/**
 * Utility object for permission-related operations.
 *
 * Provides helper functions for determining required permissions
 * based on Android version and use case.
 */
object PermissionUtils {
    /**
     * Gets the required permissions for accessing images based on Android version.
     *
     * @return Array of permission strings needed for image access
     */
    fun getImagePermissions(): Array<String> =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

    /**
     * Gets the required permissions for accessing videos based on Android version.
     *
     * @return Array of permission strings needed for video access
     */
    fun getVideoPermissions(): Array<String> =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(Manifest.permission.READ_MEDIA_VIDEO)
        } else {
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

    /**
     * Gets the required permissions for accessing images and videos.
     *
     * @return Array of permission strings needed for media access
     */
    fun getMediaPermissions(): Array<String> =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO,
            )
        } else {
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

    /**
     * Gets all storage-related permissions based on Android version.
     * This includes images, videos, and documents.
     *
     * @return Array of permission strings needed for full storage access
     */
    fun getAllStoragePermissions(): Array<String> =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO,
                // Note: READ_MEDIA_AUDIO can be added if you need audio files
            )
        } else {
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

    /**
     * Gets camera permission.
     *
     * @return Array containing camera permission
     */
    fun getCameraPermission(): Array<String> = arrayOf(Manifest.permission.CAMERA)

    /**
     * Checks if the current Android version requires granular media permissions.
     *
     * @return true if running on Android 13 or higher
     */
    fun requiresGranularPermissions(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU

    /**
     * Gets a user-friendly description of why a permission is needed.
     *
     * @param permission The Android permission string
     * @return User-friendly description
     */
    fun getPermissionRationale(permission: String): String =
        when (permission) {
            Manifest.permission.READ_MEDIA_IMAGES ->
                "We need access to your photos to allow you to upload images as collateral documents."

            Manifest.permission.READ_MEDIA_VIDEO ->
                "We need access to your videos to allow you to upload video files as collateral documents."

            Manifest.permission.READ_EXTERNAL_STORAGE ->
                "We need access to your files to allow you to upload documents and images."

            Manifest.permission.CAMERA ->
                "We need camera access to allow you to take photos of your collateral."

            else -> "This permission is required for the app to function properly."
        }
}
