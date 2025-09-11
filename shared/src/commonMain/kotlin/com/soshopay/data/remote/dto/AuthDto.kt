package com.soshopay.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// Request DTOs
@Serializable
data class SendOtpRequest(
    val mobile: String,
)

@Serializable
data class VerifyOtpRequest(
    @SerialName("otp_id") val otpId: String,
    @SerialName("otp_code") val otpCode: String,
)

@Serializable
data class SetPinRequest(
    val mobile: String,
    @SerialName("new_pin") val newPin: String,
    @SerialName("confirm_pin") val confirmPin: String,
)

@Serializable
data class LoginRequest(
    val mobile: String,
    val pin: String,
)

@Serializable
data class RefreshTokenRequest(
    @SerialName("refresh_token") val refreshToken: String,
)

@Serializable
data class CreateClientRequest(
    @SerialName("first_name") val firstName: String,
    @SerialName("last_name") val lastName: String,
    val mobile: String,
    val pin: String,
    @SerialName("confirm_pin") val confirmPin: String,
)

@Serializable
data class UpdatePinRequest(
    @SerialName("current_pin") val currentPin: String,
    @SerialName("new_pin") val newPin: String,
    @SerialName("confirm_pin") val confirmPin: String,
)

@Serializable
data class StartMobileChangeRequest(
    @SerialName("new_mobile") val newMobile: String,
)

@Serializable
data class VerifyMobileChangeRequest(
    @SerialName("change_token") val changeToken: String,
    val otp: String,
)

@Serializable
data class ConfirmMobileChangeRequest(
    @SerialName("change_token") val changeToken: String,
)

// Response DTOs
@Serializable
data class OtpResponse(
    @SerialName("otp_id") val otpId: String,
    @SerialName("expires_in") val expiresIn: Int,
    val message: String,
)

@Serializable
data class TempTokenResponse(
    val token: String,
    @SerialName("expires_at") val expiresAt: String,
)

@Serializable
data class AuthResponse(
    @SerialName("access_token") val accessToken: String,
    @SerialName("access_token_type") val accessTokenType: String = "Bearer",
    @SerialName("access_expires_at") val accessExpiresAt: String,
    @SerialName("refresh_token") val refreshToken: String,
    @SerialName("refresh_expires_at") val refreshExpiresAt: String,
    @SerialName("device_id") val deviceId: String? = null,
    val client: ClientDto,
)

@Serializable
data class SetPinResponse(
    val token: String,
    @SerialName("token_type") val tokenType: String = "Bearer",
    @SerialName("expires_at") val expiresAt: String,
    @SerialName("expires_in") val expiresIn: Int,
    val client: ClientDto,
)

@Serializable
data class RefreshTokenResponse(
    @SerialName("access_token") val accessToken: String,
    @SerialName("refresh_token") val refreshToken: String,
    @SerialName("access_expires_at") val accessExpiresAt: String,
    @SerialName("refresh_expires_at") val refreshExpiresAt: String,
)

@Serializable
data class CreateClientResponse(
    val client: ClientDto,
)

@Serializable
data class MobileChangeStartResponse(
    @SerialName("change_token") val changeToken: String,
    @SerialName("ttl_minutes") val ttlMinutes: Int,
)

@Serializable
data class MobileChangeVerifyResponse(
    val message: String,
    @SerialName("change_token") val changeToken: String,
)

@Serializable
data class MobileChangeConfirmResponse(
    val message: String,
    val mobile: String,
)

@Serializable
data class ClientDto(
    val id: String,
    @SerialName("first_name") val firstName: String? = null,
    @SerialName("last_name") val lastName: String? = null,
    val mobile: String,
    @SerialName("profile_picture") val profilePicture: String? = null,
    @SerialName("personal_details") val personalDetails: PersonalDetailsDto? = null,
    val address: AddressDto? = null,
    val documents: DocumentsDto? = null,
    @SerialName("next_of_kin") val nextOfKin: NextOfKinDto? = null,
    @SerialName("client_type") val clientType: String = "PRIVATE_SECTOR_EMPLOYEE",
    @SerialName("verification_status") val verificationStatus: String = "UNVERIFIED",
    @SerialName("can_apply_for_loan") val canApplyForLoan: Boolean = false,
    @SerialName("account_status") val accountStatus: String = "INCOMPLETE",
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
)

@Serializable
data class PersonalDetailsDto(
    @SerialName("first_name") val firstName: String,
    @SerialName("last_name") val lastName: String,
    @SerialName("date_of_birth") val dateOfBirth: String,
    val gender: String,
    val nationality: String,
    val occupation: String,
    @SerialName("monthly_income") val monthlyIncome: Double,
    @SerialName("last_updated") val lastUpdated: String? = null,
)

@Serializable
data class AddressDto(
    @SerialName("street_address") val streetAddress: String,
    val suburb: String,
    val city: String,
    val province: String,
    @SerialName("postal_code") val postalCode: String,
    @SerialName("residence_type") val residenceType: String,
    @SerialName("last_updated") val lastUpdated: String? = null,
)

@Serializable
data class DocumentsDto(
    @SerialName("proof_of_residence") val proofOfResidence: DocumentDto? = null,
    @SerialName("national_id") val nationalId: DocumentDto? = null,
)

@Serializable
data class DocumentDto(
    val id: String,
    val url: String,
    @SerialName("file_name") val fileName: String,
    @SerialName("file_size") val fileSize: String,
    @SerialName("upload_date") val uploadDate: String,
    @SerialName("last_updated") val lastUpdated: String,
    @SerialName("verification_status") val verificationStatus: String,
    @SerialName("verification_date") val verificationDate: String? = null,
    @SerialName("verification_notes") val verificationNotes: String? = null,
    @SerialName("document_type") val documentType: String,
)

@Serializable
data class NextOfKinDto(
    val id: String,
    @SerialName("user_id") val userId: String,
    @SerialName("full_name") val fullName: String,
    val relationship: String,
    @SerialName("phone_number") val phoneNumber: String,
    val address: AddressDto,
    val documents: DocumentsDto? = null,
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String,
)

// Error Response DTO
@Serializable
data class ErrorResponse(
    val message: String,
    val errors: Map<String, List<String>>? = null,
    val code: String? = null,
)
