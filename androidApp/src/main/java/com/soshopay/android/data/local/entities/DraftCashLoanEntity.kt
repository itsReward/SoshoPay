package com.soshopay.android.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "draft_cash_loans")
data class DraftCashLoanEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val applicationData: String, // JSON serialized CashLoanApplication
    val createdAt: Long,
    val updatedAt: Long,
)
