package com.soshopay.android.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.soshopay.domain.model.EarlyPayoffCalculation

@Entity(tableName = "early_payoff_calculations")
data class EarlyPayoffCalculationEntity(
    @PrimaryKey val loanId: String,
    val currentBalance: Double,
    val earlyPayoffAmount: Double,
    val savingsAmount: Double,
    val calculatedAt: Long,
    val createdAt: Long,
    val updatedAt: Long,
) {
    fun toDomain(): EarlyPayoffCalculation =
        EarlyPayoffCalculation(
            loanId = loanId,
            currentBalance = currentBalance,
            earlyPayoffAmount = earlyPayoffAmount,
            savingsAmount = savingsAmount,
            calculatedAt = calculatedAt,
        )
}
