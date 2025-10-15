package com.soshopay.domain.model

import kotlinx.serialization.Serializable

/**
 * Represents the state of a permission request.
 *
 * Following SOLID principles:
 * - Single Responsibility: Manages permission state only
 * - Open/Closed: Can be extended for additional permission states
 *
 * @property isGranted Whether the permission is currently granted
 * @property shouldShowRationale Whether to show permission rationale to the user
 * @property isPermanentlyDenied Whether the user has permanently denied the permission
 */
@Serializable
data class PermissionState(
    val isGranted: Boolean = false,
    val shouldShowRationale: Boolean = false,
    val isPermanentlyDenied: Boolean = false,
) {
    /**
     * Checks if we need to show permission rationale or settings dialog
     */
    fun needsUserAction(): Boolean = !isGranted && (shouldShowRationale || isPermanentlyDenied)

    /**
     * Checks if permission can be requested again
     */
    fun canRequest(): Boolean = !isGranted && !isPermanentlyDenied
}

/**
 * Enum representing different types of permissions the app needs.
 */
enum class AppPermission(
    val description: String,
) {
    READ_MEDIA_IMAGES("Access to photos and images"),
    READ_MEDIA_VIDEO("Access to videos"),
    READ_MEDIA_DOCUMENTS("Access to documents and PDFs"), // ADDED for PDF support
    READ_MEDIA_VISUAL_USER_SELECTED("Access to selected media"),
    READ_EXTERNAL_STORAGE("Access to files and documents"),
    CAMERA("Access to camera"),
    ;

    /**
     * Gets user-friendly permission name
     */
    fun getDisplayName(): String =
        when (this) {
            READ_MEDIA_IMAGES -> "Photos"
            READ_MEDIA_VIDEO -> "Videos"
            READ_MEDIA_DOCUMENTS -> "Documents"
            READ_MEDIA_VISUAL_USER_SELECTED -> "Selected Media"
            READ_EXTERNAL_STORAGE -> "Files"
            CAMERA -> "Camera"
        }
}

/**
 * Result of a permission request operation.
 */
sealed class PermissionResult {
    /**
     * All requested permissions were granted
     */
    data object Granted : PermissionResult()

    /**
     * One or more permissions were denied
     * @property deniedPermissions List of permissions that were denied
     * @property permanentlyDenied List of permissions that were permanently denied
     */
    data class Denied(
        val deniedPermissions: List<AppPermission>,
        val permanentlyDenied: List<AppPermission> = emptyList(),
    ) : PermissionResult()

    /**
     * User needs to be shown a rationale for the permission
     * @property permissions Permissions that need rationale
     */
    data class ShowRationale(
        val permissions: List<AppPermission>,
    ) : PermissionResult()
}
