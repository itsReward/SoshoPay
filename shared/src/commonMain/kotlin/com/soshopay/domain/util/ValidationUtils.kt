package com.soshopay.domain.util

object ValidationUtils {
    // Zimbabwe Phone Number Validation
    object Phone {
        private val validPrefixes =
            listOf(
                "077",
                "078", // Econet
                "071",
                "073",
                "074", // NetOne
                "086",
                "087", // Telecel
            )

        fun isValidZimbabwePhone(phone: String): Boolean {
            val cleanPhone = phone.replace(Regex("[^0-9]"), "")

            return when {
                // International format: +263771234567 or 263771234567
                cleanPhone.startsWith("263") -> {
                    val localNumber = cleanPhone.substring(3)
                    localNumber.length == 9 && validPrefixes.any { localNumber.startsWith(it) }
                }
                // Local format: 0771234567
                cleanPhone.startsWith("0") -> {
                    cleanPhone.length == 10 && validPrefixes.any { cleanPhone.substring(1).startsWith(it) }
                }
                // Direct format: 771234567
                cleanPhone.length == 9 -> {
                    validPrefixes.any { cleanPhone.startsWith(it) }
                }
                else -> false
            }
        }

        fun normalizeZimbabwePhone(phone: String): String {
            val cleanPhone = phone.replace(Regex("[^0-9]"), "")

            return when {
                cleanPhone.startsWith("263") -> cleanPhone
                cleanPhone.startsWith("0") -> "263${cleanPhone.substring(1)}"
                cleanPhone.length == 9 -> "263$cleanPhone"
                else -> cleanPhone
            }
        }

        fun formatForDisplay(phone: String): String {
            val normalized = normalizeZimbabwePhone(phone)
            return if (normalized.startsWith("263") && normalized.length == 12) {
                "+${normalized.substring(0, 3)} ${normalized.substring(3, 5)} ${normalized.substring(5, 8)} ${normalized.substring(8)}"
            } else {
                phone
            }
        }

        fun getValidationError(phone: String): String? {
            if (phone.isBlank()) return "Phone number is required"
            if (!isValidZimbabwePhone(phone)) {
                return "Please enter a valid Zimbabwe phone number (e.g., +263 77 123 4567)"
            }
            return null
        }
    }

    // Zimbabwe National ID Validation
    object NationalId {
        // Zimbabwe National ID format: 63-123456-A-12
        private val nationalIdRegex = Regex("^\\d{2}-\\d{6,7}-[A-Z]\\d{2}$")
        private val nationalIdRegexWithoutDashes = Regex("^\\d{2}\\d{6,7}[A-Z]\\d{2}$")

        fun isValidZimbabweNationalId(nationalId: String): Boolean {
            val cleanId = nationalId.trim().uppercase()
            return nationalIdRegex.matches(cleanId) || nationalIdRegexWithoutDashes.matches(cleanId)
        }

        fun formatNationalId(nationalId: String): String {
            val cleanId = nationalId.replace(Regex("[^0-9A-Za-z]"), "").uppercase()

            return if (cleanId.length >= 11) {
                "${cleanId.substring(0, 2)}-${cleanId.substring(2, 8)}-${cleanId.substring(8, 9)}${cleanId.substring(9, 11)}"
            } else {
                nationalId
            }
        }

        fun getValidationError(nationalId: String): String? {
            if (nationalId.isBlank()) return "National ID is required"
            if (!isValidZimbabweNationalId(nationalId)) {
                return "Please enter a valid Zimbabwe National ID (e.g., 63-123456-A12)"
            }
            return null
        }
    }

    // Zimbabwe Address Validation
    object Address {
        private val zimbabweProvinces =
            listOf(
                "Harare",
                "Bulawayo",
                "Manicaland",
                "Mashonaland Central",
                "Mashonaland East",
                "Mashonaland West",
                "Masvingo",
                "Matabeleland North",
                "Matabeleland South",
                "Midlands",
            )

        private val residenceTypes =
            listOf(
                "Owned",
                "Rented",
                "Family Property",
                "Company Property",
                "Other",
            )

        fun isValidProvince(province: String): Boolean = zimbabweProvinces.contains(province.trim())

        fun isValidResidenceType(type: String): Boolean = residenceTypes.contains(type.trim())

        fun getProvinces(): List<String> = zimbabweProvinces

        fun getResidenceTypes(): List<String> = residenceTypes

