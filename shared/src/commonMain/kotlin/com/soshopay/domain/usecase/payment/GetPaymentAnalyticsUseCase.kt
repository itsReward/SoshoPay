package com.soshopay.domain.usecase.payment

import com.soshopay.domain.model.Payment
import com.soshopay.domain.model.PaymentAnalytics
import com.soshopay.domain.repository.PaymentRepository
import com.soshopay.domain.repository.Result
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class GetPaymentAnalyticsUseCase(
    private val paymentRepository: PaymentRepository,
) {
    suspend operator fun invoke(
        startDate: Long? = null,
        endDate: Long? = null,
    ): Result<PaymentAnalytics> {
        // Get all payments for analysis
        val paymentsResult = paymentRepository.getCachedPayments()

        val payments =
            when (paymentsResult) {
                is Result.Success -> paymentsResult.data
                is Result.Error -> return paymentsResult
                is Result.Loading -> return Result.Error(Exception("Unable to load payments"))
            }

        // Filter by date range if provided
        val filteredPayments =
            payments.filter { payment ->
                val withinStart = startDate?.let { payment.processedAt >= it } ?: true
                val withinEnd = endDate?.let { payment.processedAt <= it } ?: true
                withinStart && withinEnd
            }

        // Calculate analytics
        val analytics =
            PaymentAnalytics(
                totalPayments = filteredPayments.size,
                totalAmount = filteredPayments.sumOf { it.amount },
                successfulPayments = filteredPayments.count { it.isSuccessful() },
                failedPayments = filteredPayments.count { it.isFailed() },
                averagePaymentAmount =
                    if (filteredPayments.isNotEmpty()) {
                        filteredPayments.sumOf { it.amount } / filteredPayments.size
                    } else {
                        0.0
                    },
                paymentMethodBreakdown =
                    filteredPayments
                        .groupBy { it.method }
                        .mapValues { it.value.size },
                monthlyTrend = calculateMonthlyTrend(filteredPayments),
                successRate =
                    if (filteredPayments.isNotEmpty()) {
                        (
                            filteredPayments
                                .count { it.isSuccessful() }
                                .toDouble() / filteredPayments.size
                        ) * 100
                    } else {
                        0.0
                    },
            )

        return Result.Success(analytics)
    }

    private fun calculateMonthlyTrend(payments: List<Payment>): Map<String, Double> =
        payments
            .groupBy { getMonthYear(it.processedAt) }
            .mapValues { entry -> entry.value.sumOf { it.amount } }

    private fun getMonthYear(timestamp: Long): String {
        val instant = Instant.fromEpochMilliseconds(timestamp)
        val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
        return "${localDateTime.month.name.take(3)} ${localDateTime.year}"
    }
}
