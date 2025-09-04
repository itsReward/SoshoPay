package com.soshopay.domain.repository

import com.soshopay.domain.model.User

interface AuthRepository {
    suspend fun sendOtp(phoneNumber: String): Result<OtpResponse>
    suspend fun verifyOtp(otpId: String, otpCode: String): Result<TempToken>
    suspend fun setPassword(tempToken: String, password: String): Result<AuthResult>
    suspend fun getCurrentUser(): Result<User>
    suspend fun logout(): Result<Unit>
}

data class OtpResponse(
    val otpId: String,
    val expiresIn: Int,
    val message: String
)

data class TempToken(
    val token: String,
    val expiresAt: Long
)

data class AuthResult(
    val user: User,
    val accessToken: String
)
