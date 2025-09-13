package com.soshopay.domain.usecase.payment

import com.soshopay.domain.repository.PaymentRepository
import com.soshopay.domain.repository.Result

class RetryFailedPaymentUseCase(
    private val paymentRepository: PaymentRepository,
) {
    suspend operator fun invoke(paymentId: String): Result<String> {
        if (paymentId.isBlank()) {
            return Result.Error(Exception("Payment ID is required"))
        }
        return paymentRepository.retryFailedPayment(paymentId)
    }
}
