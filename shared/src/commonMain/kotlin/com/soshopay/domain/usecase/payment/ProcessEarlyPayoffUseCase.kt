package com.soshopay.domain.usecase.payment

import com.soshopay.domain.model.PaymentRequest
import com.soshopay.domain.repository.PaymentRepository
import com.soshopay.domain.repository.Result

class ProcessEarlyPayoffUseCase(
    private val paymentRepository: PaymentRepository,
) {
    suspend operator fun invoke(
        loanId: String,
        paymentMethod: String,
        phoneNumber: String,
        customerReference: String? = null,
    ): Result<String> {
        // First calculate early payoff amount
        val calculationResult = paymentRepository.calculateEarlyPayoff(loanId)

        val calculation =
            when (calculationResult) {
                is Result.Success -> calculationResult.data
                is Result.Error -> return calculationResult
                is Result.Loading -> return Result.Error(Exception("Unable to calculate early payoff amount"))
            }

        // Create payment request with early payoff amount
        val request =
            PaymentRequest(
                loanId = loanId,
                amount = calculation.earlyPayoffAmount,
                paymentMethod = paymentMethod,
                phoneNumber = phoneNumber,
                customerReference = customerReference,
            )

        // Validate request
        val validation = paymentRepository.validatePaymentRequest(request)
        if (!validation.isValid) {
            return Result.Error(Exception(validation.getErrorMessage()))
        }

        // Process early payoff
        return paymentRepository.processEarlyPayoff(loanId, request)
    }
}
