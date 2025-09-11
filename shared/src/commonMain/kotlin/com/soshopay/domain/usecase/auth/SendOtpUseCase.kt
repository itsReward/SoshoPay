package com.soshopay.domain.usecase.auth

import com.soshopay.domain.model.OtpSession
import com.soshopay.domain.repository.AuthRepository
import com.soshopay.domain.util.Result

class SendOtpUseCase(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(phoneNumber: String): Result<OtpSession> = authRepository.sendOtp(phoneNumber)
}
