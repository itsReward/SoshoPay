package com.soshopay.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class LoanDetails(
    val loan: Loan,
    val paymentSchedule: List<PaymentSchedule>,
    val recentPayments: List<Payment>,
    val canEarlyPayoff: Boolean = false,
    val earlyPayoffAmount: Double? = null,
)
