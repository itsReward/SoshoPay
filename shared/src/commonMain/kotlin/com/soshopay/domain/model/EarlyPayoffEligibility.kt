package com.soshopay.domain.model

data class EarlyPayoffEligibility(
    val isEligible: Boolean,
    val reasons: List<String>,
    val minimumPaymentsRequired: Int,
    val paymentsCompleted: Int,
    val estimatedSavings: Double,
)
