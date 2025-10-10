package com.soshopay.android.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.soshopay.android.ui.state.AddressEditState
import com.soshopay.android.ui.state.DocumentUploadState
import com.soshopay.android.ui.state.NextOfKinEditState
import com.soshopay.android.ui.state.PersonalDetailsEditState
import com.soshopay.android.ui.state.ProfileEvent
import com.soshopay.android.ui.state.ProfileNavigation
import com.soshopay.android.ui.state.ProfilePictureState
import com.soshopay.android.ui.state.ProfileScreenState
import com.soshopay.domain.model.Address
import com.soshopay.domain.model.DocumentType
import com.soshopay.domain.model.NextOfKin
import com.soshopay.domain.model.PersonalDetails
import com.soshopay.domain.usecase.auth.LogoutUseCase
import com.soshopay.domain.usecase.profile.GetUserProfileUseCase
import com.soshopay.domain.usecase.profile.ManageClientTypeUseCase
import com.soshopay.domain.usecase.profile.ManageNextOfKinUseCase
import com.soshopay.domain.usecase.profile.UpdateAddressUseCase
import com.soshopay.domain.usecase.profile.UpdatePersonalDetailsUseCase
import com.soshopay.domain.usecase.profile.UploadDocumentUseCase
import com.soshopay.domain.usecase.profile.UploadProfilePictureUseCase
import com.soshopay.domain.util.Result
import com.soshopay.domain.util.SoshoPayException
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Comprehensive ViewModel for profile management following MVVM and Clean Architecture patterns.
 *
 * This ViewModel coordinates all profile workflows including viewing, editing personal details,
 * address, next of kin, profile picture, documents, client type management, and logout.
 * It depends only on Use Cases (Domain layer) and manages UI state reactively.
 *
 * Key principles followed:
 * - Single Responsibility: Each method handles one specific profile operation
 * - Dependency Inversion: Depends on Use Case abstractions, not implementations
 * - Open/Closed: Extensible for new profile features without modifying existing code
 * - Interface Segregation: Uses focused Use Cases rather than monolithic services
 *
 * @param getUserProfileUseCase Use case for retrieving user profile
 * @param updatePersonalDetailsUseCase Use case for updating personal details
 * @param updateAddressUseCase Use case for updating address
 * @param manageNextOfKinUseCase Use case for managing next of kin
 * @param uploadProfilePictureUseCase Use case for uploading profile picture
 * @param uploadDocumentUseCase Use case for uploading documents
 * @param manageClientTypeUseCase Use case for managing client type
 * @param logoutUseCase Use case for logout
 */
