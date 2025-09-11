package com.soshopay.domain.usecase.auth

import com.soshopay.domain.repository.AuthRepository
import com.soshopay.domain.util.Result

class LogoutUseCase(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(): Result<Unit> = authRepository.logout()
}
