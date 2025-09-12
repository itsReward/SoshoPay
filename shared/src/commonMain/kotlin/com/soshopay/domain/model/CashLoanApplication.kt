package com.soshopay.domain.model

import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable

@Serializable
data class CashLoanApplication(
    val id: String = "",
    val userId: String = "",
    val applicationId: String = "",
    val loanType: LoanType = LoanType.CASH,
    val loanAmount: Double,
    val repaymentPeriod: String,
    val loanPurpose: String,
    val employerIndustry: String,
    val collateralValue: Double,
    val collateralDetails: String,
    val calculatedTerms: CashLoanTerms? = null,
    val status: ApplicationStatus = ApplicationStatus.DRAFT,
    val submittedAt: Long = 0,
    val reviewStartedAt: Long? = null,
    val reviewCompletedAt: Long? = null,
    val acceptedTerms: Boolean = false,
    val createdAt: Long = Clock.System.now().toEpochMilliseconds(),
    val updatedAt: Long = Clock.System.now().toEpochMilliseconds(),
) {
    fun isEditable(): Boolean = status in listOf(ApplicationStatus.DRAFT)

    fun canBeWithdrawn(): Boolean =
        status in
            listOf(
                ApplicationStatus.SUBMITTED,
                ApplicationStatus.UNDER_REVIEW,
            )

    fun getStatusText(): String =
        when (status) {
            ApplicationStatus.DRAFT -> "Draft"
            ApplicationStatus.SUBMITTED -> "Submitted"
            ApplicationStatus.UNDER_REVIEW -> "Under Review"
            ApplicationStatus.APPROVED -> "Approved"
            ApplicationStatus.REJECTED -> "Rejected"
            ApplicationStatus.CANCELLED -> "Cancelled"
        }
}
