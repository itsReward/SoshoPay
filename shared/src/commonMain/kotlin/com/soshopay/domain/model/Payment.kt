package com.soshopay.domain.model

import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable

// ========== PAYMENT MODELS ==========
@Serializable
data class Payment(
    val id: String,
    val userId: String,
    val loanId: String,
    val paymentId: String,
    val amount: Double,
    val method: String,
    val phoneNumber: String,
    val receiptNumber: String,
    val status: PaymentStatus,
    val processedAt: Long,
    val failureReason: String? = null,
    val createdAt: Long,
    val principal: Double? = null,
    val interest: Double? = null,
    val penalties: Double? = null,
    val updatedAt: Long = Clock.System.now().toEpochMilliseconds(),
) {
    fun isSuccessful(): Boolean = status == PaymentStatus.SUCCESSFUL

    fun isFailed(): Boolean = status == PaymentStatus.FAILED

    fun isPending(): Boolean = status in listOf(PaymentStatus.PENDING, PaymentStatus.PROCESSING)

    fun getStatusText(): String =
        when (status) {
            PaymentStatus.PENDING -> "Pending"
            PaymentStatus.PROCESSING -> "Processing"
            PaymentStatus.SUCCESSFUL -> "Successful"
            PaymentStatus.FAILED -> "Failed"
            PaymentStatus.CANCELLED -> "Cancelled"
            PaymentStatus.OVERDUE -> "Overdue"
            PaymentStatus.CURRENT -> "Current"
            PaymentStatus.COMPLETED -> "Completed"
        }
}
