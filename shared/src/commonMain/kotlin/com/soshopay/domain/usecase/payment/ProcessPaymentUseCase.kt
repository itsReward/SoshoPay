package com.soshopay.domain.usecase.payment

import com.soshopay.domain.model.PaymentRequest
import com.soshopay.domain.repository.PaymentRepository
import com.soshopay.domain.repository.Result

class ProcessPaymentUseCase(
    private val paymentRepository: PaymentRepository,
) {
    suspend operator fun invoke(
        loanId: String,
        amount: Double,
        paymentMethod: String,
        phoneNumber: String,
        customerReference: String? = null,
    ): Result<String> {
        // Create payment request
        val request =
            PaymentRequest(
                loanId = loanId,
                amount = amount,
                paymentMethod = paymentMethod,
                phoneNumber = phoneNumber,
                customerReference = customerReference,
            )

        // Validate request
        val validation = paymentRepository.validatePaymentRequest(request)
        if (!validation.isValid) {
            return Result.Error(Exception(validation.getErrorMessage()))
        }

        // Check eligibility
        val eligibilityResult = paymentRepository.checkPaymentEligibility(loanId, amount)
        when (eligibilityResult) {
            is Result.Success -> {
                if (!eligibilityResult.data) {
                    return Result.Error(Exception("Payment not eligible for this loan"))
                }
            }
            is Result.Error -> {
                // Continue with processing if eligibility check fails (API might be down)
            }
            else -> {}
        }

        // Process payment
        return paymentRepository.processPayment(request)
    }
}
