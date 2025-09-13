package com.soshopay.domain.usecase.payment

import com.soshopay.domain.model.PaymentReceipt
import com.soshopay.domain.repository.PaymentRepository
import com.soshopay.domain.repository.Result

class GetPaymentReceiptUseCase(
    private val paymentRepository: PaymentRepository,
) {
    suspend operator fun invoke(receiptNumber: String): Result<PaymentReceipt> {
        if (receiptNumber.isBlank()) {
            return Result.Error(Exception("Receipt number is required"))
        }
        return paymentRepository.getPaymentReceipt(receiptNumber)
    }
}
