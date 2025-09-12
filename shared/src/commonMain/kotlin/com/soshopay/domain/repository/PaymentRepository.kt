package com.soshopay.domain.repository

import com.soshopay.domain.model.EarlyPayoffCalculation
import com.soshopay.domain.model.Payment
import com.soshopay.domain.model.PaymentDashboard
import com.soshopay.domain.model.PaymentHistoryResponse
import com.soshopay.domain.model.PaymentMethodInfo
import com.soshopay.domain.model.PaymentReceipt
import com.soshopay.domain.model.PaymentRequest
import com.soshopay.domain.model.PaymentStatus
import com.soshopay.domain.model.ValidationResult
import kotlinx.coroutines.flow.Flow

interface PaymentRepository {
    // ========== DASHBOARD & OVERVIEW ==========
    suspend fun getPaymentDashboard(): Result<PaymentDashboard>

    suspend fun getPaymentHistory(
        page: Int = 1,
        limit: Int = 20,
    ): Result<PaymentHistoryResponse>

    suspend fun getPaymentMethods(): Result<List<PaymentMethodInfo>>

    // ========== PAYMENT PROCESSING ==========
    suspend fun processPayment(request: PaymentRequest): Result<String>

    suspend fun getPaymentStatus(paymentId: String): Result<PaymentStatus>

    suspend fun cancelPayment(paymentId: String): Result<Unit>

    suspend fun retryFailedPayment(paymentId: String): Result<String>

    // ========== RECEIPT & DOCUMENTATION ==========
    suspend fun downloadReceipt(receiptNumber: String): Result<ByteArray>

    suspend fun getPaymentReceipt(receiptNumber: String): Result<PaymentReceipt>

    suspend fun resendReceiptToEmail(
        receiptNumber: String,
        email: String,
    ): Result<Unit>

    // ========== EARLY PAYOFF ==========
    suspend fun calculateEarlyPayoff(loanId: String): Result<EarlyPayoffCalculation>

    suspend fun processEarlyPayoff(
        loanId: String,
        paymentRequest: PaymentRequest,
    ): Result<String>

    // ========== LOCAL/CACHE METHODS ==========
    fun observePaymentUpdates(): Flow<List<Payment>>

    fun observePaymentStatus(paymentId: String): Flow<PaymentStatus>

    fun observeDashboard(): Flow<PaymentDashboard>

    suspend fun getCachedPayments(): Result<List<Payment>>

    suspend fun getCachedDashboard(): Result<PaymentDashboard?>

    suspend fun syncPaymentsFromRemote(): Result<Unit>

    // ========== VALIDATION & UTILITY ==========
    fun validatePaymentRequest(request: PaymentRequest): ValidationResult

    suspend fun checkPaymentEligibility(
        loanId: String,
        amount: Double,
    ): Result<Boolean>

    suspend fun getRecommendedPaymentAmount(loanId: String): Result<Double>
}
