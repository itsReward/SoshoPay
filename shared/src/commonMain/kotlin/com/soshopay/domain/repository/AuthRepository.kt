package com.soshopay.domain.repository

import com.soshopay.domain.model.AuthToken
import com.soshopay.domain.model.OtpSession
import com.soshopay.domain.model.User
import com.soshopay.domain.util.Result

interface AuthRepository {
    suspend fun sendOtp(phoneNumber: String): Result<OtpSession>

    suspend fun verifyOtp(
        otpSession: OtpSession,
        enteredCode: String,
    ): Result<String>

    suspend fun setPin(
        tempToken: String,
        pin: String,
        phoneNumber: String,
    ): Result<AuthToken>

    suspend fun login(
        phoneNumber: String,
        pin: String,
    ): Result<AuthToken>

    suspend fun refreshToken(): Result<AuthToken>

    suspend fun logout(): Result<Unit>

    suspend fun isLoggedIn(): Boolean

    suspend fun getCurrentUser(): User?

    suspend fun updatePin(
        currentPin: String,
        newPin: String,
    ): Result<Unit>

    suspend fun createClient(
        firstName: String,
        lastName: String,
        phoneNumber: String,
        pin: String,
    ): Result<User>

    suspend fun startMobileChange(newMobile: String): Result<String>

    suspend fun verifyMobileChange(
        changeToken: String,
        otp: String,
    ): Result<String>

    suspend fun confirmMobileChange(changeToken: String): Result<String>
}
