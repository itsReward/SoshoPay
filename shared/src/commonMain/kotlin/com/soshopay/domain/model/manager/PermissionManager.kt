package com.soshopay.domain.manager

import com.soshopay.domain.model.AppPermission
import com.soshopay.domain.model.PermissionState

/**
 * Platform-agnostic interface for managing runtime permissions.
 *
 * This interface follows the Dependency Inversion Principle by providing
 * an abstraction for permission management that can be implemented differently
 * on each platform (Android, iOS).
 *
 * Implementations handle:
 * - Checking permission status
 * - Requesting permissions from users
 * - Determining if rationale should be shown
 * - Opening app settings for manual permission grants
 */
interface PermissionManager {
    /**
     * Checks if a specific permission is currently granted.
     *
     * @param permission The permission to check
     * @return true if the permission is granted, false otherwise
     */
    suspend fun isPermissionGranted(permission: AppPermission): Boolean

    /**
     * Checks if multiple permissions are currently granted.
     *
     * @param permissions List of permissions to check
     * @return Map of permissions to their granted status
     */
    suspend fun arePermissionsGranted(permissions: List<AppPermission>): Map<AppPermission, Boolean>

    /**
     * Gets the current state of a permission.
     *
     * @param permission The permission to check
     * @return PermissionState containing detailed permission status
     */
    suspend fun getPermissionState(permission: AppPermission): PermissionState

    /**
     * Checks if we should show a rationale for requesting the permission.
     * This is typically true when the user has denied the permission once.
     *
     * @param permission The permission to check
     * @return true if rationale should be shown, false otherwise
     */
    suspend fun shouldShowRationale(permission: AppPermission): Boolean

    /**
     * Opens the app's settings page where users can manually grant permissions.
     * This is useful when a permission has been permanently denied.
     */
    suspend fun openAppSettings()

    /**
     * Gets the appropriate permissions needed for file/document access
     * based on the current Android version.
     *
     * @return List of permissions needed for file access on this device
     */
    fun getStoragePermissions(): List<AppPermission>

    /**
     * Gets permissions needed specifically for image access.
     *
     * @return List of permissions needed for image access
     */
    fun getImagePermissions(): List<AppPermission>

    /**
     * Checks if all storage permissions are granted.
     *
     * @return true if all storage permissions are granted
     */
    suspend fun hasStoragePermission(): Boolean

    /**
     * Checks if all image permissions are granted.
     *
     * @return true if all image permissions are granted
     */
    suspend fun hasImagePermission(): Boolean
}
