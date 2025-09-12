package com.soshopay.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class PaymentMethodInfo(
    val id: String,
    val name: String,
    val type: PaymentMethod,
    val isActive: Boolean,
    val description: String,
    val processingTime: String,
    val minimumAmount: Double,
    val maximumAmount: Double,
    val fees: Double = 0.0,
)
