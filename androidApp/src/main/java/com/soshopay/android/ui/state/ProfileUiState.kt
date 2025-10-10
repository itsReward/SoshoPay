package com.soshopay.android.ui.state

import com.soshopay.domain.model.Address
import com.soshopay.domain.model.ClientType
import com.soshopay.domain.model.Document
import com.soshopay.domain.model.NextOfKin
import com.soshopay.domain.model.PersonalDetails
import com.soshopay.domain.model.User

/**
 * Data class representing the profile screen state
 */
data class ProfileScreenState(
    val user: User? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isEditMode: Boolean = false,
    val showLogoutDialog: Boolean = false,
    val showClientTypeDialog: Boolean = false,
    val availableClientTypes: List<String> = emptyList(),
) {
    fun hasErrors(): Boolean = errorMessage != null
}

/**
 * Data class representing personal details edit state
 */
data class PersonalDetailsEditState(
    val firstName: String = "",
    val lastName: String = "",
    val dateOfBirth: String = "",
    val gender: String = "",
    val nationality: String = "",
    val occupation: String = "",
    val monthlyIncome: String = "",
    val firstNameError: String? = null,
    val lastNameError: String? = null,
    val dateOfBirthError: String? = null,
    val genderError: String? = null,
    val nationalityError: String? = null,
    val occupationError: String? = null,
    val monthlyIncomeError: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSaveEnabled: Boolean = false,
) {
    fun hasErrors(): Boolean =
        firstNameError != null ||
            lastNameError != null ||
            dateOfBirthError != null ||
            genderError != null ||
            nationalityError != null ||
            occupationError != null ||
            monthlyIncomeError != null ||
            errorMessage != null
}

/**
 * Data class representing address edit state
 */
data class AddressEditState(
    val streetAddress: String = "",
    val suburb: String = "",
    val city: String = "",
    val province: String = "",
    val postalCode: String = "",
    val residenceType: String = "",
    val streetAddressError: String? = null,
    val suburbError: String? = null,
    val cityError: String? = null,
    val provinceError: String? = null,
    val postalCodeError: String? = null,
    val residenceTypeError: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSaveEnabled: Boolean = false,
) {
    fun hasErrors(): Boolean =
        streetAddressError != null ||
            suburbError != null ||
            cityError != null ||
            provinceError != null ||
            postalCodeError != null ||
            residenceTypeError != null ||
            errorMessage != null
}

/**
 * Data class representing next of kin edit state
 */
data class NextOfKinEditState(
    val fullName: String = "",
    val relationship: String = "",
    val phoneNumber: String = "",
    val address: AddressEditState = AddressEditState(),
    val fullNameError: String? = null,
    val relationshipError: String? = null,
    val phoneNumberError: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSaveEnabled: Boolean = false,
) {
    fun hasErrors(): Boolean =
        fullNameError != null ||
            relationshipError != null ||
            phoneNumberError != null ||
            address.hasErrors() ||
            errorMessage != null
}

/**
 * Data class representing profile picture upload state
 */
data class ProfilePictureState(
    val imageUrl: String? = null,
    val isUploading: Boolean = false,
    val uploadProgress: Float = 0f,
    val errorMessage: String? = null,
) {
    fun hasErrors(): Boolean = errorMessage != null
}

/**
 * Data class representing document upload state
 */
data class DocumentUploadState(
    val nationalId: Document? = null,
    val proofOfResidence: Document? = null,
    val isUploadingNationalId: Boolean = false,
    val isUploadingProofOfResidence: Boolean = false,
    val nationalIdProgress: Float = 0f,
    val proofOfResidenceProgress: Float = 0f,
    val errorMessage: String? = null,
) {
    fun hasErrors(): Boolean = errorMessage != null
}

/**
 * Sealed class representing different profile events
 */
sealed class ProfileEvent {
    // Profile View Events
    object LoadProfile : ProfileEvent()

    object RefreshProfile : ProfileEvent()

    object ToggleEditMode : ProfileEvent()

    object CancelEdit : ProfileEvent()

    // Personal Details Events
    data class UpdateFirstName(
        val firstName: String,
    ) : ProfileEvent()

    data class UpdateLastName(
        val lastName: String,
    ) : ProfileEvent()

    data class UpdateDateOfBirth(
        val dateOfBirth: String,
    ) : ProfileEvent()

    data class UpdateGender(
        val gender: String,
    ) : ProfileEvent()

    data class UpdateNationality(
        val nationality: String,
    ) : ProfileEvent()

    data class UpdateOccupation(
        val occupation: String,
    ) : ProfileEvent()

    data class UpdateMonthlyIncome(
        val income: String,
    ) : ProfileEvent()

    object SavePersonalDetails : ProfileEvent()

    // Address Events
    data class UpdateStreetAddress(
        val address: String,
    ) : ProfileEvent()

    data class UpdateSuburb(
        val suburb: String,
    ) : ProfileEvent()

    data class UpdateCity(
        val city: String,
    ) : ProfileEvent()

    data class UpdateProvince(
        val province: String,
    ) : ProfileEvent()

    data class UpdatePostalCode(
        val postalCode: String,
    ) : ProfileEvent()

    data class UpdateResidenceType(
        val type: String,
    ) : ProfileEvent()

    object SaveAddress : ProfileEvent()

    // Next of Kin Events
    data class UpdateNextOfKinFullName(
        val fullName: String,
    ) : ProfileEvent()

    data class UpdateNextOfKinRelationship(
        val relationship: String,
    ) : ProfileEvent()

    data class UpdateNextOfKinPhoneNumber(
        val phoneNumber: String,
    ) : ProfileEvent()

    data class UpdateNextOfKinStreetAddress(
        val address: String,
    ) : ProfileEvent()

    data class UpdateNextOfKinSuburb(
        val suburb: String,
    ) : ProfileEvent()

    data class UpdateNextOfKinCity(
        val city: String,
    ) : ProfileEvent()

    data class UpdateNextOfKinProvince(
        val province: String,
    ) : ProfileEvent()

    data class UpdateNextOfKinPostalCode(
        val postalCode: String,
    ) : ProfileEvent()

    object SaveNextOfKin : ProfileEvent()

    // Profile Picture Events
    data class UploadProfilePicture(
        val imageBytes: ByteArray,
        val fileName: String,
    ) : ProfileEvent()

    // Document Events
    data class UploadNationalId(
        val documentBytes: ByteArray,
        val fileName: String,
    ) : ProfileEvent()

    data class UploadProofOfResidence(
        val documentBytes: ByteArray,
        val fileName: String,
    ) : ProfileEvent()

    // Client Type Events
    object ShowClientTypeDialog : ProfileEvent()

    object DismissClientTypeDialog : ProfileEvent()

    data class RequestClientTypeChange(
        val newType: String,
    ) : ProfileEvent()

    // Logout Events
    object ShowLogoutDialog : ProfileEvent()

    object DismissLogoutDialog : ProfileEvent()

    object ConfirmLogout : ProfileEvent()

    // Common Events
    object ClearError : ProfileEvent()

    object NavigateBack : ProfileEvent()
}

/**
 * Sealed class representing different profile navigation destinations
 */
sealed class ProfileNavigation {
    object ToLogin : ProfileNavigation()

    object Back : ProfileNavigation()
}
