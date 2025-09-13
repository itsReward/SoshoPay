package com.soshopay.domain.usecase.payment

import com.soshopay.domain.model.PaymentHistoryResponse
import com.soshopay.domain.repository.PaymentRepository
import com.soshopay.domain.repository.Result

class GetPaymentHistoryUseCase(
    private val paymentRepository: PaymentRepository,
) {
    suspend operator fun invoke(
        page: Int = 1,
        limit: Int = 20,
    ): Result<PaymentHistoryResponse> {
        if (page < 1) {
            return Result.Error(Exception("Page must be greater than 0"))
        }

        if (limit < 1 || limit > 100) {
            return Result.Error(Exception("Limit must be between 1 and 100"))
        }

        return paymentRepository.getPaymentHistory(page, limit)
    }
}
