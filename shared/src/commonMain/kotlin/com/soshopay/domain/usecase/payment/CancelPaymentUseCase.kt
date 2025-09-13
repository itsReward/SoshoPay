package com.soshopay.domain.usecase.payment

import com.soshopay.domain.repository.PaymentRepository
import com.soshopay.domain.repository.Result

class CancelPaymentUseCase(
    private val paymentRepository: PaymentRepository,
) {
    suspend operator fun invoke(paymentId: String): Result<Unit> {
        if (paymentId.isBlank()) {
            return Result.Error(Exception("Payment ID is required"))
        }
        return paymentRepository.cancelPayment(paymentId)
    }
}
