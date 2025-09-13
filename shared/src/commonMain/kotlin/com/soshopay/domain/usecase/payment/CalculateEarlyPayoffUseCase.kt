package com.soshopay.domain.usecase.payment

import com.soshopay.domain.model.EarlyPayoffCalculation
import com.soshopay.domain.repository.PaymentRepository
import com.soshopay.domain.repository.Result

class CalculateEarlyPayoffUseCase(
    private val paymentRepository: PaymentRepository,
) {
    suspend operator fun invoke(loanId: String): Result<EarlyPayoffCalculation> {
        if (loanId.isBlank()) {
            return Result.Error(Exception("Loan ID is required"))
        }
        return paymentRepository.calculateEarlyPayoff(loanId)
    }
}
