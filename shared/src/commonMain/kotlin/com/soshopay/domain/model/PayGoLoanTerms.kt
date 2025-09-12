package com.soshopay.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class PayGoLoanTerms(
    val weeklyPayment: Double,
    val monthlyPayment: Double,
    val totalLoanAmount: Double,
    val interestRate: Double,
    val installationFee: Double,
) {
    fun getTotalInterest(): Double = totalLoanAmount - (totalLoanAmount / (1 + interestRate))

    fun getTotalCost(): Double = totalLoanAmount + installationFee
}
