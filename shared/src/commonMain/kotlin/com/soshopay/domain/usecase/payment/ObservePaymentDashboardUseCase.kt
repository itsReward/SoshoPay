package com.soshopay.domain.usecase.payment

import com.soshopay.domain.model.PaymentDashboard
import com.soshopay.domain.repository.PaymentRepository
import kotlinx.coroutines.flow.Flow

class ObservePaymentDashboardUseCase(
    private val paymentRepository: PaymentRepository,
) {
    operator fun invoke(): Flow<PaymentDashboard> = paymentRepository.observeDashboard()
}
