package com.soshopay.domain.repository

import com.soshopay.domain.model.ApplicationStatus
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
import com.soshopay.domain.model.ValidationResult
import kotlinx.coroutines.flow.Flow

interface LoanRepository {
    // ========== CASH LOAN METHODS ==========
    suspend fun getCashLoanFormData(): Result<CashLoanFormData>

    suspend fun calculateCashLoanTerms(request: CashLoanCalculationRequest): Result<CashLoanTerms>

    suspend fun submitCashLoanApplication(application: CashLoanApplication): Result<String>

    suspend fun saveDraftCashLoanApplication(application: CashLoanApplication): Result<Unit>

    suspend fun getDraftCashLoanApplication(userId: String): Result<CashLoanApplication?>

    suspend fun deleteDraftCashLoanApplication(applicationId: String): Result<Unit>

    // ========== PAYGO LOAN METHODS ==========
    suspend fun getPayGoCategories(): Result<List<String>>

    suspend fun getCategoryProducts(categoryId: String): Result<List<PayGoProduct>>

    suspend fun calculatePayGoTerms(request: PayGoCalculationRequest): Result<PayGoLoanTerms>

    suspend fun submitPayGoApplication(application: PayGoLoanApplication): Result<String>

    suspend fun saveDraftPayGoApplication(application: PayGoLoanApplication): Result<Unit>

    suspend fun getDraftPayGoApplication(userId: String): Result<PayGoLoanApplication?>

    suspend fun deleteDraftPayGoApplication(applicationId: String): Result<Unit>

    // ========== GENERAL LOAN METHODS ==========
    suspend fun getLoanHistory(
        filter: String = "all",
        page: Int = 1,
        limit: Int = 20,
    ): Result<LoanHistoryResponse>

    suspend fun getLoanDetails(loanId: String): Result<LoanDetails>

    suspend fun getCurrentLoans(): Result<List<Loan>>

    suspend fun downloadLoanAgreement(loanId: String): Result<ByteArray>

    suspend fun withdrawApplication(applicationId: String): Result<Unit>

    suspend fun syncLoansFromRemote(): Result<Unit>

    // ========== LOCAL/CACHE METHODS ==========
    fun observeLoanUpdates(): Flow<List<Loan>>

    fun observeApplicationStatus(applicationId: String): Flow<ApplicationStatus>

    suspend fun getCachedLoans(): Result<List<Loan>>

    suspend fun getCachedLoanDetails(loanId: String): Result<LoanDetails?>

    // ========== VALIDATION METHODS ==========
    fun validateCashLoanApplication(application: CashLoanApplication): ValidationResult

    fun validatePayGoApplication(application: PayGoLoanApplication): ValidationResult
}
