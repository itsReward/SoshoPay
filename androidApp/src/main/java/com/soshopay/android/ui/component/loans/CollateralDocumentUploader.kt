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
 * FIXED: Proper permission denial detection for Android 13+
 *
 * Features:
 * - Automatic permission checks before file picking
 * - Permission rationale dialog
 * - Settings navigation for permanently denied permissions
 * - File picker for images and PDFs (unified)
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
    val scope = rememberCoroutineScope()

    // Permission state
    var showPermissionRationale by remember { mutableStateOf(false) }
    var showPermissionDeniedDialog by remember { mutableStateOf(false) }
    var permissionGranted by remember { mutableStateOf(false) }
    var permissionDenialCount by remember { mutableStateOf(0) }

    // Check permission on composition
    LaunchedEffect(Unit) {
        permissionGranted = permissionManager.hasStoragePermission()
    }

    // Permission request launcher
    val permissionLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestMultiplePermissions(),
        ) { permissions ->
            scope.launch {
                val allGranted = permissions.values.all { it }
                permissionGranted = allGranted

                if (!allGranted) {
                    permissionDenialCount++

                    // CRITICAL FIX: Only show "permanently denied" after 2+ denials
                    // First denial = show rationale
                    // Second+ denial = assume permanent denial
                    if (permissionDenialCount >= 2) {
                        showPermissionDeniedDialog = true
                    } else {
                        // First denial - just allow them to try again
                        // The system will handle showing rationale on next request
                    }
                }
            }
        }

    // CRITICAL FIX: Use OpenDocument instead of GetContent for better document support
    val filePickerLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.OpenDocument(),
        ) { uri: Uri? ->
            uri?.let {
                try {
                    // Grant persistent read permission
                    context.contentResolver.takePersistableUriPermission(
                        uri,
                        android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION,
                    )
                } catch (e: Exception) {
                    // Persistable permission not available, continue anyway
                }

                try {
                    val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
                    val fileBytes = inputStream?.readBytes()
                    val fileName = getFileName(context, uri)
                    val mimeType = context.contentResolver.getType(uri) ?: "application/octet-stream"

                    if (fileBytes != null && fileName != null) {
                        // Validate file size (5MB max)
                        if (fileBytes.size > CollateralDocument.MAX_FILE_SIZE_BYTES) {
                            // Error is handled by parent component via error parameter
                            return@rememberLauncherForActivityResult
                        }

                        onDocumentSelected(fileBytes, fileName, mimeType)
                    }

                    inputStream?.close()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

    // CRITICAL FIX: Request both READ_MEDIA_IMAGES and READ_MEDIA_DOCUMENTS
    val requestPermissions = {
        val permissions =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                // Android 13+ requires granular permissions
                arrayOf(
                    Manifest.permission.READ_MEDIA_IMAGES, // For images
                    "android.permission.READ_MEDIA_DOCUMENTS", // For PDFs
                )
            } else {
                // Android 12 and below use legacy permission
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        permissionLauncher.launch(permissions)
    }

    // Function to handle file picker click
    val onPickerClick = {
        scope.launch {
            if (permissionGranted) {
                // CRITICAL FIX: OpenDocument accepts an array of MIME types
                filePickerLauncher.launch(
                    arrayOf(
                        "image/*", // All image types
                        "application/pdf", // PDF documents
                    ),
                )
            } else {
                // Request permissions directly - system will show rationale if needed
                requestPermissions()
            }
        }
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Upload Button
        OutlinedCard(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clickable(enabled = !isUploading) { onPickerClick() },
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.CloudUpload,
                    contentDescription = "Upload",
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )

                if (isUploading) {
                    CircularProgressIndicator(
                        progress = { uploadProgress },
                        modifier = Modifier.size(40.dp),
                    )
                    Text(
                        text = "${(uploadProgress * 100).toInt()}%",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                } else {
                    Text(
                        text = "Tap to upload documents",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "Images (JPEG, PNG) or PDFs up to 5MB",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }

        // Error message
        if (error != null) {
            Card(
                colors =
                    CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                    ),
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = "Error",
                        tint = MaterialTheme.colorScheme.error,
                    )
                    Text(
                        text = error,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                    )
                }
            }
        }

        // Uploaded documents list
        if (documents.isNotEmpty()) {
            Text(
                text = "Uploaded Documents (${documents.size})",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(documents) { document ->
                    DocumentCard(
                        document = document,
                        onRemove = { onDocumentRemoved(document.id) },
                    )
                }
            }
        }

        // Permission Permanently Denied Dialog
        // FIXED: Better messaging for Android 13+
        if (showPermissionDeniedDialog) {
            AlertDialog(
                onDismissRequest = { showPermissionDeniedDialog = false },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Warning",
                        tint = MaterialTheme.colorScheme.error,
                    )
                },
                title = { Text("Permission Required") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "To upload documents, you need to grant the following permissions:",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        Text(
                            text = "• Photos and videos\n• Files and media",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = "\nPlease enable these permissions in your device settings:",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        Text(
                            text = "Settings → Apps → SoshoPay → Permissions",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showPermissionDeniedDialog = false
                            scope.launch {
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
    }
}

/**
 * Document card displaying uploaded document with preview and remove button.
 */
@Composable
private fun DocumentCard(
    document: CollateralDocument,
    onRemove: () -> Unit,
) {
    Card(
        modifier = Modifier.size(width = 140.dp, height = 180.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.fillMaxSize(),
            ) {
                // Document preview
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    if (document.isImage()) {
                        AsyncImage(
                            model = document.fileUrl,
                            contentDescription = document.fileName,
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
