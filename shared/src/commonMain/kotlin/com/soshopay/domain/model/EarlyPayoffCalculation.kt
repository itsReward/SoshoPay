package com.soshopay.domain.model

import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable

@Serializable
data class EarlyPayoffCalculation(
    val loanId: String,
    val currentBalance: Double,
    val earlyPayoffAmount: Double,
    val savingsAmount: Double,
    val calculatedAt: Long = Clock.System.now().toEpochMilliseconds(),
)
