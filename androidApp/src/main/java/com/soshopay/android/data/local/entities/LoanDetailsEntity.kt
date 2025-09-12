package com.soshopay.android.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "loan_details")
data class LoanDetailsEntity(
    @PrimaryKey val loanId: String,
    val loanDetailsData: String, // JSON serialized LoanDetails
    val createdAt: Long,
    val updatedAt: Long,
)
