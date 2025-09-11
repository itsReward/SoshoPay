package com.soshopay.domain.usecase.profile

import com.soshopay.domain.model.NextOfKin
import com.soshopay.domain.repository.ProfileRepository
import com.soshopay.domain.util.Result

class ManageNextOfKinUseCase(
    private val profileRepository: ProfileRepository,
) {
    suspend fun updateNextOfKin(nextOfKin: NextOfKin): Result<Unit> = profileRepository.updateNextOfKin(nextOfKin)

    suspend fun getNextOfKin(): Result<NextOfKin> = profileRepository.getNextOfKin()
}
