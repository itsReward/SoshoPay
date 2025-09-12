package com.soshopay.android.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "draft_paygo_loans")
data class DraftPayGoLoanEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val applicationData: String, // JSON serialized PayGoLoanApplication
    val createdAt: Long,
    val updatedAt: Long,
)
