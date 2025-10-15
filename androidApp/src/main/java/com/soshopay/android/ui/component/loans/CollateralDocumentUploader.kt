package com.soshopay.android.ui.component.loans

import android.Manifest
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.soshopay.domain.manager.PermissionManager
import com.soshopay.domain.model.CollateralDocument
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import java.io.InputStream

/**
 * Enhanced Collateral Document Uploader Component with Permission Handling.
 *
 * Features:
 * - Automatic permission checks before file picking
 * - Permission rationale dialog
 * - Settings navigation for permanently denied permissions
 * - File picker for images and PDFs
 * - Upload progress indicator
 * - List of uploaded documents with preview
 * - Remove document functionality
 * - File size and type validation
 * - Thumbnails for images, PDF icon for PDFs
 *
 * @param documents List of uploaded documents
 * @param onDocumentSelected Callback when a document is selected for upload
 * @param onDocumentRemoved Callback when a document is removed
 * @param isUploading Whether a document is currently uploading
 * @param uploadProgress Upload progress (0.0 to 1.0)
 * @param error Error message to display
 * @param modifier Modifier for customization
 * @param permissionManager Permission manager for handling runtime permissions
 */
@Composable
fun CollateralDocumentUploader(
    documents: List<CollateralDocument>,
    onDocumentSelected: (ByteArray, String, String) -> Unit,
    onDocumentRemoved: (String) -> Unit,
    isUploading: Boolean = false,
    uploadProgress: Float = 0f,
    error: String? = null,
    modifier: Modifier = Modifier,
    permissionManager: PermissionManager = koinInject(),
) {
    val context = LocalContext.current

    // Permission state
    var showPermissionRationale by remember { mutableStateOf(false) }
    var showPermissionDeniedDialog by remember { mutableStateOf(false) }
    var permissionGranted by remember { mutableStateOf(false) }

    // Check permission on composition
    LaunchedEffect(Unit) {
        permissionGranted = permissionManager.hasStoragePermission()
    }

    // Permission request launcher
    val permissionLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestMultiplePermissions(),
        ) { permissions ->
            val allGranted = permissions.values.all { it }
            permissionGranted = allGranted

            if (!allGranted) {
                // Check if any permission was permanently denied
                val anyPermanentlyDenied =
                    permissions.any { (permission, granted) ->
                        !granted &&
                            !androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale(
                                context as androidx.activity.ComponentActivity,
                                permission,
                            )
                    }

                if (anyPermanentlyDenied) {
                    showPermissionDeniedDialog = true
                }
            }
        }

    // File picker launcher
    val filePickerLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent(),
        ) { uri: Uri? ->
            uri?.let {
                try {
                    val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
                    val fileBytes = inputStream?.readBytes()
                    val fileName = getFileName(context, uri)
                    val mimeType = context.contentResolver.getType(uri) ?: "application/octet-stream"

                    if (fileBytes != null && fileName != null) {
                        // Validate file size (5MB max)
                        if (fileBytes.size > CollateralDocument.MAX_FILE_SIZE_BYTES) {
                            // Show error via callback or toast
                            return@rememberLauncherForActivityResult
                        }

                        onDocumentSelected(fileBytes, fileName, mimeType)
                    }

                    inputStream?.close()
                } catch (e: Exception) {
                    // Handle error
                    e.printStackTrace()
                }
            }
        }

    // Function to request permissions
    val requestPermissions = {
        val permissions =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                arrayOf(
                    Manifest.permission.READ_MEDIA_IMAGES,
                    // Add READ_MEDIA_VIDEO if you need video support
                )
            } else {
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        permissionLauncher.launch(permissions)
    }

    // Function to handle file picker click
    val onPickerClick = {
        if (permissionGranted) {
            filePickerLauncher.launch("image/*,application/pdf")
        } else {
            // Check if we should show rationale
            val shouldShowRationale =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale(
                        context as androidx.activity.ComponentActivity,
                        Manifest.permission.READ_MEDIA_IMAGES,
                    )
                } else {
                    androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale(
                        context as androidx.activity.ComponentActivity,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                    )
                }

            if (shouldShowRationale) {
                showPermissionRationale = true
            } else {
                requestPermissions()
            }
        }
    }

    Column(modifier = modifier) {
        // Permission Rationale Dialog
        if (showPermissionRationale) {
            AlertDialog(
                onDismissRequest = { showPermissionRationale = false },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Folder,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                },
                title = {
                    Text(
                        text = "Storage Permission Required",
                        style = MaterialTheme.typography.headlineSmall,
                    )
                },
                text = {
                    Text(
                        text =
                            "To upload collateral documents, we need access to your photos and files. " +
                                "This allows you to select images and PDFs from your device to support your loan application.",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showPermissionRationale = false
                            requestPermissions()
                        },
                    ) {
                        Text("Grant Permission")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showPermissionRationale = false },
                    ) {
                        Text("Cancel")
                    }
                },
            )
        }

        // Permission Denied Dialog (Navigate to Settings)
        if (showPermissionDeniedDialog) {
            AlertDialog(
                onDismissRequest = { showPermissionDeniedDialog = false },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                    )
                },
                title = {
                    Text(
                        text = "Permission Denied",
                        style = MaterialTheme.typography.headlineSmall,
                    )
                },
                text = {
                    Text(
                        text =
                            "Storage permission is required to upload documents. " +
                                "Please enable it in your device settings to continue.",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showPermissionDeniedDialog = false
                            kotlinx.coroutines.GlobalScope.launch {
                                permissionManager.openAppSettings()
                            }
                        },
                    ) {
                        Text("Open Settings")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showPermissionDeniedDialog = false },
                    ) {
                        Text("Cancel")
                    }
                },
            )
        }

        // Upload button
        OutlinedCard(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clickable(enabled = !isUploading) {
                        onPickerClick()
                    },
            border =
                BorderStroke(
                    width = 2.dp,
                    color =
                        if (error != null) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.outline
                        },
                ),
            colors =
                CardDefaults.cardColors(
                    containerColor = Color.Transparent,
                ),
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                if (isUploading) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        CircularProgressIndicator(
                            progress = { uploadProgress },
                            modifier = Modifier.size(40.dp),
                        )
                        Text(
                            text = "Uploading... ${(uploadProgress * 100).toInt()}%",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudUpload,
                            contentDescription = "Upload",
                            modifier = Modifier.size(40.dp),
                            tint = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text =
                                if (!permissionGranted) {
                                    "Tap to grant permission"
                                } else {
                                    "Tap to upload documents"
                                },
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center,
                        )
                        Text(
                            text = "Images or PDFs (Max 5MB)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
        }

        // Error message
        if (error != null) {
            Text(
                text = error,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 4.dp, start = 16.dp),
            )
        }

        // Uploaded documents
        if (documents.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Uploaded Documents (${documents.size})",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(documents) { document ->
                    DocumentPreviewCard(
                        document = document,
                        onRemove = { onDocumentRemoved(document.id) },
                    )
                }
            }
        }
    }
}

