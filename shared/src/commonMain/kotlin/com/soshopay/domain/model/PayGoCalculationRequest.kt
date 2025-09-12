package com.soshopay.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class PayGoCalculationRequest(
    val productId: String,
    val repaymentPeriod: String,
    val usagePerDay: String,
    val salaryBand: String,
    val monthlyIncome: Double,
)
