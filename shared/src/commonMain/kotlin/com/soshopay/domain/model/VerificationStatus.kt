package com.soshopay.domain.model

enum class VerificationStatus {
    UNVERIFIED,
    PENDING,
    VERIFIED,
    REJECTED,
    ;

    fun getDisplayName(): String =
        when (this) {
            UNVERIFIED -> "Not Verified"
            PENDING -> "Pending Verification"
            VERIFIED -> "Verified"
            REJECTED -> "Verification Rejected"
        }

    fun getDescription(): String =
        when (this) {
            UNVERIFIED -> "Documents or information not yet submitted for verification"
            PENDING -> "Under review by our verification team"
            VERIFIED -> "Successfully verified and approved"
            REJECTED -> "Verification failed - please check requirements and resubmit"
        }

    fun getColorCode(): String =
        when (this) {
            UNVERIFIED -> "#9E9E9E" // Grey
            PENDING -> "#FF9800" // Orange
            VERIFIED -> "#4CAF50" // Green
            REJECTED -> "#F44336" // Red
        }

    fun isActionRequired(): Boolean =
        when (this) {
            UNVERIFIED -> true
            PENDING -> false
            VERIFIED -> false
            REJECTED -> true
        }
}
