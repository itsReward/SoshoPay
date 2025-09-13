package com.soshopay.domain.usecase.payment

import com.soshopay.domain.model.PaymentStatus
import com.soshopay.domain.repository.PaymentRepository
import com.soshopay.domain.repository.Result

class GetPaymentStatusUseCase(
    private val paymentRepository: PaymentRepository,
) {
    suspend operator fun invoke(paymentId: String): Result<PaymentStatus> {
        if (paymentId.isBlank()) {
            return Result.Error(Exception("Payment ID is required"))
        }
        return paymentRepository.getPaymentStatus(paymentId)
    }
}
