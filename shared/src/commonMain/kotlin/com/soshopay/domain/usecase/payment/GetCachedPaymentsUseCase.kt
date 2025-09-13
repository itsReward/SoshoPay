package com.soshopay.domain.usecase.payment

import com.soshopay.domain.model.Payment
import com.soshopay.domain.repository.PaymentRepository
import com.soshopay.domain.repository.Result

class GetCachedPaymentsUseCase(
    private val paymentRepository: PaymentRepository,
) {
    suspend operator fun invoke(): Result<List<Payment>> = paymentRepository.getCachedPayments()
}