class ProfileViewModel(
    private val getUserProfileUseCase: GetUserProfileUseCase,
    private val updatePersonalDetailsUseCase: UpdatePersonalDetailsUseCase,
    private val updateAddressUseCase: UpdateAddressUseCase,
    private val manageNextOfKinUseCase: ManageNextOfKinUseCase,
    private val uploadProfilePictureUseCase: UploadProfilePictureUseCase,
    private val uploadDocumentUseCase: UploadDocumentUseCase,
    private val manageClientTypeUseCase: ManageClientTypeUseCase,
    private val logoutUseCase: LogoutUseCase,
) : ViewModel() {
    // ========== STATE MANAGEMENT ==========

    private val _profileScreenState = MutableStateFlow(ProfileScreenState())
    val profileScreenState: StateFlow<ProfileScreenState> = _profileScreenState.asStateFlow()

    private val _personalDetailsEditState = MutableStateFlow(PersonalDetailsEditState())
    val personalDetailsEditState: StateFlow<PersonalDetailsEditState> = _personalDetailsEditState.asStateFlow()

    private val _addressEditState = MutableStateFlow(AddressEditState())
    val addressEditState: StateFlow<AddressEditState> = _addressEditState.asStateFlow()

    private val _nextOfKinEditState = MutableStateFlow(NextOfKinEditState())
    val nextOfKinEditState: StateFlow<NextOfKinEditState> = _nextOfKinEditState.asStateFlow()

    private val _profilePictureState = MutableStateFlow(ProfilePictureState())
    val profilePictureState: StateFlow<ProfilePictureState> = _profilePictureState.asStateFlow()

    private val _documentUploadState = MutableStateFlow(DocumentUploadState())
    val documentUploadState: StateFlow<DocumentUploadState> = _documentUploadState.asStateFlow()

    private val _navigationEvents = MutableSharedFlow<ProfileNavigation>()
    val navigationEvents: SharedFlow<ProfileNavigation> = _navigationEvents.asSharedFlow()

    // ========== INITIALIZATION ==========

    init {
        loadProfile()
    }

    // ========== EVENT HANDLING ==========

    fun onEvent(event: ProfileEvent) {
        when (event) {
            // Profile View Events
            is ProfileEvent.LoadProfile -> loadProfile()
            is ProfileEvent.RefreshProfile -> refreshProfile()
            is ProfileEvent.ToggleEditMode -> toggleEditMode()
            is ProfileEvent.CancelEdit -> cancelEdit()

            // Personal Details Events
            is ProfileEvent.UpdateFirstName -> updateFirstName(event.firstName)
            is ProfileEvent.UpdateLastName -> updateLastName(event.lastName)
            is ProfileEvent.UpdateDateOfBirth -> updateDateOfBirth(event.dateOfBirth)
            is ProfileEvent.UpdateGender -> updateGender(event.gender)
            is ProfileEvent.UpdateNationality -> updateNationality(event.nationality)
            is ProfileEvent.UpdateOccupation -> updateOccupation(event.occupation)
            is ProfileEvent.UpdateMonthlyIncome -> updateMonthlyIncome(event.income)
            is ProfileEvent.SavePersonalDetails -> savePersonalDetails()

            // Address Events
            is ProfileEvent.UpdateStreetAddress -> updateStreetAddress(event.address)
            is ProfileEvent.UpdateSuburb -> updateSuburb(event.suburb)
            is ProfileEvent.UpdateCity -> updateCity(event.city)
            is ProfileEvent.UpdateProvince -> updateProvince(event.province)
            is ProfileEvent.UpdatePostalCode -> updatePostalCode(event.postalCode)
            is ProfileEvent.UpdateResidenceType -> updateResidenceType(event.type)
            is ProfileEvent.SaveAddress -> saveAddress()

            // Next of Kin Events
            is ProfileEvent.UpdateNextOfKinFullName -> updateNextOfKinFullName(event.fullName)
            is ProfileEvent.UpdateNextOfKinRelationship -> updateNextOfKinRelationship(event.relationship)
            is ProfileEvent.UpdateNextOfKinPhoneNumber -> updateNextOfKinPhoneNumber(event.phoneNumber)
            is ProfileEvent.UpdateNextOfKinStreetAddress -> updateNextOfKinStreetAddress(event.address)
            is ProfileEvent.UpdateNextOfKinSuburb -> updateNextOfKinSuburb(event.suburb)
            is ProfileEvent.UpdateNextOfKinCity -> updateNextOfKinCity(event.city)
            is ProfileEvent.UpdateNextOfKinProvince -> updateNextOfKinProvince(event.province)
            is ProfileEvent.UpdateNextOfKinPostalCode -> updateNextOfKinPostalCode(event.postalCode)
            is ProfileEvent.SaveNextOfKin -> saveNextOfKin()

            // Profile Picture Events
            is ProfileEvent.UploadProfilePicture -> uploadProfilePicture(event.imageBytes, event.fileName)

            // Document Events
            is ProfileEvent.UploadNationalId -> uploadNationalId(event.documentBytes, event.fileName)
            is ProfileEvent.UploadProofOfResidence -> uploadProofOfResidence(event.documentBytes, event.fileName)

            // Client Type Events
            is ProfileEvent.ShowClientTypeDialog -> showClientTypeDialog()
            is ProfileEvent.DismissClientTypeDialog -> dismissClientTypeDialog()
            is ProfileEvent.RequestClientTypeChange -> requestClientTypeChange(event.newType)

            // Logout Events
            is ProfileEvent.ShowLogoutDialog -> showLogoutDialog()
            is ProfileEvent.DismissLogoutDialog -> dismissLogoutDialog()
            is ProfileEvent.ConfirmLogout -> confirmLogout()

            // Common Events
            is ProfileEvent.ClearError -> clearError()
            is ProfileEvent.NavigateBack -> navigateBack()
        }
    }

    // ========== PROFILE OPERATIONS ==========

    private fun loadProfile() {
        viewModelScope.launch {
            _profileScreenState.value = _profileScreenState.value.copy(isLoading = true)

            when (val result = getUserProfileUseCase(forceRefresh = false)) {
                is Result.Success -> {
                    _profileScreenState.value =
                        _profileScreenState.value.copy(
                            user = result.data,
                            isLoading = false,
                            errorMessage = null,
                        )
                    initializeEditStates(result.data)
                }
                is Result.Error -> {
                    _profileScreenState.value =
                        _profileScreenState.value.copy(
                            isLoading = false,
                            errorMessage = result.exception.message ?: "Failed to load profile",
                        )
                }
                is Result.Loading -> {
                    _profileScreenState.value = _profileScreenState.value.copy(isLoading = true)
                }
            }
        }
    }

    private fun refreshProfile() {
        viewModelScope.launch {
            _profileScreenState.value = _profileScreenState.value.copy(isLoading = true)

            when (val result = getUserProfileUseCase(forceRefresh = true)) {
                is Result.Success -> {
                    _profileScreenState.value =
                        _profileScreenState.value.copy(
                            user = result.data,
                            isLoading = false,
                            errorMessage = null,
                        )
                    initializeEditStates(result.data)
                }
                is Result.Error -> {
                    _profileScreenState.value =
                        _profileScreenState.value.copy(
                            isLoading = false,
                            errorMessage = result.exception.message ?: "Failed to refresh profile",
                        )
                }

                Result.Loading -> {
                    _profileScreenState.value = _profileScreenState.value.copy(isLoading = true)
                }
            }
        }
    }

    private fun initializeEditStates(user: com.soshopay.domain.model.User) {
        // Initialize personal details
        user.personalDetails?.let { details ->
            _personalDetailsEditState.value =
                PersonalDetailsEditState(
                    firstName = details.firstName,
                    lastName = details.lastName,
                    dateOfBirth = details.dateOfBirth.toString(),
                    gender = details.gender,
                    nationality = details.nationality,
                    occupation = details.occupation,
                    monthlyIncome = details.monthlyIncome.toString(),
                )
        }

        // Initialize address
        user.address?.let { address ->
            _addressEditState.value =
                AddressEditState(
                    streetAddress = address.streetAddress,
                    suburb = address.suburb,
                    city = address.city,
                    province = address.province,
                    postalCode = address.postalCode,
                    residenceType = address.residenceType,
                )
        }

        // Initialize next of kin
        user.nextOfKin?.let { kin ->
            _nextOfKinEditState.value =
                NextOfKinEditState(
                    fullName = kin.fullName,
                    relationship = kin.relationship,
                    phoneNumber = kin.phoneNumber,
                    address =
                        AddressEditState(
                            streetAddress = kin.address.streetAddress,
                            suburb = kin.address.suburb,
                            city = kin.address.city,
                            province = kin.address.province,
                            postalCode = kin.address.postalCode,
                            residenceType = kin.address.residenceType,
                        ),
                )
        }

        // Initialize documents
        user.documents?.let { docs ->
            _documentUploadState.value =
                DocumentUploadState(
                    nationalId = docs.nationalId,
                    proofOfResidence = docs.proofOfResidence,
                )
        }

        // Initialize profile picture
        user.profilePicture?.let { pic ->
            _profilePictureState.value =
                ProfilePictureState(
                    imageUrl = pic.url,
                )
        }
    }

    private fun toggleEditMode() {
        _profileScreenState.value =
            _profileScreenState.value.copy(
                isEditMode = !_profileScreenState.value.isEditMode,
            )
    }

    private fun cancelEdit() {
        _profileScreenState.value = _profileScreenState.value.copy(isEditMode = false)
        _profileScreenState.value.user?.let { initializeEditStates(it) }
    }

    // ========== PERSONAL DETAILS OPERATIONS ==========

    private fun updateFirstName(firstName: String) {
        _personalDetailsEditState.value =
            _personalDetailsEditState.value.copy(
                firstName = firstName,
                firstNameError = if (firstName.isBlank()) "First name is required" else null,
                isSaveEnabled = validatePersonalDetails(),
            )
    }

    private fun updateLastName(lastName: String) {
        _personalDetailsEditState.value =
            _personalDetailsEditState.value.copy(
                lastName = lastName,
                lastNameError = if (lastName.isBlank()) "Last name is required" else null,
                isSaveEnabled = validatePersonalDetails(),
            )
    }

    private fun updateDateOfBirth(dateOfBirth: String) {
        _personalDetailsEditState.value =
            _personalDetailsEditState.value.copy(
                dateOfBirth = dateOfBirth,
                dateOfBirthError = if (dateOfBirth.isBlank()) "Date of birth is required" else null,
                isSaveEnabled = validatePersonalDetails(),
            )
    }

    private fun updateGender(gender: String) {
        _personalDetailsEditState.value =
            _personalDetailsEditState.value.copy(
                gender = gender,
                genderError = if (gender.isBlank()) "Gender is required" else null,
                isSaveEnabled = validatePersonalDetails(),
            )
    }

    private fun updateNationality(nationality: String) {
        _personalDetailsEditState.value =
            _personalDetailsEditState.value.copy(
                nationality = nationality,
                nationalityError = if (nationality.isBlank()) "Nationality is required" else null,
                isSaveEnabled = validatePersonalDetails(),
            )
    }

    private fun updateOccupation(occupation: String) {
        _personalDetailsEditState.value =
            _personalDetailsEditState.value.copy(
                occupation = occupation,
                occupationError = if (occupation.isBlank()) "Occupation is required" else null,
                isSaveEnabled = validatePersonalDetails(),
            )
    }

    private fun updateMonthlyIncome(income: String) {
        val incomeError =
            when {
                income.isBlank() -> "Monthly income is required"
                income.toDoubleOrNull() == null -> "Invalid income amount"
                income.toDouble() <= 0 -> "Income must be greater than 0"
                else -> null
            }
        _personalDetailsEditState.value =
            _personalDetailsEditState.value.copy(
                monthlyIncome = income,
                monthlyIncomeError = incomeError,
                isSaveEnabled = validatePersonalDetails(),
            )
    }

    private fun validatePersonalDetails(): Boolean {
        val state = _personalDetailsEditState.value
        return state.firstName.isNotBlank() &&
            state.lastName.isNotBlank() &&
            state.dateOfBirth.isNotBlank() &&
            state.gender.isNotBlank() &&
            state.nationality.isNotBlank() &&
            state.occupation.isNotBlank() &&
            state.monthlyIncome.isNotBlank() &&
            state.monthlyIncome.toDoubleOrNull() != null &&
            state.monthlyIncome.toDouble() > 0
    }

    @OptIn(ExperimentalTime::class)
    private fun savePersonalDetails() {
        viewModelScope.launch {
            val state = _personalDetailsEditState.value

            if (!validatePersonalDetails()) {
                _personalDetailsEditState.value =
                    state.copy(
                        errorMessage = "Please fill in all required fields correctly",
                    )
                return@launch
            }

            _personalDetailsEditState.value = state.copy(isLoading = true)

            val personalDetails =
                PersonalDetails(
                    firstName = state.firstName,
                    lastName = state.lastName,
                    dateOfBirth = state.dateOfBirth.toLongOrNull() ?: 0L,
                    gender = state.gender,
                    nationality = state.nationality,
                    occupation = state.occupation,
                    monthlyIncome = state.monthlyIncome.toDoubleOrNull() ?: 0.0,
                    lastUpdated =
                        Clock.System
                            .now()
                            .toEpochMilliseconds(),
                )

            when (val result = updatePersonalDetailsUseCase(personalDetails)) {
                is Result.Success -> {
                    _personalDetailsEditState.value =
                        state.copy(
                            isLoading = false,
                            errorMessage = null,
                        )
                    refreshProfile()
                    toggleEditMode()
                }
                is Result.Error -> {
                    _personalDetailsEditState.value =
                        state.copy(
                            isLoading = false,
                            errorMessage = result.exception.message ?: "Failed to update personal details",
                        )
                }
                is Result.Loading -> {
                    _personalDetailsEditState.value = state.copy(isLoading = true)
                }
            }
        }
    }

    // ========== ADDRESS OPERATIONS ==========

    private fun updateStreetAddress(address: String) {
        _addressEditState.value =
            _addressEditState.value.copy(
                streetAddress = address,
                streetAddressError = if (address.isBlank()) "Street address is required" else null,
                isSaveEnabled = validateAddress(),
            )
    }

    private fun updateSuburb(suburb: String) {
        _addressEditState.value =
            _addressEditState.value.copy(
                suburb = suburb,
                suburbError = if (suburb.isBlank()) "Suburb is required" else null,
                isSaveEnabled = validateAddress(),
            )
    }

    private fun updateCity(city: String) {
        _addressEditState.value =
            _addressEditState.value.copy(
                city = city,
                cityError = if (city.isBlank()) "City is required" else null,
                isSaveEnabled = validateAddress(),
            )
    }

    private fun updateProvince(province: String) {
        _addressEditState.value =
            _addressEditState.value.copy(
                province = province,
                provinceError = if (province.isBlank()) "Province is required" else null,
                isSaveEnabled = validateAddress(),
            )
    }

    private fun updatePostalCode(postalCode: String) {
        _addressEditState.value =
            _addressEditState.value.copy(
                postalCode = postalCode,
                postalCodeError = if (postalCode.isBlank()) "Postal code is required" else null,
                isSaveEnabled = validateAddress(),
            )
    }

    private fun updateResidenceType(type: String) {
        _addressEditState.value =
            _addressEditState.value.copy(
                residenceType = type,
                residenceTypeError = if (type.isBlank()) "Residence type is required" else null,
                isSaveEnabled = validateAddress(),
            )
    }

    private fun validateAddress(): Boolean {
        val state = _addressEditState.value
        return state.streetAddress.isNotBlank() &&
            state.suburb.isNotBlank() &&
            state.city.isNotBlank() &&
            state.province.isNotBlank() &&
            state.postalCode.isNotBlank() &&
            state.residenceType.isNotBlank()
    }

    @OptIn(ExperimentalTime::class)
    private fun saveAddress() {
        viewModelScope.launch {
            val state = _addressEditState.value

            if (!validateAddress()) {
                _addressEditState.value =
                    state.copy(
                        errorMessage = "Please fill in all required fields",
                    )
                return@launch
            }

            _addressEditState.value = state.copy(isLoading = true)

            val address =
                Address(
                    streetAddress = state.streetAddress,
                    suburb = state.suburb,
                    city = state.city,
                    province = state.province,
                    postalCode = state.postalCode,
                    residenceType = state.residenceType,
                    lastUpdated =
                        Clock.System
                            .now()
                            .toEpochMilliseconds(),
                )

            when (val result = updateAddressUseCase(address)) {
                is Result.Success -> {
                    _addressEditState.value =
                        state.copy(
                            isLoading = false,
                            errorMessage = null,
                        )
                    refreshProfile()
                    toggleEditMode()
                }
                is Result.Error -> {
                    _addressEditState.value =
                        state.copy(
                            isLoading = false,
                            errorMessage = result.exception.message ?: "Failed to update address",
                        )
                }
                is Result.Loading -> {
                    _addressEditState.value = state.copy(isLoading = true)
                }
            }
        }
    }

    // ========== NEXT OF KIN OPERATIONS ==========

    private fun updateNextOfKinFullName(fullName: String) {
        _nextOfKinEditState.value =
            _nextOfKinEditState.value.copy(
                fullName = fullName,
                fullNameError = if (fullName.isBlank()) "Full name is required" else null,
                isSaveEnabled = validateNextOfKin(),
            )
    }

    private fun updateNextOfKinRelationship(relationship: String) {
        _nextOfKinEditState.value =
            _nextOfKinEditState.value.copy(
                relationship = relationship,
                relationshipError = if (relationship.isBlank()) "Relationship is required" else null,
                isSaveEnabled = validateNextOfKin(),
            )
    }

    private fun updateNextOfKinPhoneNumber(phoneNumber: String) {
        _nextOfKinEditState.value =
            _nextOfKinEditState.value.copy(
                phoneNumber = phoneNumber,
                phoneNumberError = if (phoneNumber.isBlank()) "Phone number is required" else null,
                isSaveEnabled = validateNextOfKin(),
            )
    }

    private fun updateNextOfKinStreetAddress(address: String) {
        val currentAddress = _nextOfKinEditState.value.address
        _nextOfKinEditState.value =
            _nextOfKinEditState.value.copy(
                address =
                    currentAddress.copy(
                        streetAddress = address,
                        streetAddressError = if (address.isBlank()) "Street address is required" else null,
                    ),
                isSaveEnabled = validateNextOfKin(),
            )
    }

    private fun updateNextOfKinSuburb(suburb: String) {
        val currentAddress = _nextOfKinEditState.value.address
        _nextOfKinEditState.value =
            _nextOfKinEditState.value.copy(
                address =
                    currentAddress.copy(
                        suburb = suburb,
                        suburbError = if (suburb.isBlank()) "Suburb is required" else null,
                    ),
                isSaveEnabled = validateNextOfKin(),
            )
    }

    private fun updateNextOfKinCity(city: String) {
        val currentAddress = _nextOfKinEditState.value.address
        _nextOfKinEditState.value =
            _nextOfKinEditState.value.copy(
                address =
                    currentAddress.copy(
                        city = city,
                        cityError = if (city.isBlank()) "City is required" else null,
                    ),
                isSaveEnabled = validateNextOfKin(),
            )
    }

    private fun updateNextOfKinProvince(province: String) {
        val currentAddress = _nextOfKinEditState.value.address
        _nextOfKinEditState.value =
            _nextOfKinEditState.value.copy(
                address =
                    currentAddress.copy(
                        province = province,
                        provinceError = if (province.isBlank()) "Province is required" else null,
                    ),
                isSaveEnabled = validateNextOfKin(),
            )
    }

    private fun updateNextOfKinPostalCode(postalCode: String) {
        val currentAddress = _nextOfKinEditState.value.address
        _nextOfKinEditState.value =
            _nextOfKinEditState.value.copy(
                address =
                    currentAddress.copy(
                        postalCode = postalCode,
                        postalCodeError = if (postalCode.isBlank()) "Postal code is required" else null,
                    ),
                isSaveEnabled = validateNextOfKin(),
            )
    }

    private fun validateNextOfKin(): Boolean {
        val state = _nextOfKinEditState.value
        val address = state.address
        return state.fullName.isNotBlank() &&
            state.relationship.isNotBlank() &&
            state.phoneNumber.isNotBlank() &&
            address.streetAddress.isNotBlank() &&
            address.suburb.isNotBlank() &&
            address.city.isNotBlank() &&
            address.province.isNotBlank() &&
            address.postalCode.isNotBlank()
    }

    @OptIn(ExperimentalTime::class)
    private fun saveNextOfKin() {
        viewModelScope.launch {
            val state = _nextOfKinEditState.value

            if (!validateNextOfKin()) {
                _nextOfKinEditState.value =
                    state.copy(
                        errorMessage = "Please fill in all required fields",
                    )
                return@launch
            }

            _nextOfKinEditState.value = state.copy(isLoading = true)

            val address =
                Address(
                    streetAddress = state.address.streetAddress,
                    suburb = state.address.suburb,
                    city = state.address.city,
                    province = state.address.province,
                    postalCode = state.address.postalCode,
                    residenceType = state.address.residenceType,
                    lastUpdated =
                        Clock.System
                            .now()
                            .toEpochMilliseconds(),
                )

            val nextOfKin =
                NextOfKin(
                    id =
                        _profileScreenState.value.user
                            ?.nextOfKin
                            ?.id ?: "",
                    userId = _profileScreenState.value.user?.id ?: "",
                    fullName = state.fullName,
                    relationship = state.relationship,
                    phoneNumber = state.phoneNumber,
                    address = address,
                    documents = null,
                    createdAt =
                        _profileScreenState.value.user
                            ?.nextOfKin
                            ?.createdAt
                            ?: Clock.System
                                .now()
                                .toEpochMilliseconds(),
                    updatedAt =
                        Clock.System
                            .now()
                            .toEpochMilliseconds(),
                )

            when (val result = manageNextOfKinUseCase.updateNextOfKin(nextOfKin)) {
                is Result.Success -> {
                    _nextOfKinEditState.value =
                        state.copy(
                            isLoading = false,
                            errorMessage = null,
                        )
                    refreshProfile()
                    toggleEditMode()
                }
                is Result.Error -> {
                    _nextOfKinEditState.value =
                        state.copy(
                            isLoading = false,
                            errorMessage = result.exception.message ?: "Failed to update next of kin",
                        )
                }
                is Result.Loading -> {
                    _nextOfKinEditState.value = state.copy(isLoading = true)
                }
            }
        }
    }

    // ========== PROFILE PICTURE OPERATIONS ==========

    private fun uploadProfilePicture(
        imageBytes: ByteArray,
        fileName: String,
    ) {
        viewModelScope.launch {
            _profilePictureState.value =
                _profilePictureState.value.copy(
                    isUploading = true,
                    uploadProgress = 0f,
                )

            when (val result = uploadProfilePictureUseCase(imageBytes, fileName)) {
                is Result.Success -> {
                    _profilePictureState.value =
                        _profilePictureState.value.copy(
                            isUploading = false,
                            uploadProgress = 1f,
                            imageUrl = result.data,
                            errorMessage = null,
                        )
                    refreshProfile()
                }
                is Result.Error -> {
                    _profilePictureState.value =
                        _profilePictureState.value.copy(
                            isUploading = false,
                            uploadProgress = 0f,
                            errorMessage = result.exception.message ?: "Failed to upload profile picture",
                        )
                }

                Result.Loading -> {
                    _profilePictureState.value = _profilePictureState.value.copy(isUploading = true)
                }
            }
        }
    }

    // ========== DOCUMENT OPERATIONS ==========

    private fun uploadNationalId(
        documentBytes: ByteArray,
        fileName: String,
    ) {
        viewModelScope.launch {
            _documentUploadState.value =
                _documentUploadState.value.copy(
                    isUploadingNationalId = true,
                    nationalIdProgress = 0f,
                )

            val documents = mapOf(DocumentType.NATIONAL_ID to Pair(documentBytes, fileName))

            when (val result = uploadDocumentUseCase.uploadDocuments(documents)) {
                is Result.Success<*> -> {
                    _documentUploadState.value =
                        _documentUploadState.value.copy(
                            isUploadingNationalId = false,
                            nationalIdProgress = 1f,
                            errorMessage = null,
                        )
                    refreshProfile()
                }
                is Result.Error -> {
                    _documentUploadState.value =
                        _documentUploadState.value.copy(
                            isUploadingNationalId = false,
                            nationalIdProgress = 0f,
                            errorMessage = result.exception.message ?: "Failed to upload National ID",
                        )
                }

                Result.Loading -> { }
            }
        }
    }

    private fun uploadProofOfResidence(
        documentBytes: ByteArray,
        fileName: String,
    ) {
        viewModelScope.launch {
            _documentUploadState.value =
                _documentUploadState.value.copy(
                    isUploadingProofOfResidence = true,
                    proofOfResidenceProgress = 0f,
                )

            val documents = mapOf(DocumentType.PROOF_OF_RESIDENCE to Pair(documentBytes, fileName))

            when (val result = uploadDocumentUseCase.uploadDocuments(documents)) {
                is Result.Success -> {
                    _documentUploadState.value =
                        _documentUploadState.value.copy(
                            isUploadingProofOfResidence = false,
                            proofOfResidenceProgress = 1f,
                            errorMessage = null,
                        )
                    refreshProfile()
                }
                is Result.Error -> {
                    _documentUploadState.value =
                        _documentUploadState.value.copy(
                            isUploadingProofOfResidence = false,
                            proofOfResidenceProgress = 0f,
                            errorMessage = result.exception.message ?: "Failed to upload Proof of Residence",
                        )
                }

                Result.Loading -> {}
            }
        }
    }

    // ========== CLIENT TYPE OPERATIONS ==========

    private fun showClientTypeDialog() {
        viewModelScope.launch {
            when (val result = manageClientTypeUseCase.getAvailableClientTypes()) {
                is Result.Success -> {
                    _profileScreenState.value =
                        _profileScreenState.value.copy(
                            showClientTypeDialog = true,
                            availableClientTypes = result.data,
                        )
                }
                is Result.Error -> {
                    _profileScreenState.value =
                        _profileScreenState.value.copy(
                            errorMessage = result.exception.message ?: "Failed to load client types",
                        )
                }

                Result.Loading -> {
                    _profileScreenState.value = _profileScreenState.value.copy(isLoading = true)
                }
            }
        }
    }

    private fun dismissClientTypeDialog() {
        _profileScreenState.value =
            _profileScreenState.value.copy(
                showClientTypeDialog = false,
            )
    }

    private fun requestClientTypeChange(newType: String) {
        viewModelScope.launch {
            _profileScreenState.value = _profileScreenState.value.copy(isLoading = true)

            when (val result = manageClientTypeUseCase.requestClientTypeChange(newType)) {
                is Result.Success -> {
                    _profileScreenState.value =
                        _profileScreenState.value.copy(
                            isLoading = false,
                            showClientTypeDialog = false,
                            errorMessage = null,
                        )
                    refreshProfile()
                }
                is Result.Error -> {
                    _profileScreenState.value =
                        _profileScreenState.value.copy(
                            isLoading = false,
                            errorMessage = result.exception.message ?: "Failed to request client type change",
                        )
                }

                Result.Loading -> {
                    _profileScreenState.value = _profileScreenState.value.copy(isLoading = true)
                }
            }
        }
    }

    // ========== LOGOUT OPERATIONS ==========

    private fun showLogoutDialog() {
        _profileScreenState.value =
            _profileScreenState.value.copy(
                showLogoutDialog = true,
            )
    }

    private fun dismissLogoutDialog() {
        _profileScreenState.value =
            _profileScreenState.value.copy(
                showLogoutDialog = false,
            )
    }

    private fun confirmLogout() {
        viewModelScope.launch {
            _profileScreenState.value = _profileScreenState.value.copy(isLoading = true)

            when (val result = logoutUseCase()) {
                is Result.Success -> {
                    _navigationEvents.emit(ProfileNavigation.ToLogin)
                }
                is Result.Error -> {
                    _profileScreenState.value =
                        _profileScreenState.value.copy(
                            isLoading = false,
                            showLogoutDialog = false,
                            errorMessage = result.exception.message ?: "Failed to logout",
                        )
                }

                Result.Loading -> {
                    _profileScreenState.value = _profileScreenState.value.copy(isLoading = true)
                }
            }
        }
    }

    // ========== COMMON OPERATIONS ==========

    private fun clearError() {
        _profileScreenState.value = _profileScreenState.value.copy(errorMessage = null)
        _personalDetailsEditState.value = _personalDetailsEditState.value.copy(errorMessage = null)
        _addressEditState.value = _addressEditState.value.copy(errorMessage = null)
        _nextOfKinEditState.value = _nextOfKinEditState.value.copy(errorMessage = null)
        _profilePictureState.value = _profilePictureState.value.copy(errorMessage = null)
        _documentUploadState.value = _documentUploadState.value.copy(errorMessage = null)
    }

    private fun navigateBack() {
        viewModelScope.launch {
            _navigationEvents.emit(ProfileNavigation.Back)
        }
    }
}
