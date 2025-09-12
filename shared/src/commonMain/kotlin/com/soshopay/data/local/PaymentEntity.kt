package com.soshopay.data.local

import com.soshopay.domain.model.Payment
import com.soshopay.domain.model.PaymentStatus
import kotlinx.serialization.Serializable

@Serializable
data class PaymentEntity(
    val id: String,
    val userId: String,
    val loanId: String,
    val paymentId: String,
    val amount: Double,
    val method: String,
    val phoneNumber: String,
    val receiptNumber: String,
    val status: String, // Store as string for database
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

    companion object {
        fun fromDomain(payment: Payment): PaymentEntity =
            PaymentEntity(
                id = payment.id,
                userId = payment.userId,
                loanId = payment.loanId,
                paymentId = payment.paymentId,
                amount = payment.amount,
                method = payment.method,
                phoneNumber = payment.phoneNumber,
                receiptNumber = payment.receiptNumber,
                status = payment.status.name,
                processedAt = payment.processedAt,
                failureReason = payment.failureReason,
                createdAt = payment.createdAt,
                principal = payment.principal,
                interest = payment.interest,
                penalties = payment.penalties,
                updatedAt = payment.updatedAt,
            )
    }
}
