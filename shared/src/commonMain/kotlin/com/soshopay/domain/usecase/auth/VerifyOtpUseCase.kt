package com.soshopay.domain.usecase.auth

import com.soshopay.domain.model.OtpSession
import com.soshopay.domain.repository.AuthRepository
import com.soshopay.domain.util.Result

class VerifyOtpUseCase(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(
        otpSession: OtpSession,
        enteredCode: String,
    ): Result<String> = authRepository.verifyOtp(otpSession, enteredCode)
}
