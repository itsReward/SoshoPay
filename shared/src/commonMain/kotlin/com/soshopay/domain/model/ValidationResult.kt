package com.soshopay.domain.model

import kotlinx.serialization.Serializable

// ========== VALIDATION RESULT ==========
@Serializable
data class ValidationResult(
    val isValid: Boolean,
    val errors: List<String> = emptyList(),
    val warnings: List<String> = emptyList(),
) {
    fun hasErrors(): Boolean = errors.isNotEmpty()

    fun hasWarnings(): Boolean = warnings.isNotEmpty()

    fun getErrorMessage(): String? = errors.joinToString(", ").takeIf { it.isNotBlank() }
}
