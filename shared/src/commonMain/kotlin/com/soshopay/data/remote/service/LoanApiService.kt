package com.soshopay.data.remote

import com.soshopay.domain.model.CashLoanApplication
import com.soshopay.domain.model.CashLoanCalculationRequest
import com.soshopay.domain.model.CashLoanFormData
import com.soshopay.domain.model.CashLoanTerms
import com.soshopay.domain.model.Loan
import com.soshopay.domain.model.LoanDetails
import com.soshopay.domain.model.LoanHistoryResponse
import com.soshopay.domain.model.PayGoCalculationRequest
import com.soshopay.domain.model.PayGoLoanApplication
import com.soshopay.domain.model.PayGoLoanTerms
import com.soshopay.domain.model.PayGoProduct
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class LoanApiService(
    private val httpClient: HttpClient,
) {
    // ========== CASH LOAN ENDPOINTS ==========
    suspend fun getCashLoanFormData(): ApiResponse<CashLoanFormData> =
        try {
            val response = httpClient.get("api/loans/cash/form-data")
            ApiResponse.Success(response.body())
        } catch (e: Exception) {
            ApiResponse.Error(e.message ?: "Failed to get cash loan form data", e)
        }

    suspend fun calculateCashLoanTerms(request: CashLoanCalculationRequest): ApiResponse<CashLoanTerms> =
        try {
            val response =
                httpClient.post("api/loans/cash/calculate") {
                    contentType(ContentType.Application.Json)
                    setBody(request)
                }
            ApiResponse.Success(response.body())
        } catch (e: Exception) {
            ApiResponse.Error(e.message ?: "Failed to calculate cash loan terms", e)
        }

    suspend fun submitCashLoanApplication(application: CashLoanApplication): ApiResponse<CashLoanApplicationResponse> =
        try {
            val response =
                httpClient.post("api/loans/cash/apply") {
                    contentType(ContentType.Application.Json)
                    setBody(application)
                }
            ApiResponse.Success(response.body())
        } catch (e: Exception) {
            ApiResponse.Error(e.message ?: "Failed to submit cash loan application", e)
        }

    // ========== PAYGO LOAN ENDPOINTS ==========
    suspend fun getPayGoCategories(): ApiResponse<PayGoCategoriesResponse> =
        try {
            val response = httpClient.get("api/loans/paygo/categories")
            ApiResponse.Success(response.body())
        } catch (e: Exception) {
            ApiResponse.Error(e.message ?: "Failed to get PayGo categories", e)
        }

    suspend fun getCategoryProducts(categoryId: String): ApiResponse<PayGoProductsResponse> =
        try {
            val response = httpClient.get("api/loans/paygo/categories/$categoryId/products")
            ApiResponse.Success(response.body())
        } catch (e: Exception) {
            ApiResponse.Error(e.message ?: "Failed to get category products", e)
        }

    suspend fun calculatePayGoTerms(request: PayGoCalculationRequest): ApiResponse<PayGoLoanTerms> =
        try {
            val response =
                httpClient.post("api/loans/paygo/calculate") {
                    contentType(ContentType.Application.Json)
                    setBody(request)
                }
            ApiResponse.Success(response.body())
        } catch (e: Exception) {
            ApiResponse.Error(e.message ?: "Failed to calculate PayGo loan terms", e)
        }

    suspend fun submitPayGoApplication(application: PayGoLoanApplication): ApiResponse<PayGoApplicationResponse> =
        try {
            val response =
                httpClient.post("api/loans/paygo/apply") {
                    contentType(ContentType.Application.Json)
                    setBody(application)
                }
            ApiResponse.Success(response.body())
        } catch (e: Exception) {
            ApiResponse.Error(e.message ?: "Failed to submit PayGo loan application", e)
        }

    // ========== GENERAL LOAN ENDPOINTS ==========
    suspend fun getLoanHistory(
        filter: String = "all",
        page: Int = 1,
        limit: Int = 20,
    ): ApiResponse<LoanHistoryResponse> =
        try {
            val response =
                httpClient.get("api/loans/history") {
                    parameter("filter", filter)
                    parameter("page", page)
                    parameter("limit", limit)
                }
            ApiResponse.Success(response.body())
        } catch (e: Exception) {
            ApiResponse.Error(e.message ?: "Failed to get loan history", e)
        }

    suspend fun getLoanDetails(loanId: String): ApiResponse<LoanDetails> =
        try {
            val response = httpClient.get("api/loans/$loanId/details")
            ApiResponse.Success(response.body())
        } catch (e: Exception) {
            ApiResponse.Error(e.message ?: "Failed to get loan details", e)
        }

    suspend fun getCurrentLoans(): ApiResponse<CurrentLoansResponse> =
        try {
            val response = httpClient.get("api/loans/current")
            ApiResponse.Success(response.body())
        } catch (e: Exception) {
            ApiResponse.Error(e.message ?: "Failed to get current loans", e)
        }

    suspend fun downloadLoanAgreement(loanId: String): ApiResponse<ByteArray> =
        try {
            val response = httpClient.get("api/loans/$loanId/agreement")
            ApiResponse.Success(response.body())
        } catch (e: Exception) {
            ApiResponse.Error(e.message ?: "Failed to download loan agreement", e)
        }

    suspend fun withdrawApplication(applicationId: String): ApiResponse<WithdrawApplicationResponse> =
        try {
            val response =
                httpClient.post("api/loans/applications/$applicationId/withdraw") {
                    contentType(ContentType.Application.Json)
                }
            ApiResponse.Success(response.body())
        } catch (e: Exception) {
            ApiResponse.Error(e.message ?: "Failed to withdraw application", e)
        }
}

// ========== API RESPONSE MODELS ==========
@kotlinx.serialization.Serializable
data class CashLoanApplicationResponse(
    val applicationId: String,
    val status: String,
    val message: String,
    val estimatedReviewTime: String,
)

@kotlinx.serialization.Serializable
data class PayGoCategoriesResponse(
    val categories: List<String>,
)

@kotlinx.serialization.Serializable
data class PayGoProductsResponse(
    val products: List<PayGoProduct>,
)

@kotlinx.serialization.Serializable
data class PayGoApplicationResponse(
    val applicationId: String,
    val status: String,
    val message: String,
    val estimatedReviewTime: String,
)

@kotlinx.serialization.Serializable
data class CurrentLoansResponse(
    val loans: List<Loan>,
)

@kotlinx.serialization.Serializable
data class WithdrawApplicationResponse(
    val message: String,
    val success: Boolean,
)

// ========== API RESPONSE WRAPPER ==========
sealed class ApiResponse<out T> {
    data class Success<T>(
        val data: T,
    ) : ApiResponse<T>()

    data class Error(
        val message: String,
        val exception: Throwable? = null,
    ) : ApiResponse<Nothing>()

    fun isSuccess(): Boolean = this is Success

    fun isError(): Boolean = this is Error

    fun getOrNull(): T? = if (this is Success) data else null

    fun getErrorOrNull(): String? = if (this is Error) message else null
}