        fun validateAddress(
            streetAddress: String,
            suburb: String,
            city: String,
            province: String,
            postalCode: String,
            residenceType: String,
        ): ValidationResult {
            val errors = mutableListOf<String>()

            if (streetAddress.isBlank()) errors.add("Street address is required")
            if (suburb.isBlank()) errors.add("Suburb is required")
            if (city.isBlank()) errors.add("City is required")
            if (province.isBlank()) {
                errors.add("Province is required")
            } else if (!isValidProvince(province)) {
                errors.add("Please select a valid province")
            }
            if (residenceType.isBlank()) {
                errors.add("Residence type is required")
            } else if (!isValidResidenceType(residenceType)) {
                errors.add("Please select a valid residence type")
            }

            // Optional postal code validation
            if (postalCode.isNotBlank() && !postalCode.matches(Regex("^[0-9]{4,6}$"))) {
                errors.add("Postal code should be 4-6 digits")
            }

            return ValidationResult(
                isValid = errors.isEmpty(),
                errors = errors,
            )
        }
    }

    // Personal Details Validation
    object PersonalDetails {
        private val genders = listOf("Male", "Female", "Other")
        private val occupations =
            listOf(
                "Government Employee",
                "Private Employee",
                "Self Employed",
                "Student",
                "Unemployed",
                "Retired",
                "Other",
            )

        fun isValidName(name: String): Boolean = name.trim().length >= 2 && name.matches(Regex("^[a-zA-Z\\s'-]+$"))

        fun isValidAge(dateOfBirth: Long): Boolean {
            val currentTime =
                kotlinx.datetime.Clock.System
                    .now()
                    .toEpochMilliseconds()
            val age = (currentTime - dateOfBirth) / (365.25 * 24 * 60 * 60 * 1000)
            return age >= 18 && age <= 100
        }

        fun isValidMonthlyIncome(income: Double): Boolean {
            return income >= 0 && income <= 1_000_000 // Reasonable upper limit
        }

        fun getGenders(): List<String> = genders

        fun getOccupations(): List<String> = occupations

        fun validatePersonalDetails(
            firstName: String,
            lastName: String,
            dateOfBirth: Long,
            gender: String,
            nationality: String,
            occupation: String,
            monthlyIncome: Double,
        ): ValidationResult {
            val errors = mutableListOf<String>()

            if (!isValidName(firstName)) errors.add("First name must be at least 2 characters and contain only letters")
            if (!isValidName(lastName)) errors.add("Last name must be at least 2 characters and contain only letters")
            if (!isValidAge(dateOfBirth)) errors.add("You must be between 18 and 100 years old")
            if (gender.isBlank()) errors.add("Gender is required")
            if (nationality.isBlank()) errors.add("Nationality is required")
            if (occupation.isBlank()) errors.add("Occupation is required")
            if (!isValidMonthlyIncome(monthlyIncome)) errors.add("Monthly income must be a valid amount")

            return ValidationResult(
                isValid = errors.isEmpty(),
                errors = errors,
            )
        }
    }

    // PIN Validation
    object Pin {
        fun isValidPin(pin: String): Boolean = pin.length == 4 && pin.all { it.isDigit() }

        fun getValidationError(pin: String): String? =
            when {
                pin.isBlank() -> "PIN is required"
                pin.length != 4 -> "PIN must be exactly 4 digits"
                !pin.all { it.isDigit() } -> "PIN must contain only numbers"
                else -> null
            }
    }

    // File Validation
    object File {
        private val allowedImageTypes = listOf("jpg", "jpeg", "png")
        private val allowedDocumentTypes = listOf("pdf") + allowedImageTypes
        private const val MAX_FILE_SIZE = 5 * 1024 * 1024L // 5MB

        fun isValidFileSize(sizeBytes: Long): Boolean = sizeBytes <= MAX_FILE_SIZE

        fun isValidImageType(fileName: String): Boolean {
            val extension = fileName.substringAfterLast('.', "").lowercase()
            return allowedImageTypes.contains(extension)
        }

        fun isValidDocumentType(fileName: String): Boolean {
            val extension = fileName.substringAfterLast('.', "").lowercase()
            return allowedDocumentTypes.contains(extension)
        }

        fun getFileSizeError(sizeBytes: Long): String? =
            if (!isValidFileSize(sizeBytes)) {
                "File size must not exceed ${MAX_FILE_SIZE / (1024 * 1024)}MB"
            } else {
                null
            }

        fun getFileTypeError(
            fileName: String,
            isDocument: Boolean = true,
        ): String? {
            val isValid = if (isDocument) isValidDocumentType(fileName) else isValidImageType(fileName)
            return if (!isValid) {
                val allowedTypes = if (isDocument) allowedDocumentTypes else allowedImageTypes
                "Allowed file types: ${allowedTypes.joinToString(", ")}"
            } else {
                null
            }
        }
    }
}
