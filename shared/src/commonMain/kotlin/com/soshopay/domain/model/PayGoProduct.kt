package com.soshopay.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class PayGoProduct(
    val id: String,
    val name: String,
    val category: String,
    val price: Double,
    val description: String,
    val specifications: String,
    val image: String? = null,
    val installationFee: Double,
    val isAvailable: Boolean = true,
) {
    fun getTotalCost(): Double = price + installationFee
}
