package com.soshopay.domain.usecase.payment

import com.soshopay.domain.model.Payment
import com.soshopay.domain.repository.PaymentRepository
import kotlinx.coroutines.flow.Flow

class ObservePaymentUpdatesUseCase(
    private val paymentRepository: PaymentRepository,
) {
    operator fun invoke(): Flow<List<Payment>> = paymentRepository.observePaymentUpdates()
}
