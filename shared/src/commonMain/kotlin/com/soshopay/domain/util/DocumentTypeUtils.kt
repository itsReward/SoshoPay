package com.soshopay.domain.util

import com.soshopay.domain.model.DocumentType

object DocumentTypeUtils {
    fun getRequiredDocumentTypes(): List<DocumentType> = DocumentType.values().filter { it.isRequired() }

    fun getOptionalDocumentTypes(): List<DocumentType> = DocumentType.values().filter { !it.isRequired() }

    fun validateFileForDocumentType(
        documentType: DocumentType,
        fileName: String,
        fileSizeBytes: Long,
    ): ValidationResult {
        val errors = mutableListOf<String>()

        // Check file size
        if (fileSizeBytes > documentType.getMaxSizeBytes()) {
            val maxSizeMB = documentType.getMaxSizeBytes() / (1024 * 1024)
            errors.add("File size must not exceed ${maxSizeMB}MB")
        }

        // Check file format
        val fileExtension = fileName.substringAfterLast('.', "").lowercase()
        if (!documentType.getAllowedFormats().contains(fileExtension)) {
            errors.add("Allowed formats: ${documentType.getAllowedFormats().joinToString(", ")}")
        }

        // Check file name
        if (fileName.isBlank()) {
            errors.add("File name is required")
        }

        return ValidationResult(
            isValid = errors.isEmpty(),
            errors = errors,
        )
    }

    fun getUploadInstructions(documentType: DocumentType): List<String> =
        when (documentType) {
            DocumentType.PROOF_OF_RESIDENCE ->
                listOf(
                    "Upload a recent utility bill, bank statement, or rental agreement",
                    "Document must show your name and current address",
                    "Document should be dated within the last 3 months",
                    "Ensure all text is clearly visible and readable",
                )
            DocumentType.NATIONAL_ID ->
                listOf(
                    "Upload a clear photo or scan of your National ID",
                    "Both front and back sides must be visible",
                    "Ensure all text and numbers are clearly readable",
                    "Do not cover any part of the ID with your fingers",
                )
            DocumentType.PROFILE_PICTURE ->
                listOf(
                    "Upload a clear, recent photo of yourself",
                    "Face should be clearly visible and well-lit",
                    "Photo should be taken against a plain background",
                    "Remove sunglasses, hats, or face coverings",
                )
        }

    fun getDocumentTypeFromApiString(apiString: String): DocumentType? =
        when (apiString.lowercase()) {
            "proof_of_residence", "proof-of-residence" -> DocumentType.PROOF_OF_RESIDENCE
            "national_id", "national-id", "nationalid" -> DocumentType.NATIONAL_ID
            "profile_picture", "profile-picture", "profilepicture" -> DocumentType.PROFILE_PICTURE
            else -> null
        }

    fun getApiStringFromDocumentType(documentType: DocumentType): String =
        when (documentType) {
            DocumentType.PROOF_OF_RESIDENCE -> "proof_of_residence"
            DocumentType.NATIONAL_ID -> "national_id"
            DocumentType.PROFILE_PICTURE -> "profile_picture"
        }
}
