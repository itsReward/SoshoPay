package com.soshopay.domain.usecase.payment

import com.soshopay.domain.model.PaymentMethodInfo
import com.soshopay.domain.repository.PaymentRepository
import com.soshopay.domain.repository.Result

class GetActivePaymentMethodsUseCase(
    private val paymentRepository: PaymentRepository,
) {
    suspend operator fun invoke(): Result<List<PaymentMethodInfo>> =
        when (val result = paymentRepository.getPaymentMethods()) {
            is Result.Success -> {
                val activeMethods = result.data.filter { it.isActive }
                Result.Success(activeMethods)
            }
            is Result.Error -> result
            is Result.Loading -> result
        }
}
