package com.soshopay.domain.usecase.profile

import com.soshopay.domain.model.User
import com.soshopay.domain.repository.ProfileRepository
import com.soshopay.domain.util.Result
import kotlinx.coroutines.flow.Flow

class GetUserProfileUseCase(
    private val profileRepository: ProfileRepository,
) {
    suspend operator fun invoke(forceRefresh: Boolean = false): Result<User> = profileRepository.getUserProfile(forceRefresh)

    fun observeProfile(): Flow<User?> = profileRepository.observeProfile()
}
