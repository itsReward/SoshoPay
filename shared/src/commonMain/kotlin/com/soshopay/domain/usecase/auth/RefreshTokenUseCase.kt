package com.soshopay.domain.usecase.auth

import com.soshopay.domain.model.AuthToken
import com.soshopay.domain.repository.AuthRepository
import com.soshopay.domain.util.Result

class RefreshTokenUseCase(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(): Result<AuthToken> = authRepository.refreshToken()
}
