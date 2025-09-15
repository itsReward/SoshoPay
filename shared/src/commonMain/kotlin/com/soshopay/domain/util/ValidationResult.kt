package com.soshopay.domain.util

/**
 * Represents the result of a validation operation, including validity status, errors, and warnings.
 *
 * This data class is used to encapsulate the outcome of validating user input or domain objects.
 * It provides utility methods to check for errors and warnings, and to retrieve error messages.
 *
 * @property isValid Indicates whether the validation was successful (true) or failed (false).
 * @property errors A list of error messages describing validation failures. Empty if no errors.
 * @property warnings A list of warning messages that do not invalidate the result but may require attention.
 */
data class ValidationResult(
    val isValid: Boolean,
    val errors: List<String> = emptyList(),
    val warnings: List<String> = emptyList(),
) {
    /**
     * Returns true if there are any errors present in the validation result.
     */
    fun hasErrors(): Boolean = errors.isNotEmpty()

    /**
     * Returns true if there are any warnings present in the validation result.
     */
    fun hasWarnings(): Boolean = warnings.isNotEmpty()

    /**
     * Returns the first error message, or null if there are no errors.
     */
    fun getErrorMessage(): String? = errors.firstOrNull()

    /**
     * Returns all error messages concatenated into a single string, separated by commas.
     */
    fun getAllErrorMessages(): String = errors.joinToString(", ")
}
