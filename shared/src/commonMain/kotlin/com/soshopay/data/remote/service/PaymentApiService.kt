package com.soshopay.data.remote

import com.soshopay.domain.model.EarlyPayoffCalculation
import com.soshopay.domain.model.PaymentDashboard
import com.soshopay.domain.model.PaymentHistoryResponse
import com.soshopay.domain.model.PaymentMethodInfo
import com.soshopay.domain.model.PaymentReceipt
import com.soshopay.domain.model.PaymentRequest
import com.soshopay.domain.model.PaymentStatus
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class PaymentApiService(
    private val httpClient: HttpClient,
) {
    // ========== DASHBOARD & OVERVIEW ==========
    suspend fun getPaymentDashboard(): ApiResponse<PaymentDashboard> =
        try {
            val response = httpClient.get("api/payments/dashboard")
            ApiResponse.Success(response.body())
        } catch (e: Exception) {
            ApiResponse.Error(e.message ?: "Failed to get payment dashboard", e)
        }

    suspend fun getPaymentHistory(
        page: Int = 1,
        limit: Int = 20,
    ): ApiResponse<PaymentHistoryResponse> =
        try {
            val response =
                httpClient.get("api/payments/history") {
                    parameter("page", page)
                    parameter("limit", limit)
                }
            ApiResponse.Success(response.body())
        } catch (e: Exception) {
            ApiResponse.Error(e.message ?: "Failed to get payment history", e)
        }

    suspend fun getPaymentMethods(): ApiResponse<PaymentMethodsResponse> =
        try {
            val response = httpClient.get("api/payments/methods")
            ApiResponse.Success(response.body())
        } catch (e: Exception) {
            ApiResponse.Error(e.message ?: "Failed to get payment methods", e)
        }

    // ========== PAYMENT PROCESSING ==========
    suspend fun processPayment(request: PaymentRequest): ApiResponse<PaymentProcessResponse> =
        try {
            val response =
                httpClient.post("api/payments/process") {
                    contentType(ContentType.Application.Json)
                    setBody(request)
                }
            ApiResponse.Success(response.body())
        } catch (e: Exception) {
            ApiResponse.Error(e.message ?: "Failed to process payment", e)
        }

    suspend fun getPaymentStatus(paymentId: String): ApiResponse<PaymentStatusResponse> =
        try {
            val response = httpClient.get("api/payments/$paymentId/status")
            ApiResponse.Success(response.body())
        } catch (e: Exception) {
            ApiResponse.Error(e.message ?: "Failed to get payment status", e)
        }

    suspend fun cancelPayment(paymentId: String): ApiResponse<PaymentCancelResponse> =
        try {
            val response =
                httpClient.post("api/payments/$paymentId/cancel") {
                    contentType(ContentType.Application.Json)
                }
            ApiResponse.Success(response.body())
        } catch (e: Exception) {
            ApiResponse.Error(e.message ?: "Failed to cancel payment", e)
        }

    suspend fun retryFailedPayment(paymentId: String): ApiResponse<PaymentRetryResponse> =
        try {
            val response =
                httpClient.post("api/payments/$paymentId/retry") {
                    contentType(ContentType.Application.Json)
                }
            ApiResponse.Success(response.body())
        } catch (e: Exception) {
            ApiResponse.Error(e.message ?: "Failed to retry payment", e)
        }

    // ========== RECEIPT & DOCUMENTATION ==========
    suspend fun downloadReceipt(receiptNumber: String): ApiResponse<ByteArray> =
        try {
            val response = httpClient.get("api/payments/receipts/$receiptNumber/download")
            ApiResponse.Success(response.body())
        } catch (e: Exception) {
            ApiResponse.Error(e.message ?: "Failed to download receipt", e)
        }

    suspend fun getPaymentReceipt(receiptNumber: String): ApiResponse<PaymentReceipt> =
        try {
            val response = httpClient.get("api/payments/receipts/$receiptNumber")
            ApiResponse.Success(response.body())
        } catch (e: Exception) {
            ApiResponse.Error(e.message ?: "Failed to get payment receipt", e)
        }

    suspend fun resendReceiptToEmail(
        receiptNumber: String,
        email: String,
    ): ApiResponse<ResendReceiptResponse> =
        try {
            val response =
                httpClient.post("api/payments/receipts/$receiptNumber/resend") {
                    contentType(ContentType.Application.Json)
                    setBody(ResendReceiptRequest(email))
                }
            ApiResponse.Success(response.body())
        } catch (e: Exception) {
            ApiResponse.Error(e.message ?: "Failed to resend receipt", e)
        }

    // ========== EARLY PAYOFF ==========
    suspend fun calculateEarlyPayoff(loanId: String): ApiResponse<EarlyPayoffCalculation> =
        try {
            val response = httpClient.get("api/payments/loans/$loanId/early-payoff/calculate")
            ApiResponse.Success(response.body())
        } catch (e: Exception) {
            ApiResponse.Error(e.message ?: "Failed to calculate early payoff", e)
        }

    suspend fun processEarlyPayoff(
        loanId: String,
        request: PaymentRequest,
    ): ApiResponse<EarlyPayoffProcessResponse> =
        try {
            val response =
                httpClient.post("api/payments/loans/$loanId/early-payoff/process") {
                    contentType(ContentType.Application.Json)
                    setBody(request)
                }
            ApiResponse.Success(response.body())
        } catch (e: Exception) {
            ApiResponse.Error(e.message ?: "Failed to process early payoff", e)
        }

    // ========== UTILITY ENDPOINTS ==========
    suspend fun checkPaymentEligibility(
        loanId: String,
        amount: Double,
    ): ApiResponse<PaymentEligibilityResponse> =
        try {
            val response =
                httpClient.post("api/payments/loans/$loanId/check-eligibility") {
                    contentType(ContentType.Application.Json)
                    setBody(PaymentEligibilityRequest(amount))
                }
            ApiResponse.Success(response.body())
        } catch (e: Exception) {
            ApiResponse.Error(e.message ?: "Failed to check payment eligibility", e)
        }

    suspend fun getRecommendedPaymentAmount(loanId: String): ApiResponse<RecommendedPaymentResponse> =
        try {
            val response = httpClient.get("api/payments/loans/$loanId/recommended-amount")
            ApiResponse.Success(response.body())
        } catch (e: Exception) {
            ApiResponse.Error(e.message ?: "Failed to get recommended payment amount", e)
        }
}

