package com.soshopay.domain.util

import com.soshopay.domain.model.DocumentType

/**
 * Utility class for handling operations related to document types, such as validation,
 * instructions, and mapping between API strings and enum values.
 *
 * Provides helper methods for determining required/optional document types, validating files,
 * retrieving upload instructions, and converting between API strings and DocumentType enums.
 */
object DocumentTypeUtils {
    /**
     * Returns a list of required document types.
     * @return List of DocumentType values that are required.
     */
    fun getRequiredDocumentTypes(): List<DocumentType> = DocumentType.values().filter { it.isRequired() }

    /**
     * Returns a list of optional document types.
     * @return List of DocumentType values that are optional.
     */
    fun getOptionalDocumentTypes(): List<DocumentType> = DocumentType.values().filter { !it.isRequired() }

    /**
     * Validates a file for the specified document type, checking size, format, and name.
     * @param documentType The type of document being validated.
     * @param fileName The name of the file to validate.
     * @param fileSizeBytes The size of the file in bytes.
     * @return ValidationResult indicating validity and any error messages.
     */
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

    /**
     * Returns a list of upload instructions for the specified document type.
     * @param documentType The type of document for which instructions are needed.
     * @return List of instruction strings.
     */
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

    /**
     * Maps an API string to the corresponding DocumentType enum value.
     * @param apiString The API string representing a document type.
     * @return DocumentType if matched, or null if not recognized.
     */
    fun getDocumentTypeFromApiString(apiString: String): DocumentType? =
        when (apiString.lowercase()) {
            "proof_of_residence", "proof-of-residence" -> DocumentType.PROOF_OF_RESIDENCE
            "national_id", "national-id", "nationalid" -> DocumentType.NATIONAL_ID
            "profile_picture", "profile-picture", "profilepicture" -> DocumentType.PROFILE_PICTURE
            else -> null
        }

    /**
     * Converts a DocumentType enum value to its corresponding API string.
     * @param documentType The DocumentType to convert.
     * @return API string representation of the document type.
     */
    fun getApiStringFromDocumentType(documentType: DocumentType): String =
        when (documentType) {
            DocumentType.PROOF_OF_RESIDENCE -> "proof_of_residence"
            DocumentType.NATIONAL_ID -> "national_id"
            DocumentType.PROFILE_PICTURE -> "profile_picture"
        }
}
