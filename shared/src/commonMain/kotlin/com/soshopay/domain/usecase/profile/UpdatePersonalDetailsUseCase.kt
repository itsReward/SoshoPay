package com.soshopay.domain.usecase.profile

import com.soshopay.domain.model.PersonalDetails
import com.soshopay.domain.repository.ProfileRepository
import com.soshopay.domain.util.Result

class UpdatePersonalDetailsUseCase(
    private val profileRepository: ProfileRepository,
) {
    suspend operator fun invoke(personalDetails: PersonalDetails): Result<Unit> = profileRepository.updatePersonalDetails(personalDetails)
}
