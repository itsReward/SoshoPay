package com.soshopay.android.ui.component.loans

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
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
import com.soshopay.domain.model.CollateralDocument
import java.io.InputStream

/**
 * Collateral Document Uploader Component.
 *
 * Features:
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
) {
    val context = LocalContext.current

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

    Column(modifier = modifier) {
        // Upload button
        OutlinedCard(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clickable(enabled = !isUploading) {
                        filePickerLauncher.launch("image/*,application/pdf")
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
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                if (isUploading) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        CircularProgressIndicator(
                            progress = uploadProgress,
                            modifier = Modifier.size(40.dp),
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Uploading... ${(uploadProgress * 100).toInt()}%",
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudUpload,
                            contentDescription = "Upload",
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.primary,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Tap to upload collateral documents",
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
