package com.soshopay.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class CashLoanTerms(
    val monthlyPayment: Double,
    val interestRate: Double,
    val totalAmount: Double,
    val processingFee: Double,
    val firstPaymentDate: Long,
    val finalPaymentDate: Long,
) {
    fun getTotalInterest(): Double = totalAmount - (totalAmount - processingFee - getTotalInterest())

    fun getTotalCost(): Double = totalAmount + processingFee
}
