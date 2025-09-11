package com.soshopay.domain.usecase.auth

import com.soshopay.domain.repository.AuthRepository
import com.soshopay.domain.util.Result

class UpdatePinUseCase(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(
        currentPin: String,
        newPin: String,
    ): Result<Unit> = authRepository.updatePin(currentPin, newPin)
}
