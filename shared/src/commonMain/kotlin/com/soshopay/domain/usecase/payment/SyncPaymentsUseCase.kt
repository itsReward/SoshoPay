package com.soshopay.domain.usecase.payment

import com.soshopay.domain.repository.PaymentRepository
import com.soshopay.domain.repository.Result

class SyncPaymentsUseCase(
    private val paymentRepository: PaymentRepository,
) {
    suspend operator fun invoke(): Result<Unit> = paymentRepository.syncPaymentsFromRemote()
}
