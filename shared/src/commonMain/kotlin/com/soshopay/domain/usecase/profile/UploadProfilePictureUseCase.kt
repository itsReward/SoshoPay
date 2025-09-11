package com.soshopay.domain.usecase.profile

import com.soshopay.domain.repository.ProfileRepository
import com.soshopay.domain.util.Result

class UploadProfilePictureUseCase(
    private val profileRepository: ProfileRepository,
) {
    suspend operator fun invoke(
        imageBytes: ByteArray,
        fileName: String,
    ): Result<String> = profileRepository.uploadProfilePicture(imageBytes, fileName)
}
