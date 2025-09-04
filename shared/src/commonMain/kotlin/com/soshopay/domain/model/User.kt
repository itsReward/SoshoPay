package com.soshopay.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: String,
    val phoneNumber: String,
    val profilePicture: String? = null,
    val personalDetails: PersonalDetails? = null,
    val address: Address? = null,
    val clientType: ClientType = ClientType.PRIVATE_SECTOR_EMPLOYEE,
    val verificationStatus: VerificationStatus = VerificationStatus.UNVERIFIED,
    val canApplyForLoan: Boolean = false,
    val accountStatus: AccountStatus = AccountStatus.INCOMPLETE
)

@Serializable
data class PersonalDetails(
    val firstName: String,
    val lastName: String,
    val dateOfBirth: String,
    val gender: String,
    val nationality: String,
    val occupation: String,
    val monthlyIncome: Double
)

@Serializable
data class Address(
    val streetAddress: String,
    val suburb: String,
    val city: String,
    val province: String,
    val postalCode: String,
    val residenceType: String
)

enum class ClientType {
    PRIVATE_SECTOR_EMPLOYEE,
    GOVERNMENT_EMPLOYEE,
    ENTREPRENEUR
}

enum class VerificationStatus {
    UNVERIFIED,
    PENDING,
    VERIFIED,
    REJECTED
}

enum class AccountStatus {
    INCOMPLETE,
    COMPLETE,
    VERIFIED
}
