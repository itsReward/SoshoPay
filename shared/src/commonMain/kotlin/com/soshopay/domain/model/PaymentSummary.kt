package com.soshopay.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class PaymentSummary(
    val loanId: String,
    val loanType: LoanType,
    val productName: String? = null,
    val amountDue: Double,
    val dueDate: Long,
    val status: PaymentStatus,
    val daysUntilDue: Int,
    val daysOverdue: Int,
    val penalties: Double = 0.0,
) {
    fun isOverdue(): Boolean = daysOverdue > 0

    fun isCurrent(): Boolean = status == PaymentStatus.CURRENT

    fun getStatusText(): String =
        when {
            isOverdue() -> "Overdue"
            daysUntilDue <= 7 -> "Due Soon"
            else -> "Current"
        }
}
