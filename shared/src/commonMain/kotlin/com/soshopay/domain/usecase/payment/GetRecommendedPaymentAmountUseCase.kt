package com.soshopay.domain.usecase.payment

import com.soshopay.domain.repository.PaymentRepository
import com.soshopay.domain.repository.Result

class GetRecommendedPaymentAmountUseCase(
    private val paymentRepository: PaymentRepository,
) {
    suspend operator fun invoke(loanId: String): Result<Double> {
        if (loanId.isBlank()) {
            return Result.Error(Exception("Loan ID is required"))
        }
        return paymentRepository.getRecommendedPaymentAmount(loanId)
    }
}
