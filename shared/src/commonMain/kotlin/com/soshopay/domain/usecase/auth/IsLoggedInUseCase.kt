package com.soshopay.domain.usecase.auth

import com.soshopay.domain.repository.AuthRepository

class IsLoggedInUseCase(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(): Boolean = authRepository.isLoggedIn()
}
