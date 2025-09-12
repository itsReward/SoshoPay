package com.soshopay.domain.model

import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable

@Serializable
data class PaymentSchedule(
    val paymentNumber: Int,
    val dueDate: Long,
    val amount: Double,
    val principal: Double,
    val interest: Double,
    val status: PaymentStatus,
    val paidDate: Long? = null,
    val receiptNumber: String? = null,
    val penalties: Double? = null,
) {
    fun isPaid(): Boolean = status == PaymentStatus.SUCCESSFUL

    fun isUpcoming(): Boolean = dueDate > Clock.System.now().toEpochMilliseconds() && status == PaymentStatus.PENDING

    fun isOverdue(): Boolean = dueDate < Clock.System.now().toEpochMilliseconds() && status != PaymentStatus.SUCCESSFUL
}
