package com.soshopay.domain.model

data class PaymentAnalytics(
    val totalPayments: Int,
    val totalAmount: Double,
    val successfulPayments: Int,
    val failedPayments: Int,
    val averagePaymentAmount: Double,
    val paymentMethodBreakdown: Map<String, Int>,
    val monthlyTrend: Map<String, Double>,
    val successRate: Double,
)
