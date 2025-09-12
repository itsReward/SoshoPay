package com.soshopay.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class CashLoanCalculationRequest(
    val loanAmount: Double,
    val repaymentPeriod: String,
    val employerIndustry: String,
    val collateralValue: Double,
    val monthlyIncome: Double,
)
