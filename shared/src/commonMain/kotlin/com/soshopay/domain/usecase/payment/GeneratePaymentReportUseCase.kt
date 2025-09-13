package com.soshopay.domain.usecase.payment

import com.soshopay.domain.model.Payment
import com.soshopay.domain.model.PaymentReport
import com.soshopay.domain.model.PaymentReportType
import com.soshopay.domain.repository.PaymentRepository
import com.soshopay.domain.repository.Result
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class GeneratePaymentReportUseCase(
    private val paymentRepository: PaymentRepository,
) {
    suspend operator fun invoke(
        reportType: PaymentReportType,
        startDate: Long,
        endDate: Long,
    ): Result<PaymentReport> {
        // Get payments for the period
        val paymentsResult = paymentRepository.getCachedPayments()

        val allPayments =
            when (paymentsResult) {
                is Result.Success -> paymentsResult.data
                is Result.Error -> return paymentsResult
                is Result.Loading -> return Result.Error(Exception("Unable to load payments"))
            }

        // Filter payments by date range
        val payments =
            allPayments.filter { payment ->
                payment.processedAt in startDate..endDate
            }

        val report =
            when (reportType) {
                PaymentReportType.SUMMARY -> generateSummaryReport(payments, startDate, endDate)
                PaymentReportType.DETAILED -> generateDetailedReport(payments, startDate, endDate)
                PaymentReportType.ANALYTICS -> generateAnalyticsReport(payments, startDate, endDate)
            }

        return Result.Success(report)
    }

    private fun generateSummaryReport(
        payments: List<Payment>,
        startDate: Long,
        endDate: Long,
    ): PaymentReport =
        PaymentReport(
            type = PaymentReportType.SUMMARY,
            startDate = startDate,
            endDate = endDate,
            totalPayments = payments.size,
            totalAmount = payments.sumOf { it.amount },
            successfulPayments = payments.count { it.isSuccessful() },
            failedPayments = payments.count { it.isFailed() },
            pendingPayments = payments.count { it.isPending() },
            averageAmount = if (payments.isNotEmpty()) payments.sumOf { it.amount } / payments.size else 0.0,
            data =
                mapOf(
                    "summary" to
                        payments
                            .groupBy { it.status.name }
                            .mapValues { it.value.size },
                ),
        )

    private fun generateDetailedReport(
        payments: List<Payment>,
        startDate: Long,
        endDate: Long,
    ): PaymentReport =
        PaymentReport(
            type = PaymentReportType.DETAILED,
            startDate = startDate,
            endDate = endDate,
            totalPayments = payments.size,
            totalAmount = payments.sumOf { it.amount },
            successfulPayments = payments.count { it.isSuccessful() },
            failedPayments = payments.count { it.isFailed() },
            pendingPayments = payments.count { it.isPending() },
            averageAmount = if (payments.isNotEmpty()) payments.sumOf { it.amount } / payments.size else 0.0,
            data =
                mapOf(
                    "payments" to payments,
                    "methodBreakdown" to
                        payments
                            .groupBy { it.method }
                            .mapValues { entry ->
                                mapOf(
                                    "count" to entry.value.size,
                                    "amount" to entry.value.sumOf { it.amount },
                                )
                            },
                    "dailyTrend" to
                        payments
                            .groupBy { getDayString(it.processedAt) }
                            .mapValues { it.value.sumOf { payment -> payment.amount } },
                ),
        )

    private fun generateAnalyticsReport(
        payments: List<Payment>,
        startDate: Long,
        endDate: Long,
    ): PaymentReport {
        val successRate =
            if (payments.isNotEmpty()) {
                (payments.count { it.isSuccessful() }.toDouble() / payments.size) * 100
            } else {
                0.0
            }

        val failureReasons =
            payments
                .filter { it.isFailed() && !it.failureReason.isNullOrBlank() }
                .groupBy { it.failureReason!! }
                .mapValues { it.value.size }

        return PaymentReport(
            type = PaymentReportType.ANALYTICS,
            startDate = startDate,
            endDate = endDate,
            totalPayments = payments.size,
            totalAmount = payments.sumOf { it.amount },
            successfulPayments = payments.count { it.isSuccessful() },
            failedPayments = payments.count { it.isFailed() },
            pendingPayments = payments.count { it.isPending() },
            averageAmount = if (payments.isNotEmpty()) payments.sumOf { it.amount } / payments.size else 0.0,
            data =
                mapOf(
                    "successRate" to successRate,
                    "failureReasons" to failureReasons,
                    "hourlyDistribution" to
                        payments
                            .groupBy { getHourString(it.processedAt) }
                            .mapValues { it.value.size },
                    "amountRanges" to categorizeByAmountRanges(payments),
                ),
        )
    }

    private fun getDayString(timestamp: Long): String {
        val instant = Instant.fromEpochMilliseconds(timestamp)
        val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
        return "${localDateTime.year}-${localDateTime.monthNumber.toString().padStart(
            2,
            '0',
        )}-${localDateTime.dayOfMonth.toString().padStart(2, '0')}"
    }

    private fun getHourString(timestamp: Long): String {
        val instant = Instant.fromEpochMilliseconds(timestamp)
        val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
        return "${localDateTime.hour.toString().padStart(2, '0')}:00"
    }

    private fun categorizeByAmountRanges(payments: List<Payment>): Map<String, Int> =
        payments
            .groupBy { payment ->
                when {
                    payment.amount <= 50 -> "0-50"
                    payment.amount <= 100 -> "51-100"
                    payment.amount <= 200 -> "101-200"
                    payment.amount <= 500 -> "201-500"
                    else -> "500+"
                }
            }.mapValues { it.value.size }
}
