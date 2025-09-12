package com.soshopay.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class PaymentDashboard(
    val totalOutstanding: Double,
    val nextPaymentAmount: Double,
    val nextPaymentDate: Long,
    val overdueAmount: Double,
    val overdueCount: Int,
    val paymentSummaries: List<PaymentSummary>,
    val recentPayments: List<Payment>,
)
