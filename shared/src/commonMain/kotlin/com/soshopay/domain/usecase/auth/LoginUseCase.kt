package com.soshopay.domain.usecase.auth

import com.soshopay.domain.model.AuthToken
import com.soshopay.domain.repository.AuthRepository
import com.soshopay.domain.util.Result

class LoginUseCase(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(
        phoneNumber: String,
        pin: String,
    ): Result<AuthToken> = authRepository.login(phoneNumber, pin)
}
