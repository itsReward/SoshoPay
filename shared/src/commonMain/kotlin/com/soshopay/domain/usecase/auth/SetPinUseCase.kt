package com.soshopay.domain.usecase.auth

import com.soshopay.domain.model.AuthToken
import com.soshopay.domain.repository.AuthRepository
import com.soshopay.domain.util.Result

class SetPinUseCase(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(
        tempToken: String,
        pin: String,
        phoneNumber: String,
    ): Result<AuthToken> = authRepository.setPin(tempToken, pin, phoneNumber)
}
