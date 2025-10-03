package com.soshopay.domain.util

/**
 * Utility class providing validation functions for Zimbabwe-specific data such as phone numbers,
 * national IDs, addresses, personal details, PINs, and file uploads.
 *
 * Contains nested objects for each validation domain, with helper methods for checking validity,
 * formatting, and error reporting.
 */
object ValidationUtils {
    /**
     * Provides validation and formatting utilities for Zimbabwean phone numbers.
     */
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

        /**
         * Checks if the given phone number is a valid Zimbabwean mobile number.
         * Accepts international, local, and direct formats.
         * @param phone The phone number to validate.
         * @return True if valid, false otherwise.
         */
        fun isValidZimbabwePhone(phone: String): Boolean {
            val cleanPhone = phone.replace(Regex("[^0-9]"), "")
            Logger.e("Cleaned phone number $cleanPhone", "AUTH")

            return when {
                // International format: +263771234567 or 263771234567
                cleanPhone.startsWith("263") -> {
                    val localNumber = cleanPhone.substring(3)
                    localNumber.length == 9 && validPrefixes.any { localNumber.startsWith(it) }
                }
                // Local format: 0771234567
                cleanPhone.startsWith("0") -> {
                    cleanPhone.length == 10 && validPrefixes.any { cleanPhone.startsWith("0$it".drop(1)) }
                }
                // Direct format: 771234567
                cleanPhone.length == 9 -> {
                    validPrefixes.any { cleanPhone.startsWith(it) }
                }
                else -> false
            }
        }

        /**
         * Normalizes a Zimbabwean phone number to international format (263XXXXXXXXX).
         * @param phone The phone number to normalize.
         * @return Normalized phone number as a string.
         */
        fun normalizeZimbabwePhone(phone: String): String {
            val cleanPhone = phone.replace(Regex("[^0-9]"), "")

            return when {
                cleanPhone.startsWith("263") -> cleanPhone
                cleanPhone.startsWith("0") -> "263${cleanPhone.substring(1)}"
                cleanPhone.length == 9 -> "263$cleanPhone"
                else -> cleanPhone
            }
        }

        /**
         * Formats a Zimbabwean phone number for display (e.g., +263 77 123 4567).
         * @param phone The phone number to format.
         * @return Formatted phone number string.
         */
        fun formatForDisplay(phone: String): String {
            val normalized = normalizeZimbabwePhone(phone)
            return if (normalized.startsWith("263") && normalized.length == 12) {
                "+${normalized.substring(0, 3)} ${normalized.substring(3, 5)} ${normalized.substring(5, 8)} ${normalized.substring(8)}"
            } else {
                phone
            }
        }

