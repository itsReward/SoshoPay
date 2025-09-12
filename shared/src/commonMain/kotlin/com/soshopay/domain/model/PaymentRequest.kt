package com.soshopay.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class PaymentRequest(
    val loanId: String,
    val amount: Double,
    val paymentMethod: String,
    val phoneNumber: String,
    val customerReference: String? = null,
) {
    fun validate(): ValidationResult {
        val errors = mutableListOf<String>()

        if (loanId.isBlank()) {
            errors.add("Loan ID is required")
        }

        if (amount <= 0) {
            errors.add("Payment amount must be greater than zero")
        }

        if (paymentMethod.isBlank()) {
            errors.add("Payment method is required")
        }

        if (phoneNumber.isBlank()) {
            errors.add("Phone number is required")
        } else if (!isValidZimbabweanPhoneNumber(phoneNumber)) {
            errors.add("Please enter a valid Zimbabwean phone number")
        }

        return ValidationResult(
            isValid = errors.isEmpty(),
            errors = errors,
        )
    }

    private fun isValidZimbabweanPhoneNumber(phone: String): Boolean {
        val normalizedPhone = phone.replace(Regex("[^0-9+]"), "")
        return when {
            normalizedPhone.startsWith("077") -> normalizedPhone.length == 10
            normalizedPhone.startsWith("263") -> normalizedPhone.length == 12
            normalizedPhone.startsWith("+263") -> normalizedPhone.length == 13
            else -> false
        }
    }
}
