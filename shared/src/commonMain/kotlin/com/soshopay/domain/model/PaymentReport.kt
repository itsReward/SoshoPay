package com.soshopay.domain.model

data class PaymentReport(
    val type: PaymentReportType,
    val startDate: Long,
    val endDate: Long,
    val totalPayments: Int,
    val totalAmount: Double,
    val successfulPayments: Int,
    val failedPayments: Int,
    val pendingPayments: Int,
    val averageAmount: Double,
    val data: Map<String, Any>,
)
