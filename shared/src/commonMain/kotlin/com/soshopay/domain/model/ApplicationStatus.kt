package com.soshopay.domain.model

import com.soshopay.domain.model.ApprovalStatus.PENDING
import com.soshopay.domain.model.DocumentType.NATIONAL_ID
import com.soshopay.domain.model.DocumentType.PROFILE_PICTURE
import com.soshopay.domain.model.DocumentType.PROOF_OF_RESIDENCE

enum class ApplicationStatus {
    DRAFT,
    SUBMITTED,
    UNDER_REVIEW,
    APPROVED,
    REJECTED,
    CANCELLED,
    ;

    fun getDisplayName(): String =
        when (this) {
            ApplicationStatus.DRAFT -> "Draft"
            ApplicationStatus.SUBMITTED -> "Submitted"
            ApplicationStatus.UNDER_REVIEW -> "Under Review"
            ApplicationStatus.APPROVED -> "Approved"
            ApplicationStatus.REJECTED -> "Rejected"
            ApplicationStatus.CANCELLED -> "Cancelled"
        }

    fun getDescription(): String =
        when (this) {
            ApplicationStatus.DRAFT -> "Please complete all required fields and submit your application."
            ApplicationStatus.SUBMITTED -> "Your application has been submitted and is awaiting review."
            ApplicationStatus.UNDER_REVIEW -> "Our team is currently reviewing your application."
            ApplicationStatus.APPROVED -> "Congratulations! Your application has been approved."
            ApplicationStatus.REJECTED -> "Your application has been declined. Please contact support for more information."
            ApplicationStatus.CANCELLED -> "This application has been cancelled."
        }

    fun getColorCode(): String =
        when (this) {
            ApplicationStatus.DRAFT -> "#FF9800" // Orange
            ApplicationStatus.SUBMITTED -> "#FF9800" // Orange
            ApplicationStatus.UNDER_REVIEW -> "#FF9800" // Orange
            ApplicationStatus.APPROVED -> "#4CAF50" // Green
            ApplicationStatus.REJECTED -> "#F44336" // Red
            ApplicationStatus.CANCELLED -> "#F44336" // Red
        }
}
