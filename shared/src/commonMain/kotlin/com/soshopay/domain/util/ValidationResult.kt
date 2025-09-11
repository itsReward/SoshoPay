package com.soshopay.domain.util

data class ValidationResult(
    val isValid: Boolean,
    val errors: List<String> = emptyList(),
    val warnings: List<String> = emptyList(),
) {
    fun hasErrors(): Boolean = errors.isNotEmpty()

    fun hasWarnings(): Boolean = warnings.isNotEmpty()

    fun getErrorMessage(): String? = errors.firstOrNull()

    fun getAllErrorMessages(): String = errors.joinToString(", ")
}
