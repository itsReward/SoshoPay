package com.soshopay.domain.usecase.payment

import com.soshopay.domain.repository.PaymentRepository
import com.soshopay.domain.repository.Result

class DownloadReceiptUseCase(
    private val paymentRepository: PaymentRepository,
) {
    suspend operator fun invoke(receiptNumber: String): Result<ByteArray> {
        if (receiptNumber.isBlank()) {
            return Result.Error(Exception("Receipt number is required"))
        }
        return paymentRepository.downloadReceipt(receiptNumber)
    }
}
