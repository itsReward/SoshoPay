package com.soshopay.domain.usecase.payment

import com.soshopay.domain.repository.PaymentRepository
import com.soshopay.domain.repository.Result

class CheckPaymentEligibilityUseCase(
    private val paymentRepository: PaymentRepository,
) {
    suspend operator fun invoke(
        loanId: String,
        amount: Double,
    ): Result<Boolean> {
        if (loanId.isBlank()) {
            return Result.Error(Exception("Loan ID is required"))
        }

        if (amount <= 0) {
            return Result.Error(Exception("Payment amount must be greater than zero"))
        }

        return paymentRepository.checkPaymentEligibility(loanId, amount)
    }
}
