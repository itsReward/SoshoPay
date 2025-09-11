package com.soshopay.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: String,
    val phoneNumber: String,
    val profilePicture: ProfilePicture? = null,
    val personalDetails: PersonalDetails? = null,
    val address: Address? = null,
    val documents: Documents? = null,
    val nextOfKin: NextOfKin? = null,
    val clientType: ClientType = ClientType(),
    val verificationStatus: VerificationStatus = VerificationStatus.UNVERIFIED,
    val canApplyForLoan: Boolean = false,
    val accountStatus: AccountStatus = AccountStatus.INCOMPLETE,
    val createdAt: Long =
        kotlinx.datetime.Clock.System
            .now()
            .toEpochMilliseconds(),
    val updatedAt: Long =
        kotlinx.datetime.Clock.System
            .now()
            .toEpochMilliseconds(),
) {
    fun isProfileComplete(): Boolean =
        personalDetails?.isComplete() == true &&
            address?.isComplete() == true &&
            documents?.isAllDocumentsUploaded() == true

    fun canMakeLoanApplication(): Boolean =
        isProfileComplete() &&
            verificationStatus == VerificationStatus.VERIFIED &&
            canApplyForLoan

    fun getFullName(): String = personalDetails?.getFullName() ?: "User"
}

enum class ClientType {
    PRIVATE_SECTOR_EMPLOYEE,
    GOVERNMENT_EMPLOYEE,
    ENTREPRENEUR,
}

enum class VerificationStatus {
    UNVERIFIED,
    PENDING,
    VERIFIED,
    REJECTED,
}

enum class AccountStatus {
    INCOMPLETE,
    COMPLETE,
    VERIFIED,
}
