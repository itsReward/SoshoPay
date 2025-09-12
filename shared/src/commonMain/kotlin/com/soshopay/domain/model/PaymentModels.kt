package com.soshopay.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class PaymentReceipt(
    val receiptNumber: String,
    val paymentId: String,
    val loanId: String,
    val amount: Double,
    val paymentMethod: String,
    val phoneNumber: String,
    val processedAt: Long,
    val customerName: String,
    val loanType: LoanType,
    val productName: String? = null,
    val transactionReference: String,
    val principal: Double? = null,
    val interest: Double? = null,
    val penalties: Double? = null,
)
