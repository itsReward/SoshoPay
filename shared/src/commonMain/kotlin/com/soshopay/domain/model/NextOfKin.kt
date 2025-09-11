package com.soshopay.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class NextOfKin(
    val id: String,
    val userId: String,
    val fullName: String,
    val relationship: String,
    val phoneNumber: String,
    val address: Address,
    val documents: Documents? = null,
    val createdAt: Long,
    val updatedAt: Long,
) {
    fun isComplete(): Boolean =
        fullName.isNotBlank() &&
            relationship.isNotBlank() &&
            phoneNumber.isNotBlank() &&
            address.isComplete()
}
