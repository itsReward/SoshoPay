package com.soshopay.data.repository

import com.soshopay.data.local.CacheManager
import com.soshopay.data.local.LocalPaymentStorage
import com.soshopay.data.remote.PaymentApiService
import com.soshopay.domain.model.EarlyPayoffCalculation
import com.soshopay.domain.model.Payment
import com.soshopay.domain.model.PaymentDashboard
import com.soshopay.domain.model.PaymentHistoryResponse
import com.soshopay.domain.model.PaymentMethodInfo
import com.soshopay.domain.model.PaymentReceipt
import com.soshopay.domain.model.PaymentRequest
import com.soshopay.domain.model.PaymentStatus
import com.soshopay.domain.model.ValidationResult
import com.soshopay.domain.repository.PaymentRepository
import com.soshopay.domain.repository.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock

/**
 * Implementation of [PaymentRepository] for managing all payment-related operations in the SoshoPay domain.
 *
 * This class coordinates between the remote API ([PaymentApiService]), local storage ([LocalPaymentStorage]),
 * and cache manager ([CacheManager]) to provide robust, efficient, and validated payment management.
 * It supports workflows for dashboard retrieval, payment history, payment processing, status tracking,
 * receipt/documentation, early payoff, local caching, and validation.
 *
 * Key features:
 * - Uses local cache for fast access and offline fallback, with cache validity checks and sync intervals.
 * - Validates all payment requests before submission, returning detailed errors if validation fails.
 * - Updates local cache after successful remote operations to keep data in sync.
 * - Provides fallback to cached data on API errors where possible.
 * - Supports reactive payment observation via [Flow].
 * - Handles payment processing, status tracking, receipts, early payoff, and recommended payment calculations.
 *
 * @property paymentApiService Remote API service for payment operations.
 * @property localStorage Local storage for payment data and cache.
 * @property cacheManager Cache manager for sync intervals and cache metadata.
 */
