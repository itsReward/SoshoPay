package com.soshopay.domain.model

enum class ApprovalStatus {
    PENDING,
    APPROVED,
    REJECTED,
    ;

    fun getDisplayName(): String =
        when (this) {
            PENDING -> "Pending Approval"
            APPROVED -> "Approved"
            REJECTED -> "Rejected"
        }

    fun getDescription(): String =
        when (this) {
            PENDING -> "Awaiting admin review and approval"
            APPROVED -> "Request has been approved"
            REJECTED -> "Request was rejected - please contact support for details"
        }

    fun getColorCode(): String =
        when (this) {
            PENDING -> "#FF9800" // Orange
            APPROVED -> "#4CAF50" // Green
            REJECTED -> "#F44336" // Red
        }
}
