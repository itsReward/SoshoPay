package com.soshopay.android.ui.component.common

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Reusable permission rationale dialog.
 *
 * @param showDialog Whether to show the dialog
 * @param onDismiss Callback when dialog is dismissed
 * @param onConfirm Callback when user confirms to grant permission
 * @param title Dialog title
 * @param message Dialog message explaining why permission is needed
 * @param icon Optional icon for the dialog
 */
@Composable
fun PermissionRationaleDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    title: String = "Permission Required",
    message: String,
    icon: ImageVector = Icons.Default.Folder,
) {
    if (showDialog) {
        AlertDialog(
            onDismissRequest = onDismiss,
            icon = {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
            },
            title = {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                )
            },
            text = {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                )
            },
            confirmButton = {
                TextButton(onClick = onConfirm) {
                    Text("Grant Permission")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            },
        )
    }
}

/**
 * Reusable permission denied dialog (navigates to settings).
 *
 * @param showDialog Whether to show the dialog
 * @param onDismiss Callback when dialog is dismissed
 * @param onOpenSettings Callback to open app settings
 * @param title Dialog title
 * @param message Dialog message
 */
@Composable
fun PermissionDeniedDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    onOpenSettings: () -> Unit,
    title: String = "Permission Denied",
    message: String = "This permission is required. Please enable it in settings.",
) {
    if (showDialog) {
        AlertDialog(
            onDismissRequest = onDismiss,
            icon = {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                )
            },
            title = {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                )
            },
            text = {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                )
            },
            confirmButton = {
                TextButton(onClick = onOpenSettings) {
                    Text("Open Settings")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            },
        )
    }
}
