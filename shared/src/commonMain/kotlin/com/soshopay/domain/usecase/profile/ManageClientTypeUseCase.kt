package com.soshopay.domain.usecase.profile

import com.soshopay.domain.repository.ProfileRepository
import com.soshopay.domain.util.Result

class ManageClientTypeUseCase(
    private val profileRepository: ProfileRepository,
) {
    suspend fun getAvailableClientTypes(): Result<List<String>> = profileRepository.getAvailableClientTypes()

    suspend fun requestClientTypeChange(newType: String): Result<Unit> = profileRepository.requestClientTypeChange(newType)
}