class PaymentRepositoryImpl(
    private val paymentApiService: PaymentApiService,
    private val localStorage: LocalPaymentStorage,
    private val cacheManager: CacheManager,
) : PaymentRepository {
    /**
     * Retrieves the payment dashboard, using cache if valid. Checks cache validity and sync interval before fetching from API.
     * Falls back to cached data if API fails.
     * @return [Result] containing [PaymentDashboard] if successful, or an error.
     */
    override suspend fun getPaymentDashboard(): Result<PaymentDashboard> {
        return try {
            // Check cache first
            val cachedDashboard = localStorage.getPaymentDashboard()
            val shouldSync = localStorage.shouldSync(CacheManager.CACHE_DASHBOARD, CacheManager.SYNC_INTERVAL_DASHBOARD)

            if (cachedDashboard != null && !shouldSync) {
                return Result.Success(cachedDashboard)
            }

            // Fetch from API
            val apiResponse = paymentApiService.getPaymentDashboard()
            if (apiResponse.isSuccess()) {
                val dashboard = apiResponse.getOrNull()!!
                localStorage.savePaymentDashboard(dashboard)
                localStorage.updateLastSyncTime(CacheManager.CACHE_DASHBOARD, Clock.System.now().toEpochMilliseconds())
                Result.Success(dashboard)
            } else {
                // Return cached data if API fails and cache exists
                cachedDashboard?.let { Result.Success(it) }
                    ?: Result.Error(Exception(apiResponse.getErrorOrNull() ?: "Failed to get payment dashboard"))
            }
        } catch (e: Exception) {
            // Try to return cached data on exception
            val cachedDashboard = localStorage.getPaymentDashboard()
            cachedDashboard?.let { Result.Success(it) } ?: Result.Error(e)
        }
    }

    /**
     * Retrieves the user's payment history with pagination. Caches payments for offline access and falls back to cache if API fails.
     * @param page The page number (default: 1).
     * @param limit The number of items per page (default: 20).
     * @return [Result] containing [PaymentHistoryResponse] if successful, or an error.
     */
    override suspend fun getPaymentHistory(
        page: Int,
        limit: Int,
    ): Result<PaymentHistoryResponse> =
        try {
            val apiResponse = paymentApiService.getPaymentHistory(page, limit)
            if (apiResponse.isSuccess()) {
                val response = apiResponse.getOrNull()!!
                // Cache the payments for offline access
                localStorage.insertPayments(response.payments)
                localStorage.updateLastSyncTime(CacheManager.CACHE_PAYMENTS, Clock.System.now().toEpochMilliseconds())
                Result.Success(response)
            } else {
                // Try to return cached data if API fails
                if (page == 1) {
                    val cachedPayments = localStorage.getAllPayments()
                    if (cachedPayments.isNotEmpty()) {
                        val cachedResponse =
                            PaymentHistoryResponse(
                                payments = cachedPayments,
                                currentPage = 1,
                                totalPages = 1,
                                totalCount = cachedPayments.size,
                                hasNext = false,
                                hasPrevious = false,
                            )
                        Result.Success(cachedResponse)
                    } else {
                        Result.Error(Exception(apiResponse.getErrorOrNull() ?: "Failed to get payment history"))
                    }
                } else {
                    Result.Error(Exception(apiResponse.getErrorOrNull() ?: "Failed to get payment history"))
                }
            }
        } catch (e: Exception) {
            // Try cached data on exception
            if (page == 1) {
                val cachedPayments = localStorage.getAllPayments()
                if (cachedPayments.isNotEmpty()) {
                    val cachedResponse =
                        PaymentHistoryResponse(
                            payments = cachedPayments,
                            currentPage = 1,
                            totalPages = 1,
                            totalCount = cachedPayments.size,
                            hasNext = false,
                            hasPrevious = false,
                        )
                    Result.Success(cachedResponse)
                } else {
                    Result.Error(e)
                }
            } else {
                Result.Error(e)
            }
        }

    /**
     * Retrieves the available payment methods, using cache if valid. Checks cache validity and sync interval before fetching from API.
     * Falls back to cached data if API fails.
     * @return [Result] containing a list of [PaymentMethodInfo] if successful, or an error.
     */
    override suspend fun getPaymentMethods(): Result<List<PaymentMethodInfo>> {
        return try {
            // Check cache first
            val cachedMethods = localStorage.getPaymentMethods()
            val shouldSync = localStorage.shouldSync(CacheManager.CACHE_METHODS, CacheManager.SYNC_INTERVAL_FORM_DATA)

            if (cachedMethods.isNotEmpty() && !shouldSync) {
                return Result.Success(cachedMethods)
            }

            // Fetch from API
            val apiResponse = paymentApiService.getPaymentMethods()
            if (apiResponse.isSuccess()) {
                val response = apiResponse.getOrNull()!!
                localStorage.savePaymentMethods(response.methods)
                localStorage.updateLastSyncTime(CacheManager.CACHE_METHODS, Clock.System.now().toEpochMilliseconds())
                Result.Success(response.methods)
            } else {
                // Return cached data if API fails and cache exists
                if (cachedMethods.isNotEmpty()) {
                    Result.Success(cachedMethods)
                } else {
                    Result.Error(Exception(apiResponse.getErrorOrNull() ?: "Failed to get payment methods"))
                }
            }
        } catch (e: Exception) {
            // Try to return cached data on exception
            val cachedMethods = localStorage.getPaymentMethods()
            if (cachedMethods.isNotEmpty()) {
                Result.Success(cachedMethods)
            } else {
                Result.Error(e)
            }
        }
    }

    /**
     * Processes a payment after validating the request. Saves payment to local storage with processing status.
     * @param request The payment request data.
     * @return [Result] containing the payment ID if successful, or an error.
     */
    override suspend fun processPayment(request: PaymentRequest): Result<String> {
        return try {
            // Validate payment request first
            val validation = validatePaymentRequest(request)
            if (!validation.isValid) {
                return Result.Error(Exception(validation.getErrorMessage() ?: "Invalid payment request"))
            }

            val apiResponse = paymentApiService.processPayment(request)
            if (apiResponse.isSuccess()) {
                val response = apiResponse.getOrNull()!!

                // Save payment to local storage with processing status
                val payment =
                    Payment(
                        id = generateId(),
                        userId = getCurrentUserId(), // This would need to be injected
                        loanId = request.loanId,
                        paymentId = response.paymentId,
                        amount = request.amount,
                        method = request.paymentMethod,
                        phoneNumber = request.phoneNumber,
                        receiptNumber = "", // Will be updated when payment completes
                        status = PaymentStatus.PROCESSING,
                        processedAt = Clock.System.now().toEpochMilliseconds(),
                        createdAt = Clock.System.now().toEpochMilliseconds(),
                    )
                localStorage.insertPayment(payment)

                Result.Success(response.paymentId)
            } else {
                Result.Error(Exception(apiResponse.getErrorOrNull() ?: "Failed to process payment"))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    /**
     * Retrieves the status of a payment and updates local payment record.
     * @param paymentId The payment ID.
     * @return [Result] containing [PaymentStatus] if successful, or an error.
     */
    override suspend fun getPaymentStatus(paymentId: String): Result<PaymentStatus> =
        try {
            val apiResponse = paymentApiService.getPaymentStatus(paymentId)
            if (apiResponse.isSuccess()) {
                val response = apiResponse.getOrNull()!!

                // Update local payment record
                val existingPayment = localStorage.getPaymentById(paymentId)
                existingPayment?.let { payment ->
                    val updatedPayment =
                        payment.copy(
                            status = response.status,
                            receiptNumber = response.receiptNumber ?: payment.receiptNumber,
                            failureReason = response.failureReason,
                            updatedAt = Clock.System.now().toEpochMilliseconds(),
                        )
                    localStorage.updatePayment(updatedPayment)
                }

                Result.Success(response.status)
            } else {
                Result.Error(Exception(apiResponse.getErrorOrNull() ?: "Failed to get payment status"))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }

    /**
     * Cancels a payment and updates local payment record.
     * @param paymentId The payment ID.
     * @return [Result] containing [Unit] if successful, or an error.
     */
    override suspend fun cancelPayment(paymentId: String): Result<Unit> =
        try {
            val apiResponse = paymentApiService.cancelPayment(paymentId)
            if (apiResponse.isSuccess()) {
                // Update local payment record
                val existingPayment = localStorage.getPaymentById(paymentId)
                existingPayment?.let { payment ->
                    val updatedPayment =
                        payment.copy(
                            status = PaymentStatus.CANCELLED,
                            updatedAt = Clock.System.now().toEpochMilliseconds(),
                        )
                    localStorage.updatePayment(updatedPayment)
                }

                Result.Success(Unit)
            } else {
                Result.Error(Exception(apiResponse.getErrorOrNull() ?: "Failed to cancel payment"))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }

    /**
     * Retries a failed payment and updates local payment record.
     * @param paymentId The payment ID.
     * @return [Result] containing the new payment ID if successful, or an error.
     */
    override suspend fun retryFailedPayment(paymentId: String): Result<String> =
        try {
            val apiResponse = paymentApiService.retryFailedPayment(paymentId)
            if (apiResponse.isSuccess()) {
                val response = apiResponse.getOrNull()!!

                // Update local payment record
                val existingPayment = localStorage.getPaymentById(paymentId)
                existingPayment?.let { payment ->
                    val updatedPayment =
                        payment.copy(
                            status = PaymentStatus.PROCESSING,
                            updatedAt = Clock.System.now().toEpochMilliseconds(),
                        )
                    localStorage.updatePayment(updatedPayment)
                }

                Result.Success(response.paymentId)
            } else {
                Result.Error(Exception(apiResponse.getErrorOrNull() ?: "Failed to retry payment"))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }

    /**
     * Downloads the payment receipt document for the specified receipt number.
     * @param receiptNumber The receipt number.
     * @return [Result] containing the receipt as a [ByteArray] if successful, or an error.
     */
    override suspend fun downloadReceipt(receiptNumber: String): Result<ByteArray> =
        try {
            val apiResponse = paymentApiService.downloadReceipt(receiptNumber)
            if (apiResponse.isSuccess()) {
                Result.Success(apiResponse.getOrNull()!!)
            } else {
                Result.Error(Exception(apiResponse.getErrorOrNull() ?: "Failed to download receipt"))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }

    /**
     * Retrieves the payment receipt, using cache if available. Falls back to cached data if API fails.
     * @param receiptNumber The receipt number.
     * @return [Result] containing [PaymentReceipt] if successful, or an error.
     */
    override suspend fun getPaymentReceipt(receiptNumber: String): Result<PaymentReceipt> {
        return try {
            // Check cache first
            val cachedReceipt = localStorage.getPaymentReceipt(receiptNumber)
            if (cachedReceipt != null) {
                return Result.Success(cachedReceipt)
            }

            // Fetch from API
            val apiResponse = paymentApiService.getPaymentReceipt(receiptNumber)
            if (apiResponse.isSuccess()) {
                val receipt = apiResponse.getOrNull()!!
                localStorage.savePaymentReceipt(receipt)
                Result.Success(receipt)
            } else {
                Result.Error(Exception(apiResponse.getErrorOrNull() ?: "Failed to get payment receipt"))
            }
        } catch (e: Exception) {
            // Try cached data on exception
            val cachedReceipt = localStorage.getPaymentReceipt(receiptNumber)
            cachedReceipt?.let { Result.Success(it) } ?: Result.Error(e)
        }
    }

    /**
     * Resends the payment receipt to the specified email address.
     * @param receiptNumber The receipt number.
     * @param email The email address to send the receipt to.
     * @return [Result] containing [Unit] if successful, or an error.
     */
    override suspend fun resendReceiptToEmail(
        receiptNumber: String,
        email: String,
    ): Result<Unit> =
        try {
            val apiResponse = paymentApiService.resendReceiptToEmail(receiptNumber, email)
            if (apiResponse.isSuccess()) {
                Result.Success(Unit)
            } else {
                Result.Error(Exception(apiResponse.getErrorOrNull() ?: "Failed to resend receipt"))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }

    /**
     * Calculates the early payoff amount for a loan, using cache if fresh. Falls back to cached data if API fails.
     * @param loanId The loan ID.
     * @return [Result] containing [EarlyPayoffCalculation] if successful, or an error.
     */
    override suspend fun calculateEarlyPayoff(loanId: String): Result<EarlyPayoffCalculation> {
        return try {
            // Check cache first
            val cachedCalculation = localStorage.getEarlyPayoffCalculation(loanId)
            if (cachedCalculation != null) {
                // Check if calculation is still fresh (less than 1 hour old)
                val isExpired = (Clock.System.now().toEpochMilliseconds() - cachedCalculation.calculatedAt) > 3600000
                if (!isExpired) {
                    return Result.Success(cachedCalculation)
                }
            }

            // Fetch from API
            val apiResponse = paymentApiService.calculateEarlyPayoff(loanId)
            if (apiResponse.isSuccess()) {
                val calculation = apiResponse.getOrNull()!!
                localStorage.saveEarlyPayoffCalculation(calculation)
                Result.Success(calculation)
            } else {
                // Return cached data if API fails and cache exists
                cachedCalculation?.let { Result.Success(it) }
                    ?: Result.Error(Exception(apiResponse.getErrorOrNull() ?: "Failed to calculate early payoff"))
            }
        } catch (e: Exception) {
            // Try cached data on exception
            val cachedCalculation = localStorage.getEarlyPayoffCalculation(loanId)
            cachedCalculation?.let { Result.Success(it) } ?: Result.Error(e)
        }
    }

    /**
     * Processes an early payoff payment after validating the request. Saves payment to local storage and clears early payoff cache.
     * @param loanId The loan ID.
     * @param paymentRequest The payment request data.
     * @return [Result] containing the payment ID if successful, or an error.
     */
    override suspend fun processEarlyPayoff(
        loanId: String,
        paymentRequest: PaymentRequest,
    ): Result<String> {
        return try {
            // Validate payment request first
            val validation = validatePaymentRequest(paymentRequest)
            if (!validation.isValid) {
                return Result.Error(Exception(validation.getErrorMessage() ?: "Invalid payment request"))
            }

            val apiResponse = paymentApiService.processEarlyPayoff(loanId, paymentRequest)
            if (apiResponse.isSuccess()) {
                val response = apiResponse.getOrNull()!!

                // Save payment to local storage
                val payment =
                    Payment(
                        id = generateId(),
                        userId = getCurrentUserId(),
                        loanId = loanId,
                        paymentId = response.paymentId,
                        amount = response.earlyPayoffAmount,
                        method = paymentRequest.paymentMethod,
                        phoneNumber = paymentRequest.phoneNumber,
                        receiptNumber = "",
                        status = PaymentStatus.PROCESSING,
                        processedAt = Clock.System.now().toEpochMilliseconds(),
                        createdAt = Clock.System.now().toEpochMilliseconds(),
                    )
                localStorage.insertPayment(payment)

                // Clear early payoff calculation cache
                localStorage.deleteEarlyPayoffCalculation(loanId)

                Result.Success(response.paymentId)
            } else {
                Result.Error(Exception(apiResponse.getErrorOrNull() ?: "Failed to process early payoff"))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    /**
     * Observes updates to the user's payments as a [Flow].
     * @return A [Flow] emitting the current list of [Payment] whenever updates occur.
     */
    override fun observePaymentUpdates(): Flow<List<Payment>> = localStorage.observePayments()

    /**
     * Observes the status of a payment by payment ID as a [Flow]. Emits the current status from cache.
     * @param paymentId The payment ID.
     * @return A [Flow] emitting the current [PaymentStatus] for the payment.
     */
    override fun observePaymentStatus(paymentId: String): Flow<PaymentStatus> =
        localStorage.observePaymentById(paymentId).map { payment ->
            payment?.status ?: PaymentStatus.FAILED
        }

    /**
     * Observes the payment dashboard as a [Flow]. Emits a default dashboard if cache is empty.
     * @return A [Flow] emitting the current [PaymentDashboard] whenever updates occur.
     */
    override fun observeDashboard(): Flow<PaymentDashboard> =
        localStorage.observePaymentDashboard().map { dashboard ->
            dashboard ?: PaymentDashboard(
                totalOutstanding = 0.0,
                nextPaymentAmount = 0.0,
                nextPaymentDate = 0L,
                overdueAmount = 0.0,
                overdueCount = 0,
                paymentSummaries = emptyList(),
                recentPayments = emptyList(),
            )
        }

    /**
     * Retrieves the cached payments from local storage.
     * @return [Result] containing a list of [Payment] if successful, or an error.
     */
    override suspend fun getCachedPayments(): Result<List<Payment>> =
        try {
            val payments = localStorage.getAllPayments()
            Result.Success(payments)
        } catch (e: Exception) {
            Result.Error(e)
        }

    /**
     * Retrieves the cached payment dashboard from local storage.
     * @return [Result] containing [PaymentDashboard] if successful, or an error.
     */
    override suspend fun getCachedDashboard(): Result<PaymentDashboard?> =
        try {
            val dashboard = localStorage.getPaymentDashboard()
            Result.Success(dashboard)
        } catch (e: Exception) {
            Result.Error(e)
        }

    /**
     * Synchronizes payments from the remote server to local storage.
     * @return [Result] containing [Unit] if successful, or an error.
     */
    override suspend fun syncPaymentsFromRemote(): Result<Unit> =
        try {
            val apiResponse = paymentApiService.getPaymentHistory()
            if (apiResponse.isSuccess()) {
                val response = apiResponse.getOrNull()!!
                localStorage.insertPayments(response.payments)
                localStorage.updateLastSyncTime(CacheManager.CACHE_PAYMENTS, Clock.System.now().toEpochMilliseconds())
                Result.Success(Unit)
            } else {
                Result.Error(Exception(apiResponse.getErrorOrNull() ?: "Failed to sync payments"))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }

    /**
     * Validates a payment request before submission.
     * Checks for required fields, value constraints, and method validity.
     * @param request The payment request data.
     * @return [ValidationResult] indicating validity, errors, and warnings.
     */
    override fun validatePaymentRequest(request: PaymentRequest): ValidationResult = request.validate()

    /**
     * Checks if a payment is eligible for the specified loan and amount.
     * @param loanId The loan ID.
     * @param amount The payment amount.
     * @return [Result] containing true if eligible, false otherwise.
     */
    override suspend fun checkPaymentEligibility(
        loanId: String,
        amount: Double,
    ): Result<Boolean> =
        try {
            val apiResponse = paymentApiService.checkPaymentEligibility(loanId, amount)
            if (apiResponse.isSuccess()) {
                val response = apiResponse.getOrNull()!!
                Result.Success(response.eligible)
            } else {
                Result.Error(Exception(apiResponse.getErrorOrNull() ?: "Failed to check payment eligibility"))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }

    /**
     * Retrieves the recommended payment amount for the specified loan.
     * @param loanId The loan ID.
     * @return [Result] containing the recommended amount if successful, or an error.
     */
    override suspend fun getRecommendedPaymentAmount(loanId: String): Result<Double> =
        try {
            val apiResponse = paymentApiService.getRecommendedPaymentAmount(loanId)
            if (apiResponse.isSuccess()) {
                val response = apiResponse.getOrNull()!!
                Result.Success(response.recommendedAmount)
            } else {
                Result.Error(Exception(apiResponse.getErrorOrNull() ?: "Failed to get recommended payment amount"))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }

    // ========== PRIVATE UTILITY METHODS ==========

    /**
     * Generates a unique payment ID for local storage.
     * @return A unique payment ID string.
     */
    private fun generateId(): String = "payment_${Clock.System.now().toEpochMilliseconds()}_${(1000..9999).random()}"

    /**
     * Retrieves the current user ID. Typically injected from auth repository or user session.
     * @return The current user ID string.
     */
    private fun getCurrentUserId(): String {
        // This would typically be injected from auth repository or user session
        // For now, we'll return a placeholder
        return "current_user_id"
    }
}
