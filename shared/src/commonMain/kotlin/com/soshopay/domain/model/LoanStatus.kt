package com.soshopay.domain.model

enum class LoanStatus {
    PENDING_DISBURSEMENT,
    ACTIVE,
    COMPLETED,
    DEFAULTED,
    CANCELLED,
    ;

    fun getDisplayName(): String =
        when (this) {
            PENDING_DISBURSEMENT -> "Pending Disbursement"
            ACTIVE -> "Active"
            COMPLETED -> "Completed"
            DEFAULTED -> "Defaulted"
            CANCELLED -> "Cancelled"
        }

    fun getDescription(): String =
        when (this) {
            PENDING_DISBURSEMENT -> "Loan is pending disbursement"
            ACTIVE -> "Loan is active and in repayment"
            COMPLETED -> "Loan has been fully repaid"
            DEFAULTED -> "Loan has been defaulted and will not be repaid"
            CANCELLED -> "Loan has been cancelled"
        }

    fun getColorCode(): String =
        when (this) {
            PENDING_DISBURSEMENT -> "#FF9800" // Orange
            ACTIVE -> "#4CAF50" // Green
            COMPLETED -> "#4CAF50" // Green
            DEFAULTED -> "#F44336" // Red
            CANCELLED -> "#F44336" // Red
        }
}
