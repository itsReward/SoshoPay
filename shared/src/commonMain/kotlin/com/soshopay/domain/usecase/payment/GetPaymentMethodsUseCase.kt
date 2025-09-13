package com.soshopay.domain.usecase.payment

import com.soshopay.domain.model.PaymentMethodInfo
import com.soshopay.domain.repository.PaymentRepository
import com.soshopay.domain.repository.Result

class GetPaymentMethodsUseCase(
    private val paymentRepository: PaymentRepository,
) {
    suspend operator fun invoke(): Result<List<PaymentMethodInfo>> = paymentRepository.getPaymentMethods()
}
