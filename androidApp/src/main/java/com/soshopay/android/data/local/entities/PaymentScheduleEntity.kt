package com.soshopay.android.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.soshopay.domain.model.PaymentSchedule
import com.soshopay.domain.model.PaymentStatus

@Entity(tableName = "payment_schedules")
data class PaymentScheduleEntity(
    @PrimaryKey val id: String,
    val loanId: String,
    val paymentNumber: Int,
    val dueDate: Long,
    val amount: Double,
    val principal: Double,
    val interest: Double,
    val status: String, // PaymentStatus enum as string
    val paidDate: Long?,
    val receiptNumber: String?,
    val penalties: Double?,
    val createdAt: Long,
    val updatedAt: Long,
) {
    fun toDomain(): PaymentSchedule =
        PaymentSchedule(
            paymentNumber = paymentNumber,
            dueDate = dueDate,
            amount = amount,
            principal = principal,
            interest = interest,
            status = PaymentStatus.valueOf(status),
            paidDate = paidDate,
            receiptNumber = receiptNumber,
            penalties = penalties,
        )
}
