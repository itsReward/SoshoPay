package com.soshopay.domain.model

import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable

@Serializable
data class Loan(
    val id: String,
    val userId: String,
    val applicationId: String,
    val loanType: LoanType,
    val originalAmount: Double,
    val totalAmount: Double,
    val remainingBalance: Double,
    val interestRate: Double,
    val repaymentPeriod: String,
    val disbursementDate: Long,
    val maturityDate: Long,
    val status: LoanStatus,
    val nextPaymentDate: Long? = null,
    val nextPaymentAmount: Double? = null,
    val paymentsCompleted: Int = 0,
    val totalPayments: Int,
    val productName: String? = null,
    val loanPurpose: String? = null,
    val installationDate: Long? = null,
    val rejectionReason: String? = null,
    val rejectionDate: Long? = null,
    val createdAt: Long,
    val updatedAt: Long,
) {
    fun isActive(): Boolean = status == LoanStatus.ACTIVE

    fun isOverdue(): Boolean =
        nextPaymentDate?.let {
            it < Clock.System.now().toEpochMilliseconds() && status == LoanStatus.ACTIVE
        } ?: false

    fun getProgressPercentage(): Double = if (totalPayments > 0) (paymentsCompleted.toDouble() / totalPayments) * 100 else 0.0

    fun getDaysUntilDue(): Int =
        nextPaymentDate?.let {
            val diff = it - Clock.System.now().toEpochMilliseconds()
            (diff / (1000 * 60 * 60 * 24)).toInt()
        } ?: 0

    fun getDaysOverdue(): Int = if (isOverdue()) -getDaysUntilDue() else 0
}
