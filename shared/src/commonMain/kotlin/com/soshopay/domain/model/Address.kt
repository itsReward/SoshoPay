package com.soshopay.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Address(
    val streetAddress: String,
    val suburb: String,
    val city: String,
    val province: String,
    val postalCode: String,
    val residenceType: String,
    val lastUpdated: Long =
        kotlinx.datetime.Clock.System
            .now()
            .toEpochMilliseconds(),
) {
    fun getFullAddress(): String =
        buildString {
            append(streetAddress)
            if (suburb.isNotBlank()) append(", $suburb")
            if (city.isNotBlank()) append(", $city")
            if (province.isNotBlank()) append(", $province")
            if (postalCode.isNotBlank()) append(" $postalCode")
        }

    fun isComplete(): Boolean =
        streetAddress.isNotBlank() &&
            suburb.isNotBlank() &&
            city.isNotBlank() &&
            province.isNotBlank() &&
            residenceType.isNotBlank()
}
