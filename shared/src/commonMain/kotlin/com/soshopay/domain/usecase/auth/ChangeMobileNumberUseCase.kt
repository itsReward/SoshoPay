package com.soshopay.domain.usecase.auth

import com.soshopay.domain.repository.AuthRepository
import com.soshopay.domain.util.Result

class ChangeMobileNumberUseCase(
    private val authRepository: AuthRepository,
) {
    suspend fun startMobileChange(newMobile: String): Result<String> = authRepository.startMobileChange(newMobile)

    suspend fun verifyMobileChange(
        changeToken: String,
        otp: String,
    ): Result<String> = authRepository.verifyMobileChange(changeToken, otp)

    suspend fun confirmMobileChange(changeToken: String): Result<String> = authRepository.confirmMobileChange(changeToken)
}