        /**
         * Returns a validation error message for a phone number, or null if valid.
         * @param phone The phone number to validate.
         * @return Error message string or null.
         */
        fun getValidationError(phone: String): String? {
            Logger.e("Validating phone number $phone", "AUTH")
            if (phone.isBlank()) return "Phone number is required"
            if (!isValidZimbabwePhone(phone)) {
                return "Please enter a valid Zimbabwe phone number (e.g., +263 77 123 4567)"
            }
            return null
        }
    }

    /**
     * Provides validation and formatting utilities for Zimbabwean National IDs.
     */
    object NationalId {
        // Zimbabwe National ID format: 63-123456-A-12
        private val nationalIdRegex = Regex("^\\d{2}-\\d{6,7}-[A-Z]\\d{2}$")
        private val nationalIdRegexWithoutDashes = Regex("^\\d{2}\\d{6,7}[A-Z]\\d{2}$")

        /**
         * Checks if the given national ID is valid according to Zimbabwean formats.
         * @param nationalId The national ID to validate.
         * @return True if valid, false otherwise.
         */
        fun isValidZimbabweNationalId(nationalId: String): Boolean {
            val cleanId = nationalId.trim().uppercase()
            return nationalIdRegex.matches(cleanId) || nationalIdRegexWithoutDashes.matches(cleanId)
        }

        /**
         * Formats a Zimbabwean national ID to standard format (e.g., 63-123456-A12).
         * @param nationalId The national ID to format.
         * @return Formatted national ID string.
         */
        fun formatNationalId(nationalId: String): String {
            val cleanId = nationalId.replace(Regex("[^0-9A-Za-z]"), "").uppercase()

            return if (cleanId.length >= 11) {
                "${cleanId.substring(0, 2)}-${cleanId.substring(2, 8)}-${cleanId.substring(8, 9)}${cleanId.substring(9, 11)}"
            } else {
                nationalId
            }
        }

        /**
         * Returns a validation error message for a national ID, or null if valid.
         * @param nationalId The national ID to validate.
         * @return Error message string or null.
         */
        fun getValidationError(nationalId: String): String? {
            if (nationalId.isBlank()) return "National ID is required"
            if (!isValidZimbabweNationalId(nationalId)) {
                return "Please enter a valid Zimbabwe National ID (e.g., 63-123456-A12)"
            }
            return null
        }
    }

    /**
     * Provides validation utilities for Zimbabwean addresses, including province and residence type checks.
     */
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

        /**
         * Checks if the given province is a valid Zimbabwean province.
         * @param province The province name to validate.
         * @return True if valid, false otherwise.
         */
        fun isValidProvince(province: String): Boolean = zimbabweProvinces.contains(province.trim())

        /**
         * Checks if the given residence type is valid.
         * @param type The residence type to validate.
         * @return True if valid, false otherwise.
         */
        fun isValidResidenceType(type: String): Boolean = residenceTypes.contains(type.trim())

        /**
         * Returns the list of Zimbabwean provinces.
         */
        fun getProvinces(): List<String> = zimbabweProvinces

        /**
         * Returns the list of valid residence types.
         */
        fun getResidenceTypes(): List<String> = residenceTypes

        /**
         * Validates a Zimbabwean address and returns a ValidationResult with errors if any.
         * @param streetAddress Street address string.
         * @param suburb Suburb string.
         * @param city City string.
         * @param province Province string.
         * @param postalCode Postal code string.
         * @param residenceType Residence type string.
         * @return ValidationResult containing validity and error messages.
         */
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

    /**
     * Provides validation utilities for personal details such as names, age, gender, occupation, and income.
     */
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

        /**
         * Checks if the given name is valid (at least 2 characters, letters only).
         * @param name The name to validate.
         * @return True if valid, false otherwise.
         */
        fun isValidName(name: String): Boolean = name.trim().length >= 2 && name.matches(Regex("^[a-zA-Z\\s'-]+$"))

        /**
         * Checks if the given date of birth corresponds to a valid age (18-100 years).
         * @param dateOfBirth Date of birth in epoch milliseconds.
         * @return True if age is valid, false otherwise.
         */
        fun isValidAge(dateOfBirth: Long): Boolean {
            val currentTime =
                kotlinx.datetime.Clock.System
                    .now()
                    .toEpochMilliseconds()
            val age = (currentTime - dateOfBirth) / (365.25 * 24 * 60 * 60 * 1000)
            return age >= 18 && age <= 100
        }

        /**
         * Checks if the monthly income is within a reasonable range.
         * @param income Monthly income value.
         * @return True if valid, false otherwise.
         */
        fun isValidMonthlyIncome(income: Double): Boolean {
            return income >= 0 && income <= 1_000_000 // Reasonable upper limit
        }

        /**
         * Returns the list of valid genders.
         */
        fun getGenders(): List<String> = genders

        /**
         * Returns the list of valid occupations.
         */
        fun getOccupations(): List<String> = occupations

        /**
         * Validates personal details and returns a ValidationResult with errors if any.
         * @param firstName First name string.
         * @param lastName Last name string.
         * @param dateOfBirth Date of birth in epoch milliseconds.
         * @param gender Gender string.
         * @param nationality Nationality string.
         * @param occupation Occupation string.
         * @param monthlyIncome Monthly income value.
         * @return ValidationResult containing validity and error messages.
         */
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

    /**
     * Provides validation utilities for PIN codes.
     */
    object Pin {
        /**
         * Checks if the given PIN is valid (exactly 4 digits).
         * @param pin The PIN string to validate.
         * @return True if valid, false otherwise.
         */
        fun isValidPin(pin: String): Boolean = pin.length == 4 && pin.all { it.isDigit() }

        /**
         * Returns a validation error message for a PIN, or null if valid.
         * @param pin The PIN string to validate.
         * @return Error message string or null.
         */
        fun getValidationError(pin: String): String? =
            when {
                pin.isBlank() -> "PIN is required"
                pin.length != 4 -> "PIN must be exactly 4 digits"
                !pin.all { it.isDigit() } -> "PIN must contain only numbers"
                else -> null
            }
    }

    /**
     * Provides validation utilities for file uploads, including type and size checks.
     */
    object File {
        private val allowedImageTypes = listOf("jpg", "jpeg", "png")
        private val allowedDocumentTypes = listOf("pdf") + allowedImageTypes
        private const val MAX_FILE_SIZE = 5 * 1024 * 1024L // 5MB

        /**
         * Checks if the file size is within the allowed limit (5MB).
         * @param sizeBytes File size in bytes.
         * @return True if valid, false otherwise.
         */
        fun isValidFileSize(sizeBytes: Long): Boolean = sizeBytes <= MAX_FILE_SIZE

        /**
         * Checks if the file is a valid image type (jpg, jpeg, png).
         * @param fileName File name string.
         * @return True if valid, false otherwise.
         */
        fun isValidImageType(fileName: String): Boolean {
            val extension = fileName.substringAfterLast('.', "").lowercase()
            return allowedImageTypes.contains(extension)
        }

        /**
         * Checks if the file is a valid document type (pdf, jpg, jpeg, png).
         * @param fileName File name string.
         * @return True if valid, false otherwise.
         */
        fun isValidDocumentType(fileName: String): Boolean {
            val extension = fileName.substringAfterLast('.', "").lowercase()
            return allowedDocumentTypes.contains(extension)
        }

        /**
         * Returns a file size error message, or null if valid.
         * @param sizeBytes File size in bytes.
         * @return Error message string or null.
         */
        fun getFileSizeError(sizeBytes: Long): String? =
            if (!isValidFileSize(sizeBytes)) {
                "File size must not exceed ${MAX_FILE_SIZE / (1024 * 1024)}MB"
            } else {
                null
            }

        /**
         * Returns a file type error message, or null if valid.
         * @param fileName File name string.
         * @param isDocument True if validating as document, false for image.
         * @return Error message string or null.
         */
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
