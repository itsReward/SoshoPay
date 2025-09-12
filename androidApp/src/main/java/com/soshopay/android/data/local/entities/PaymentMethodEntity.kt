package com.soshopay.android.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.soshopay.domain.model.PaymentMethod
import com.soshopay.domain.model.PaymentMethodInfo

@Entity(tableName = "payment_methods")
data class PaymentMethodEntity(
    @PrimaryKey val id: String,
    val name: String,
    val type: String, // PaymentMethod enum as string
    val isActive: Boolean,
    val description: String,
    val processingTime: String,
    val minimumAmount: Double,
    val maximumAmount: Double,
    val fees: Double,
    val createdAt: Long,
    val updatedAt: Long,
) {
    fun toDomain(): PaymentMethodInfo =
        PaymentMethodInfo(
            id = id,
            name = name,
            type = PaymentMethod.valueOf(type),
            isActive = isActive,
            description = description,
            processingTime = processingTime,
            minimumAmount = minimumAmount,
            maximumAmount = maximumAmount,
            fees = fees,
        )
}
