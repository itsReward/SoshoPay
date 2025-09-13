package com.soshopay.android.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.soshopay.domain.model.LoanType
import com.soshopay.domain.model.PaymentReceipt

@Entity(tableName = "payment_receipts")
data class PaymentReceiptEntity(
    @PrimaryKey val receiptNumber: String,
    val paymentId: String,
    val loanId: String,
    val amount: Double,
    val paymentMethod: String,
    val phoneNumber: String,
    val processedAt: Long,
    val customerName: String,
    val loanType: String, // LoanType enum as string
    val productName: String?,
    val transactionReference: String,
    val principal: Double?,
    val interest: Double?,
    val penalties: Double?,
    val createdAt: Long,
    val updatedAt: Long,
) {
    fun toDomain(): PaymentReceipt =
        PaymentReceipt(
            receiptNumber = receiptNumber,
            paymentId = paymentId,
            loanId = loanId,
            amount = amount,
            paymentMethod = paymentMethod,
            phoneNumber = phoneNumber,
            processedAt = processedAt,
            customerName = customerName,
            loanType = LoanType.valueOf(loanType),
            productName = productName,
            transactionReference = transactionReference,
            principal = principal,
            interest = interest,
            penalties = penalties,
        )
}
