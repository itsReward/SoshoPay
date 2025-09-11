package com.soshopay.domain.model

enum class DocumentType {
    PROOF_OF_RESIDENCE,
    NATIONAL_ID,
    PROFILE_PICTURE,
    ;

    fun getDisplayName(): String =
        when (this) {
            PROOF_OF_RESIDENCE -> "Proof of Residence"
            NATIONAL_ID -> "National ID"
            PROFILE_PICTURE -> "Profile Picture"
        }

    fun getDescription(): String =
        when (this) {
            PROOF_OF_RESIDENCE -> "A document showing your residential address (utility bill, bank statement, etc.)"
            NATIONAL_ID -> "Your Zimbabwe National Identity Document"
            PROFILE_PICTURE -> "A clear photo of yourself for profile identification"
        }

    fun isRequired(): Boolean =
        when (this) {
            PROOF_OF_RESIDENCE -> true
            NATIONAL_ID -> true
            PROFILE_PICTURE -> false
        }

    fun getAllowedFormats(): List<String> =
        when (this) {
            PROOF_OF_RESIDENCE -> listOf("pdf", "jpg", "jpeg", "png")
            NATIONAL_ID -> listOf("pdf", "jpg", "jpeg", "png")
            PROFILE_PICTURE -> listOf("jpg", "jpeg", "png")
        }

    fun getMaxSizeBytes(): Long =
        when (this) {
            PROOF_OF_RESIDENCE -> 5 * 1024 * 1024 // 5MB
            NATIONAL_ID -> 5 * 1024 * 1024 // 5MB
            PROFILE_PICTURE -> 2 * 1024 * 1024 // 2MB
        }
}
