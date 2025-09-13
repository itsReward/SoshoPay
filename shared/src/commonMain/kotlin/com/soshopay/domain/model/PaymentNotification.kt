package com.soshopay.domain.model

data class PaymentNotification(
    val paymentId: String,
    val status: PaymentStatus,
    val message: String,
    val timestamp: Long,
    val metadata: Map<String, String> = emptyMap(),
)
