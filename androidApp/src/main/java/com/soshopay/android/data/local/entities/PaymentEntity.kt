package com.soshopay.android.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.soshopay.domain.model.Payment
import com.soshopay.domain.model.PaymentStatus

@Entity(tableName = "payments")
data class PaymentEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val loanId: String,
    val paymentId: String,
    val amount: Double,
    val method: String,
    val phoneNumber: String,
    val receiptNumber: String,
    val status: String,
    val processedAt: Long,
    val failureReason: String?,
    val createdAt: Long,
    val principal: Double?,
    val interest: Double?,
    val penalties: Double?,
    val updatedAt: Long,
) {
    fun toDomain(): Payment =
        Payment(
            id = id,
            userId = userId,
            loanId = loanId,
            paymentId = paymentId,
            amount = amount,
            method = method,
            phoneNumber = phoneNumber,
            receiptNumber = receiptNumber,
            status = PaymentStatus.valueOf(status),
            processedAt = processedAt,
            failureReason = failureReason,
            createdAt = createdAt,
            principal = principal,
            interest = interest,
            penalties = penalties,
            updatedAt = updatedAt,
        )
}
