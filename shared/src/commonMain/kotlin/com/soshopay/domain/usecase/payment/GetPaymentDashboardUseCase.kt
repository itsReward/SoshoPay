// File: shared/src/commonMain/kotlin/com/soshopay/domain/usecase/payment/PaymentDashboardUseCases.kt
package com.soshopay.domain.usecase.payment

import com.soshopay.domain.model.PaymentDashboard
import com.soshopay.domain.repository.PaymentRepository
import com.soshopay.domain.repository.Result

// ========== PAYMENT DASHBOARD USE CASES ==========

class GetPaymentDashboardUseCase(
    private val paymentRepository: PaymentRepository,
) {
    suspend operator fun invoke(): Result<PaymentDashboard> = paymentRepository.getPaymentDashboard()
}
