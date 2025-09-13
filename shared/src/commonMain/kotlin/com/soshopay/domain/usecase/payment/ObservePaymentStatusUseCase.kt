package com.soshopay.domain.usecase.payment

import com.soshopay.domain.model.PaymentStatus
import com.soshopay.domain.repository.PaymentRepository
import kotlinx.coroutines.flow.Flow

class ObservePaymentStatusUseCase(
    private val paymentRepository: PaymentRepository,
) {
    operator fun invoke(paymentId: String): Flow<PaymentStatus> = paymentRepository.observePaymentStatus(paymentId)
}
