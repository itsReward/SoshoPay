package com.soshopay.domain.usecase.auth

import com.soshopay.domain.model.User
import com.soshopay.domain.repository.AuthRepository
import com.soshopay.domain.util.Result

class CreateClientUseCase(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(
        firstName: String,
        lastName: String,
        phoneNumber: String,
        pin: String,
    ): Result<User> = authRepository.createClient(firstName, lastName, phoneNumber, pin)
}
