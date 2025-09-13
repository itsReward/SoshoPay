package com.soshopay.android.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cash_loan_form_data")
data class CashLoanFormDataEntity(
    @PrimaryKey val id: String,
    val formDataJson: String,
    val createdAt: Long,
    val updatedAt: Long,
)
