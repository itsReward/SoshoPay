package com.soshopay.domain.model

import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable

@Serializable
data class PaymentResult(
    val paymentId: String,
    val status: PaymentStatus,
    val message: String,
    val receiptNumber: String? = null,
    val transactionReference: String? = null,
    val processedAt: Long = Clock.System.now().toEpochMilliseconds(),
)
