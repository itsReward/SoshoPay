package com.soshopay.domain.model

import kotlinx.serialization.Serializable

/**
 * Represents a document or photo uploaded as collateral evidence for a cash loan.
 *
 * Following SOLID principles:
 * - Single Responsibility: Manages collateral document data only
 * - Open/Closed: Can be extended without modification
 *
 * @property id Unique identifier for the document
 * @property fileName Original file name with extension
 * @property fileUrl Remote URL where the document is stored
 * @property fileSize Size of the file in bytes
 * @property fileType MIME type of the file (e.g., "image/jpeg", "application/pdf")
 * @property uploadedAt Timestamp when the document was uploaded (milliseconds since epoch)
 * @property isVerified Whether the document has been verified by admin
 */
@Serializable
data class CollateralDocument(
    val id: String,
    val fileName: String,
    val fileUrl: String,
    val fileSize: Long,
    val fileType: String,
    val uploadedAt: Long,
    val isVerified: Boolean = false
) {
    /**
     * Gets the file size in a human-readable format
     * @return Formatted file size (e.g., "2.5 MB", "150 KB")
     */
    fun getFormattedFileSize(): String {
        val kb = fileSize / 1024.0
        val mb = kb / 1024.0

        return when {
            mb >= 1.0 -> "${(mb * 100).toLong() / 100.0} MB"
            kb >= 1.0 -> "${(kb * 100).toLong() / 100.0} KB"
            else -> "$fileSize B"
        }
    }

    /**
     * Checks if the document is an image
     * @return true if the file type indicates an image
     */
    fun isImage(): Boolean {
        return fileType.startsWith("image/", ignoreCase = true)
    }

    /**
     * Checks if the document is a PDF
     * @return true if the file type indicates a PDF
     */
    fun isPdf(): Boolean {
        return fileType.equals("application/pdf", ignoreCase = true)
    }

    /**
     * Gets a safe file extension from the file name
     * @return File extension (lowercase) or empty string if none found
     */
    fun getFileExtension(): String {
        return fileName.substringAfterLast('.', "").lowercase()
    }

    /**
     * Validates that the document is acceptable for collateral
     * @return ValidationResult with any errors
     */
    fun validate(): ValidationResult {
        val errors = mutableListOf<String>()

        // Check file size (max 5MB)
        val maxSizeBytes = 5 * 1024 * 1024
        if (fileSize > maxSizeBytes) {
            errors.add("File size must not exceed 5MB")
        }

        // Check file name
        if (fileName.isBlank()) {
            errors.add("File name is required")
        }

        // Check file URL
        if (fileUrl.isBlank()) {
            errors.add("File URL is required")
        }

        // Check file type
        val allowedTypes = listOf("image/jpeg", "image/jpg", "image/png", "application/pdf")
        if (!allowedTypes.contains(fileType.lowercase())) {
            errors.add("Only JPEG, PNG, and PDF files are allowed")
        }

        return ValidationResult(
            isValid = errors.isEmpty(),
            errors = errors
        )
    }

    companion object {
        /**
         * Maximum allowed file size in bytes (5MB)
         */
        const val MAX_FILE_SIZE_BYTES: Long = 5 * 1024 * 1024

        /**
         * Allowed file types for collateral documents
         */
        val ALLOWED_FILE_TYPES = listOf(
            "image/jpeg",
            "image/jpg",
            "image/png",
            "application/pdf"
        )

        /**
         * Allowed file extensions for collateral documents
         */
        val ALLOWED_EXTENSIONS = listOf("jpg", "jpeg", "png", "pdf")
    }
}