package com.soshopay.data.repository

import com.soshopay.data.mapper.ProfileMapper
import com.soshopay.data.remote.api.ProfileApiService
import com.soshopay.domain.model.*
import com.soshopay.domain.repository.ProfileRepository
import com.soshopay.domain.storage.ProfileCache
import com.soshopay.domain.util.*
import kotlinx.coroutines.flow.Flow

class ProfileRepositoryImpl(
    private val profileApiService: ProfileApiService,
    private val profileCache: ProfileCache,
) : ProfileRepository {
    override suspend fun getUserProfile(forceRefresh: Boolean): Result<User> {
        // Check cache first if not forcing refresh
        if (!forceRefresh) {
            val cachedUser = profileCache.getCurrentUser()
            val isCacheValid = profileCache.isProfileCacheValid(maxAgeHours = 24)

            if (cachedUser != null && isCacheValid) {
                Logger.d("Returning cached user profile", "PROFILE")
                return Result.Success(cachedUser)
            }
        }

        Logger.logProfileEvent("PROFILE_FETCH_STARTED", "current")

        return when (val result = profileApiService.getUserProfile()) {
            is Result.Success -> {
                val user = ProfileMapper.mapToUser(result.data)

                // Update cache
                profileCache.saveUser(user)
                profileCache.setLastProfileSync(
                    kotlinx.datetime.Clock.System
                        .now()
                        .toEpochMilliseconds(),
                )

                Logger.logProfileEvent("PROFILE_FETCH_SUCCESS", user.id)
                Result.Success(user)
            }
            is Result.Error -> {
                Logger.logProfileEvent("PROFILE_FETCH_FAILED", "current")

                // Return cached user if available as fallback
                val cachedUser = profileCache.getCurrentUser()
                if (cachedUser != null) {
                    Logger.d("API failed, returning cached user profile", "PROFILE")
                    Result.Success(cachedUser)
                } else {
                    result
                }
            }
            is Result.Loading -> result
        }
    }

    override suspend fun updatePersonalDetails(personalDetails: PersonalDetails): Result<Unit> {
        // Validate personal details
        val validationResult =
            ValidationUtils.PersonalDetails.validatePersonalDetails(
                firstName = personalDetails.firstName,
                lastName = personalDetails.lastName,
                dateOfBirth = personalDetails.dateOfBirth,
                gender = personalDetails.gender,
                nationality = personalDetails.nationality,
                occupation = personalDetails.occupation,
                monthlyIncome = personalDetails.monthlyIncome,
            )

        if (!validationResult.isValid) {
            return Result.Error(SoshoPayException.ValidationException(validationResult.getAllErrorMessages()))
        }

        val dto = ProfileMapper.mapToPersonalDetailsDto(personalDetails)

        Logger.logProfileEvent("PERSONAL_DETAILS_UPDATE_STARTED", "current")

        return when (val result = profileApiService.updatePersonalDetails(dto)) {
            is Result.Success -> {
                // Update cached user
                val currentUser = profileCache.getCurrentUser()
                currentUser?.let { user ->
                    val updatedUser =
                        user.copy(
                            personalDetails = personalDetails,
                            verificationStatus = VerificationStatus.UNVERIFIED, // Reset verification status
                        )
                    profileCache.saveUser(updatedUser)
                }

                Logger.logProfileEvent("PERSONAL_DETAILS_UPDATE_SUCCESS", "current")
                result
            }
            is Result.Error -> {
                Logger.logProfileEvent("PERSONAL_DETAILS_UPDATE_FAILED", "current")
                result
            }
            is Result.Loading -> result
        }
    }

    override suspend fun updateAddress(address: Address): Result<Unit> {
        // Validate address
        val validationResult =
            ValidationUtils.Address.validateAddress(
                streetAddress = address.streetAddress,
                suburb = address.suburb,
                city = address.city,
                province = address.province,
                postalCode = address.postalCode,
                residenceType = address.residenceType,
            )

        if (!validationResult.isValid) {
            return Result.Error(SoshoPayException.ValidationException(validationResult.getAllErrorMessages()))
        }

        val dto = ProfileMapper.mapToAddressDto(address)

        Logger.logProfileEvent("ADDRESS_UPDATE_STARTED", "current")

        return when (val result = profileApiService.updateAddress(dto)) {
            is Result.Success -> {
                // Update cached user
                val currentUser = profileCache.getCurrentUser()
                currentUser?.let { user ->
                    val updatedUser =
                        user.copy(
                            address = address,
                            verificationStatus = VerificationStatus.UNVERIFIED, // Reset verification status
                        )
                    profileCache.saveUser(updatedUser)
                }

                Logger.logProfileEvent("ADDRESS_UPDATE_SUCCESS", "current")
                result
            }
            is Result.Error -> {
                Logger.logProfileEvent("ADDRESS_UPDATE_FAILED", "current")
                result
            }
            is Result.Loading -> result
        }
    }

    override suspend fun uploadProfilePicture(
        imageBytes: ByteArray,
        fileName: String,
    ): Result<String> {
        // Validate file
        val sizeError = ValidationUtils.File.getFileSizeError(imageBytes.size.toLong())
        if (sizeError != null) {
            return Result.Error(SoshoPayException.FileSizeExceededException(sizeError))
        }

        val typeError = ValidationUtils.File.getFileTypeError(fileName, isDocument = false)
        if (typeError != null) {
            return Result.Error(SoshoPayException.UnsupportedFileTypeException(typeError))
        }

        Logger.logFileUpload(fileName, imageBytes.size.toLong(), false)

        return when (val result = profileApiService.uploadProfilePicture(imageBytes, fileName)) {
            is Result.Success -> {
                // Update cached user
                val currentUser = profileCache.getCurrentUser()
                currentUser?.let { user ->
                    val profilePicture =
                        ProfilePicture(
                            url = result.data,
                            uploadDate =
                                kotlinx.datetime.Clock.System
                                    .now()
                                    .toEpochMilliseconds(),
                            lastUpdated =
                                kotlinx.datetime.Clock.System
                                    .now()
                                    .toEpochMilliseconds(),
                            fileName = fileName,
                            fileSize = "${imageBytes.size / 1024}KB",
                        )
                    val updatedUser = user.copy(profilePicture = profilePicture)
                    profileCache.saveUser(updatedUser)
                }

                Logger.logFileUpload(fileName, imageBytes.size.toLong(), true)
                result
            }
            is Result.Error -> {
                Logger.logFileUpload(fileName, imageBytes.size.toLong(), false)
                result
            }
            is Result.Loading -> result
        }
    }

    override suspend fun uploadDocuments(documents: Map<DocumentType, Pair<ByteArray, String>>): Result<Unit> {
        // Validate all documents
        for ((type, fileData) in documents) {
            val (bytes, fileName) = fileData

            val sizeError = ValidationUtils.File.getFileSizeError(bytes.size.toLong())
            if (sizeError != null) {
                return Result.Error(SoshoPayException.FileSizeExceededException("$type: $sizeError"))
            }

            val typeError = ValidationUtils.File.getFileTypeError(fileName, isDocument = true)
            if (typeError != null) {
                return Result.Error(SoshoPayException.UnsupportedFileTypeException("$type: $typeError"))
            }
        }

        // Map to API format
        val apiDocuments =
            documents.mapKeys { (type, _) ->
                when (type) {
                    DocumentType.PROOF_OF_RESIDENCE -> "proof_of_residence"
                    DocumentType.NATIONAL_ID -> "national_id"
                    DocumentType.PROFILE_PICTURE -> "profile_picture"
                }
            }

        Logger.logProfileEvent("DOCUMENTS_UPLOAD_STARTED", "current")

        return when (val result = profileApiService.uploadDocuments(apiDocuments)) {
            is Result.Success -> {
                // Update cached user - set documents as pending verification
                val currentUser = profileCache.getCurrentUser()
                currentUser?.let { user ->
                    val updatedDocuments =
                        Documents(
                            proofOfResidence =
                                if (documents.containsKey(DocumentType.PROOF_OF_RESIDENCE)) {
                                    createPendingDocument(DocumentType.PROOF_OF_RESIDENCE, documents[DocumentType.PROOF_OF_RESIDENCE]!!)
                                } else {
                                    user.documents?.proofOfResidence
                                },
                            nationalId =
                                if (documents.containsKey(DocumentType.NATIONAL_ID)) {
                                    createPendingDocument(DocumentType.NATIONAL_ID, documents[DocumentType.NATIONAL_ID]!!)
                                } else {
                                    user.documents?.nationalId
                                },
                        )

                    val updatedUser =
                        user.copy(
                            documents = updatedDocuments,
                            verificationStatus = VerificationStatus.PENDING,
                        )
                    profileCache.saveUser(updatedUser)
                }

                Logger.logProfileEvent("DOCUMENTS_UPLOAD_SUCCESS", "current")
                result
            }
            is Result.Error -> {
                Logger.logProfileEvent("DOCUMENTS_UPLOAD_FAILED", "current")
                result
            }
            is Result.Loading -> result
        }
    }

    override suspend fun replaceDocument(
        documentType: DocumentType,
        document: ByteArray,
        fileName: String,
    ): Result<Unit> {
        // Validate document
        val sizeError = ValidationUtils.File.getFileSizeError(document.size.toLong())
        if (sizeError != null) {
            return Result.Error(SoshoPayException.FileSizeExceededException(sizeError))
        }

        val typeError = ValidationUtils.File.getFileTypeError(fileName, isDocument = true)
        if (typeError != null) {
            return Result.Error(SoshoPayException.UnsupportedFileTypeException(typeError))
        }

        val apiDocumentType =
            when (documentType) {
                DocumentType.PROOF_OF_RESIDENCE -> "proof_of_residence"
                DocumentType.NATIONAL_ID -> "national_id"
                DocumentType.PROFILE_PICTURE -> "profile_picture"
            }

        Logger.logProfileEvent("DOCUMENT_REPLACE_STARTED", "current")

        return when (val result = profileApiService.replaceDocument(apiDocumentType, document, fileName)) {
            is Result.Success -> {
                // Update cached user
                val currentUser = profileCache.getCurrentUser()
                currentUser?.let { user ->
                    val newDocument = createPendingDocument(documentType, Pair(document, fileName))
                    val updatedDocuments =
                        when (documentType) {
                            DocumentType.PROOF_OF_RESIDENCE -> user.documents?.copy(proofOfResidence = newDocument)
                            DocumentType.NATIONAL_ID -> user.documents?.copy(nationalId = newDocument)
                            DocumentType.PROFILE_PICTURE -> user.documents // Profile picture handled separately
                        } ?: Documents(
                            proofOfResidence = if (documentType == DocumentType.PROOF_OF_RESIDENCE) newDocument else null,
                            nationalId = if (documentType == DocumentType.NATIONAL_ID) newDocument else null,
                        )

                    val updatedUser =
                        user.copy(
                            documents = updatedDocuments,
                            verificationStatus = VerificationStatus.PENDING,
                        )
                    profileCache.saveUser(updatedUser)
                }

                Logger.logProfileEvent("DOCUMENT_REPLACE_SUCCESS", "current")
                result
            }
            is Result.Error -> {
                Logger.logProfileEvent("DOCUMENT_REPLACE_FAILED", "current")
                result
            }
            is Result.Loading -> result
        }
    }

    override suspend fun getDocuments(): Result<Documents> =
        when (val result = profileApiService.getDocuments()) {
            is Result.Success -> {
                val documents = ProfileMapper.mapToDocuments(result.data)

                // Update cached user
                val currentUser = profileCache.getCurrentUser()
                currentUser?.let { user ->
                    val updatedUser = user.copy(documents = documents)
                    profileCache.saveUser(updatedUser)
                }

                Result.Success(documents)
            }
            is Result.Error -> {
                // Return cached documents if available
                val cachedUser = profileCache.getCurrentUser()
                cachedUser?.documents?.let { documents ->
                    Logger.d("API failed, returning cached documents", "PROFILE")
                    Result.Success(documents)
                } ?: result
            }
            is Result.Loading -> result
        }

    override suspend fun updateNextOfKin(nextOfKin: NextOfKin): Result<Unit> {
        // Validate next of kin data
        val addressValidation =
            ValidationUtils.Address.validateAddress(
                streetAddress = nextOfKin.address.streetAddress,
                suburb = nextOfKin.address.suburb,
                city = nextOfKin.address.city,
                province = nextOfKin.address.province,
                postalCode = nextOfKin.address.postalCode,
                residenceType = nextOfKin.address.residenceType,
            )

        if (!addressValidation.isValid) {
            return Result.Error(SoshoPayException.ValidationException("Next of Kin Address: ${addressValidation.getAllErrorMessages()}"))
        }

        val phoneError = ValidationUtils.Phone.getValidationError(nextOfKin.phoneNumber)
        if (phoneError != null) {
            return Result.Error(SoshoPayException.ValidationException("Next of Kin Phone: $phoneError"))
        }

        val dto = ProfileMapper.mapToNextOfKinDto(nextOfKin)

        Logger.logProfileEvent("NEXT_OF_KIN_UPDATE_STARTED", "current")

        return when (val result = profileApiService.updateNextOfKin(dto)) {
            is Result.Success -> {
                // Update cached user
                val currentUser = profileCache.getCurrentUser()
                currentUser?.let { user ->
                    val updatedUser = user.copy(nextOfKin = nextOfKin)
                    profileCache.saveUser(updatedUser)
                }

                Logger.logProfileEvent("NEXT_OF_KIN_UPDATE_SUCCESS", "current")
                result
            }
            is Result.Error -> {
                Logger.logProfileEvent("NEXT_OF_KIN_UPDATE_FAILED", "current")
                result
            }
            is Result.Loading -> result
        }
    }

    override suspend fun getNextOfKin(): Result<NextOfKin> =
        when (val result = profileApiService.getNextOfKin()) {
            is Result.Success -> {
                val nextOfKin = ProfileMapper.mapToNextOfKin(result.data)

                // Update cached user
                val currentUser = profileCache.getCurrentUser()
                currentUser?.let { user ->
                    val updatedUser = user.copy(nextOfKin = nextOfKin)
                    profileCache.saveUser(updatedUser)
                }

                Result.Success(nextOfKin)
            }
            is Result.Error -> {
                // Return cached next of kin if available
                val cachedUser = profileCache.getCurrentUser()
                cachedUser?.nextOfKin?.let { nextOfKin ->
                    Logger.d("API failed, returning cached next of kin", "PROFILE")
                    Result.Success(nextOfKin)
                } ?: result
            }
            is Result.Loading -> result
        }

    override suspend fun getAvailableClientTypes(): Result<List<String>> = profileApiService.getClientTypes()

    override suspend fun requestClientTypeChange(newType: String): Result<Unit> {
        val availableTypesResult = getAvailableClientTypes()
        if (availableTypesResult is Result.Success) {
            if (!availableTypesResult.data.contains(newType)) {
                return Result.Error(SoshoPayException.ValidationException("Invalid client type: $newType"))
            }
        }

        Logger.logProfileEvent("CLIENT_TYPE_CHANGE_STARTED", "current")

        return when (val result = profileApiService.updateClientType(newType)) {
            is Result.Success -> {
                // Update cached user
                val currentUser = profileCache.getCurrentUser()
                currentUser?.let { user ->
                    val updatedClientType =
                        ClientType(
                            current = user.clientType.current,
                            pending = newType,
                            approvalStatus = ApprovalStatus.PENDING,
                            lastChanged =
                                kotlinx.datetime.Clock.System
                                    .now()
                                    .toEpochMilliseconds(),
                            requiresAdminApproval = true,
                        )
                    val updatedUser = user.copy(clientType = updatedClientType)
                    profileCache.saveUser(updatedUser)
                }

                Logger.logProfileEvent("CLIENT_TYPE_CHANGE_SUCCESS", "current")
                result
            }
            is Result.Error -> {
                Logger.logProfileEvent("CLIENT_TYPE_CHANGE_FAILED", "current")
                result
            }
            is Result.Loading -> result
        }
    }

    override suspend fun syncProfile(): Result<Unit> = getUserProfile(forceRefresh = true).map { }

    override fun observeProfile(): Flow<User?> = profileCache.observeUser()

    private fun createPendingDocument(
        type: DocumentType,
        fileData: Pair<ByteArray, String>,
    ): Document {
        val (bytes, fileName) = fileData
        val currentTime =
            kotlinx.datetime.Clock.System
                .now()
                .toEpochMilliseconds()

        return Document(
            id = generateDocumentId(),
            url = "", // Will be set by server
            fileName = fileName,
            fileSize = "${bytes.size / 1024}KB",
            uploadDate = currentTime,
            lastUpdated = currentTime,
            verificationStatus = VerificationStatus.PENDING,
            verificationDate = null,
            verificationNotes = null,
            documentType = type,
        )
    }

    private fun generateDocumentId(): String = "doc_${kotlinx.datetime.Clock.System.now().toEpochMilliseconds()}_${(1000..9999).random()}"
}
