package com.soshopay.android.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "payment_dashboard")
data class PaymentDashboardEntity(
    @PrimaryKey val id: String,
    val dashboardData: String, // JSON serialized PaymentDashboard
    val createdAt: Long,
    val updatedAt: Long,
)
