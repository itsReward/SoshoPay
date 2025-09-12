package com.soshopay.domain.model

import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable

@Serializable
data class Guarantor(
    val id: String = "",
    val applicationId: String = "",
    val name: String,
    val mobileNumber: String,
    val nationalId: String,
    val occupationClass: String,
    val monthlyIncome: Double,
    val relationshipToClient: String,
    val address: Address,
    val verificationStatus: VerificationStatus = VerificationStatus.UNVERIFIED,
    val createdAt: Long = Clock.System.now().toEpochMilliseconds(),
) {
    fun isVerified(): Boolean = verificationStatus == VerificationStatus.VERIFIED

    fun isComplete(): Boolean =
        name.isNotBlank() &&
            mobileNumber.isNotBlank() &&
            nationalId.isNotBlank() &&
            occupationClass.isNotBlank() &&
            monthlyIncome > 0 &&
            relationshipToClient.isNotBlank() &&
            address.isComplete()
}
