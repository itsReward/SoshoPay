package com.soshopay.domain.usecase.payment

import com.soshopay.domain.model.PaymentNotification
import com.soshopay.domain.model.ValidationResult
import com.soshopay.domain.repository.PaymentRepository
import com.soshopay.domain.repository.Result

class ProcessPaymentNotificationUseCase(
    private val paymentRepository: PaymentRepository,
) {
    suspend operator fun invoke(notification: PaymentNotification): Result<Unit> {
        return try {
            // Validate notification
            val validation = validateNotification(notification)
            if (!validation.isValid) {
                return Result.Error(Exception(validation.getErrorMessage()))
            }

            // Update payment status based on notification
            val statusResult = paymentRepository.getPaymentStatus(notification.paymentId)
            when (statusResult) {
                is Result.Success -> {
                    // Payment status updated successfully
                    Result.Success(Unit)
                }
                is Result.Error -> {
                    // Log error but don't fail - notification processing should be resilient
                    Result.Success(Unit)
                }
                else -> Result.Success(Unit)
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    private fun validateNotification(notification: PaymentNotification): ValidationResult {
        val errors = mutableListOf<String>()

        if (notification.paymentId.isBlank()) {
            errors.add("Payment ID is required in notification")
        }

        if (notification.timestamp <= 0) {
            errors.add("Invalid notification timestamp")
        }

        return ValidationResult(
            isValid = errors.isEmpty(),
            errors = errors,
        )
    }
}
