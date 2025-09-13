package com.soshopay.domain.usecase.payment

import com.soshopay.domain.model.PaymentRequest
import com.soshopay.domain.model.ValidationResult
import com.soshopay.domain.repository.PaymentRepository

class ValidatePaymentRequestUseCase(
    private val paymentRepository: PaymentRepository,
) {
    operator fun invoke(
        loanId: String,
        amount: Double,
        paymentMethod: String,
        phoneNumber: String,
        customerReference: String? = null,
    ): ValidationResult {
        val request =
            PaymentRequest(
                loanId = loanId,
                amount = amount,
                paymentMethod = paymentMethod,
                phoneNumber = phoneNumber,
                customerReference = customerReference,
            )
        return paymentRepository.validatePaymentRequest(request)
    }
}
