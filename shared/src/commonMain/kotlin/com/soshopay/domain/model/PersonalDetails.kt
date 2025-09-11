package com.soshopay.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class PersonalDetails(
    val firstName: String,
    val lastName: String,
    val dateOfBirth: Long, // timestamp
    val gender: String,
    val nationality: String,
    val occupation: String,
    val monthlyIncome: Double,
    val lastUpdated: Long =
        kotlinx.datetime.Clock.System
            .now()
            .toEpochMilliseconds(),
) {
    fun getAge(): Int {
        val currentTime =
            kotlinx.datetime.Clock.System
                .now()
                .toEpochMilliseconds()
        val ageInMillis = currentTime - dateOfBirth
        return (ageInMillis / (365.25 * 24 * 60 * 60 * 1000)).toInt()
    }

    fun getFullName(): String = "$firstName $lastName"

    fun isComplete(): Boolean =
        firstName.isNotBlank() &&
            lastName.isNotBlank() &&
            dateOfBirth > 0 &&
            gender.isNotBlank() &&
            nationality.isNotBlank() &&
            occupation.isNotBlank() &&
            monthlyIncome >= 0
}