// ========== API REQUEST MODELS ==========
@kotlinx.serialization.Serializable
data class ResendReceiptRequest(
    val email: String,
)

@kotlinx.serialization.Serializable
data class PaymentEligibilityRequest(
    val amount: Double,
)

// ========== API RESPONSE MODELS ==========
@kotlinx.serialization.Serializable
data class PaymentMethodsResponse(
    val methods: List<PaymentMethodInfo>,
)

@kotlinx.serialization.Serializable
data class PaymentProcessResponse(
    val paymentId: String,
    val status: String,
    val message: String,
    val transactionReference: String? = null,
    val estimatedProcessingTime: String,
)

@kotlinx.serialization.Serializable
data class PaymentStatusResponse(
    val paymentId: String,
    val status: PaymentStatus,
    val message: String,
    val receiptNumber: String? = null,
    val failureReason: String? = null,
    val updatedAt: Long,
)

@kotlinx.serialization.Serializable
data class PaymentCancelResponse(
    val success: Boolean,
    val message: String,
)

@kotlinx.serialization.Serializable
data class PaymentRetryResponse(
    val paymentId: String,
    val status: String,
    val message: String,
    val transactionReference: String? = null,
)

@kotlinx.serialization.Serializable
data class ResendReceiptResponse(
    val success: Boolean,
    val message: String,
)

@kotlinx.serialization.Serializable
data class EarlyPayoffProcessResponse(
    val paymentId: String,
    val status: String,
    val message: String,
    val earlyPayoffAmount: Double,
    val savingsAmount: Double,
    val transactionReference: String? = null,
)

@kotlinx.serialization.Serializable
data class PaymentEligibilityResponse(
    val eligible: Boolean,
    val reason: String? = null,
    val minimumAmount: Double? = null,
    val maximumAmount: Double? = null,
)

@kotlinx.serialization.Serializable
data class RecommendedPaymentResponse(
    val recommendedAmount: Double,
    val reason: String,
    val breakdown: PaymentBreakdown? = null,
)

@kotlinx.serialization.Serializable
data class PaymentBreakdown(
    val principal: Double,
    val interest: Double,
    val penalties: Double,
    val fees: Double,
)
