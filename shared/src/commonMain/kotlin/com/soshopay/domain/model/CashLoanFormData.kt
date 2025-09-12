package com.soshopay.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class CashLoanFormData(
    val repaymentPeriods: List<String>,
    val loanPurposes: List<String>,
    val employerIndustries: List<String>,
    val minLoanAmount: Double,
    val maxLoanAmount: Double,
    val minCollateralValue: Double,
)
