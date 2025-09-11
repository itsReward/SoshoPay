package com.soshopay.domain.usecase.profile

import com.soshopay.domain.model.Address
import com.soshopay.domain.repository.ProfileRepository
import com.soshopay.domain.util.Result

class UpdateAddressUseCase(
    private val profileRepository: ProfileRepository,
) {
    suspend operator fun invoke(address: Address): Result<Unit> = profileRepository.updateAddress(address)
}