/**
 * Preview card for a single document.
 */
@Composable
private fun DocumentPreviewCard(
    document: CollateralDocument,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier =
            modifier
                .width(120.dp)
                .height(140.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                // Document preview
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                            .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    if (document.isImage()) {
                        AsyncImage(
                            model = document.fileUrl,
                            contentDescription = "Document preview",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                        )
                    } else if (document.isPdf()) {
                        Icon(
                            imageVector = Icons.Default.PictureAsPdf,
                            contentDescription = "PDF",
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.error,
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = "Document",
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                }

                // Document info
                Column(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                ) {
                    Text(
                        text = document.fileName,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        fontWeight = FontWeight.Medium,
                    )
                    Text(
                        text = document.getFormattedFileSize(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            // Remove button
            IconButton(
                onClick = onRemove,
                modifier =
                    Modifier
                        .align(Alignment.TopEnd)
                        .size(32.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Remove",
                    modifier = Modifier.size(20.dp),
                    tint = Color.White,
                )
            }
        }
    }
}

/**
 * Helper function to get file name from URI.
 */
private fun getFileName(
    context: android.content.Context,
    uri: Uri,
): String? {
    var fileName: String? = null
    val cursor = context.contentResolver.query(uri, null, null, null, null)
    cursor?.use {
        if (it.moveToFirst()) {
            val displayNameIndex = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
            if (displayNameIndex != -1) {
                fileName = it.getString(displayNameIndex)
            }
        }
    }
    return fileName ?: uri.lastPathSegment
}
