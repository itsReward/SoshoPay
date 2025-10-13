package com.soshopay.domain.repository

import com.soshopay.data.remote.PaymentProcessResponse
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

/**
 * Repository abstraction for payment-related operations in the SoshoPay domain.
 *
 * Implementations of this interface handle all payment workflows, including dashboard retrieval,
 * payment history, payment processing, status tracking, receipt/documentation, early payoff,
 * local caching, and validation. All operations return a [Result] type for robust error handling
 * and success/failure reporting, or a [Flow] for reactive updates.
 *
 * **Dashboard & Overview:**
 * - Retrieve payment dashboard and history, and available payment methods.
 *
 * **Payment Processing:**
 * - Process payments, check status, cancel or retry failed payments.
 *
 * **Receipt & Documentation:**
 * - Download and retrieve payment receipts, resend receipts to email.
 *
 * **Early Payoff:**
 * - Calculate and process early payoff for loans.
 *
 * **Local/Cache Methods:**
 * - Observe payment updates, status, dashboard, and retrieve cached data.
 *
 * **Validation & Utility:**
 * - Validate payment requests, check payment eligibility, and get recommended payment amounts.
 *
 * All suspend functions support asynchronous/coroutine-based execution.
 */
interface PaymentRepository {
    // ========== DASHBOARD & OVERVIEW ==========
    suspend fun getPaymentDashboard(): Result<PaymentDashboard>

    suspend fun getPaymentHistory(
        page: Int = 1,
        limit: Int = 20,
    ): Result<PaymentHistoryResponse>

    suspend fun getPaymentMethods(): Result<List<PaymentMethodInfo>>

    // ========== PAYMENT PROCESSING ==========
    suspend fun processPayment(request: PaymentRequest): Result<PaymentProcessResponse>

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
