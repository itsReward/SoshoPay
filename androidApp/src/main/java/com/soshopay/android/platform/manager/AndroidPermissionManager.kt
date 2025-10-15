package com.soshopay.platform.manager

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.ContextCompat
import com.soshopay.domain.manager.PermissionManager
import com.soshopay.domain.model.AppPermission
import com.soshopay.domain.model.PermissionState
import com.soshopay.domain.util.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Android-specific implementation of PermissionManager.
 *
 * This implementation handles Android's complex permission system across different
 * API levels, including the transition from READ_EXTERNAL_STORAGE to granular
 * media permissions (READ_MEDIA_IMAGES, READ_MEDIA_VIDEO, READ_MEDIA_DOCUMENTS) in Android 13+.
 *
 * Key features:
 * - API level-aware permission handling
 * - Support for Android 6.0 (API 23) through Android 16 (API 35)
 * - Graceful degradation for older Android versions
 * - Comprehensive error handling and logging
 * - Document (PDF) support for Android 13+
 *
 * @property context Application context for permission checks
 */
class AndroidPermissionManager(
    private val context: Context,
) : PermissionManager {
    companion object {
        private const val TAG = "PERMISSION_MANAGER"
    }

    /**
     * Maps AppPermission enum to Android system permission strings.
     * Returns appropriate permission based on API level.
     */
    private fun AppPermission.toAndroidPermission(): String? =
        when (this) {
            AppPermission.READ_MEDIA_IMAGES -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    Manifest.permission.READ_MEDIA_IMAGES
                } else {
                    Manifest.permission.READ_EXTERNAL_STORAGE
                }
            }
            AppPermission.READ_MEDIA_VIDEO -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    Manifest.permission.READ_MEDIA_VIDEO
                } else {
                    Manifest.permission.READ_EXTERNAL_STORAGE
                }
            }
            // CRITICAL FIX: Add support for READ_MEDIA_DOCUMENTS (Android 13+)
            AppPermission.READ_MEDIA_DOCUMENTS -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    // READ_MEDIA_DOCUMENTS is available from API 33 (Android 13)
                    "android.permission.READ_MEDIA_DOCUMENTS"
                } else {
                    Manifest.permission.READ_EXTERNAL_STORAGE
                }
            }
            AppPermission.READ_MEDIA_VISUAL_USER_SELECTED -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
                } else {
                    null // Not available on older versions
                }
            }
            AppPermission.READ_EXTERNAL_STORAGE -> Manifest.permission.READ_EXTERNAL_STORAGE
            AppPermission.CAMERA -> Manifest.permission.CAMERA
        }

    override suspend fun isPermissionGranted(permission: AppPermission): Boolean =
        withContext(Dispatchers.IO) {
            val androidPermission = permission.toAndroidPermission()
            if (androidPermission == null) {
                Logger.d("Permission $permission not available on this API level", TAG)
                return@withContext true // Permission not needed on this API level
            }

            val granted =
                ContextCompat.checkSelfPermission(
                    context,
                    androidPermission,
                ) == PackageManager.PERMISSION_GRANTED

            Logger.d("Permission $permission ($androidPermission) granted: $granted", TAG)
            granted
        }

    override suspend fun arePermissionsGranted(permissions: List<AppPermission>): Map<AppPermission, Boolean> =
        withContext(Dispatchers.IO) {
            permissions.associateWith { permission ->
                isPermissionGranted(permission)
            }
        }

    override suspend fun getPermissionState(permission: AppPermission): PermissionState =
        withContext(Dispatchers.IO) {
            val isGranted = isPermissionGranted(permission)
            val shouldShowRationale =
                if (!isGranted) {
                    shouldShowRationale(permission)
                } else {
                    false
                }

            PermissionState(
                isGranted = isGranted,
                shouldShowRationale = shouldShowRationale,
                isPermanentlyDenied = !isGranted && !shouldShowRationale,
            )
        }

    override suspend fun shouldShowRationale(permission: AppPermission): Boolean =
        withContext(Dispatchers.IO) {
            val androidPermission = permission.toAndroidPermission() ?: return@withContext false

            // Note: This method is primarily used by Activities/Fragments
            // In this context, we return false as the actual check is done
            // by the Activity using ActivityCompat.shouldShowRequestPermissionRationale
            false
        }

    override suspend fun openAppSettings() {
        withContext(Dispatchers.Main) {
            try {
                val intent =
                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", context.packageName, null)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                context.startActivity(intent)
                Logger.i("Opened app settings", TAG)
            } catch (e: Exception) {
                Logger.e("Failed to open app settings", TAG, e)
                // Fallback to general settings
                try {
                    val intent =
                        Intent(Settings.ACTION_SETTINGS).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                    context.startActivity(intent)
                } catch (fallbackException: Exception) {
                    Logger.e("Failed to open settings fallback", TAG, fallbackException)
                }
            }
        }
    }

    /**
     * CRITICAL FIX: Updated to include READ_MEDIA_DOCUMENTS for PDF support
     */
    override fun getStoragePermissions(): List<AppPermission> =
        when {
            // Android 13+ (API 33+) - Use granular media permissions
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                listOf(
                    AppPermission.READ_MEDIA_IMAGES, // For images (JPEG, PNG)
                    AppPermission.READ_MEDIA_DOCUMENTS, // For PDFs and documents
                )
            }
            // Android 6.0 - Android 12 (API 23-32)
            else -> {
                listOf(AppPermission.READ_EXTERNAL_STORAGE)
            }
        }

    override fun getImagePermissions(): List<AppPermission> =
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                listOf(AppPermission.READ_MEDIA_IMAGES)
            }
            else -> {
                listOf(AppPermission.READ_EXTERNAL_STORAGE)
            }
        }

    override suspend fun hasStoragePermission(): Boolean {
        val permissions = getStoragePermissions()
        val results = permissions.all { isPermissionGranted(it) }
        Logger.d("Storage permissions check: $results for permissions: $permissions", TAG)
        return results
    }

    override suspend fun hasImagePermission(): Boolean {
        val permissions = getImagePermissions()
        return permissions.all { isPermissionGranted(it) }
    }
}
