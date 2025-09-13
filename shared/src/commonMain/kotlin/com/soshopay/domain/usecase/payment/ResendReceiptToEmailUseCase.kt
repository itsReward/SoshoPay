package com.soshopay.domain.usecase.payment

import com.soshopay.domain.repository.PaymentRepository
import com.soshopay.domain.repository.Result

class ResendReceiptToEmailUseCase(
    private val paymentRepository: PaymentRepository,
) {
    suspend operator fun invoke(
        receiptNumber: String,
        email: String,
    ): Result<Unit> {
        if (receiptNumber.isBlank()) {
            return Result.Error(Exception("Receipt number is required"))
        }

        if (email.isBlank()) {
            return Result.Error(Exception("Email address is required"))
        }

        if (!isValidEmail(email)) {
            return Result.Error(Exception("Invalid email address format"))
        }

        return paymentRepository.resendReceiptToEmail(receiptNumber, email)
    }

    private fun isValidEmail(email: String): Boolean = email.contains("@") && email.contains(".") && email.length > 5
}
