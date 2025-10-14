package com.soshopay.domain.repository

import com.soshopay.domain.model.ApplicationStatus
import com.soshopay.domain.model.CashLoanApplication
import com.soshopay.domain.model.CashLoanCalculationRequest
import com.soshopay.domain.model.CashLoanFormData
import com.soshopay.domain.model.CashLoanTerms
import com.soshopay.domain.model.CollateralDocument
import com.soshopay.domain.model.Loan
import com.soshopay.domain.model.LoanDetails
import com.soshopay.domain.model.LoanHistoryResponse
import com.soshopay.domain.model.PayGoCalculationRequest
import com.soshopay.domain.model.PayGoLoanApplication
import com.soshopay.domain.model.PayGoLoanTerms
import com.soshopay.domain.model.PayGoProduct
import com.soshopay.domain.model.ValidationResult
import kotlinx.coroutines.flow.Flow

/**
 * Repository abstraction for loan-related operations in the SoshoPay domain.
 *
 * Implementations of this interface handle all loan workflows, including cash loans and PayGo loans,
 * as well as general loan management, local caching, and validation. All operations return a [Result]
 * type for robust error handling and success/failure reporting, or a [Flow] for reactive updates.
 *
 * **Cash Loan Methods:**
 * - Retrieve form data, calculate terms, submit applications, manage drafts.
 *
 * **PayGo Loan Methods:**
 * - Retrieve categories/products, calculate terms, submit applications, manage drafts.
 *
 * **General Loan Methods:**
 * - Fetch loan history, details, current loans, download agreements, withdraw applications, sync with remote.
 *
 * **Local/Cache Methods:**
 * - Observe loan updates and application status, retrieve cached loans and details.
 *
 * **Validation Methods:**
 * - Validate cash loan and PayGo loan applications before submission.
 *
 * All suspend functions support asynchronous/coroutine-based execution.
 */
interface LoanRepository {
    // ========== CASH LOAN METHODS ==========
    /**
     * Retrieves the form data required for a cash loan application.
     * @return [Result] containing [CashLoanFormData] if successful, or an error.
     */
    suspend fun getCashLoanFormData(): Result<CashLoanFormData>

    /**
     * Calculates the terms for a cash loan based on the provided [request].
     * @param request The calculation request parameters.
     * @return [Result] containing [CashLoanTerms] if successful, or an error.
     */
    suspend fun calculateCashLoanTerms(request: CashLoanCalculationRequest): Result<CashLoanTerms>

    /**
     * Submits a cash loan application.
     * @param application The cash loan application data.
     * @return [Result] containing the application ID if successful, or an error.
     */
    suspend fun submitCashLoanApplication(application: CashLoanApplication): Result<String>

    /**
     * Uploads a collateral document for a cash loan application.
     *
     * @param fileBytes The binary content of the file
     * @param fileName The name of the file including extension
     * @param fileType The MIME type of the file
     * @param applicationId The application ID this document belongs to
     * @return [Result] containing [CollateralDocument] if successful, or an error
     */
    suspend fun uploadCollateralDocument(
        fileBytes: ByteArray,
        fileName: String,
        fileType: String,
        applicationId: String,
    ): Result<CollateralDocument>

    /**
     * Saves a draft of the cash loan application locally.
     * @param application The cash loan application data.
     * @return [Result] containing [Unit] if successful, or an error.
     */
    suspend fun saveDraftCashLoanApplication(application: CashLoanApplication): Result<Unit>

    /**
     * Retrieves a draft cash loan application for the specified [userId].
     * @param userId The user ID.
     * @return [Result] containing the draft [CashLoanApplication] if found, or null if not present.
     */
    suspend fun getDraftCashLoanApplication(userId: String): Result<CashLoanApplication?>

    /**
     * Deletes a draft cash loan application by [applicationId].
     * @param applicationId The ID of the draft application to delete.
     * @return [Result] containing [Unit] if successful, or an error.
     */
    suspend fun deleteDraftCashLoanApplication(applicationId: String): Result<Unit>

    // ========== PAYGO LOAN METHODS ==========

    /**
     * Retrieves the list of PayGo loan categories.
     * @return [Result] containing a list of category names if successful, or an error.
     */
    suspend fun getPayGoCategories(): Result<List<String>>

    /**
     * Retrieves the products for a given PayGo category.
     * @param categoryId The category ID.
     * @return [Result] containing a list of [PayGoProduct] if successful, or an error.
     */
    suspend fun getCategoryProducts(categoryId: String): Result<List<PayGoProduct>>

    /**
     * Calculates the terms for a PayGo loan based on the provided [request].
     * @param request The calculation request parameters.
     * @return [Result] containing [PayGoLoanTerms] if successful, or an error.
     */
    suspend fun calculatePayGoTerms(request: PayGoCalculationRequest): Result<PayGoLoanTerms>

    /**
     * Submits a PayGo loan application.
     * @param application The PayGo loan application data.
     * @return [Result] containing the application ID if successful, or an error.
     */
    suspend fun submitPayGoApplication(application: PayGoLoanApplication): Result<String>

    /**
     * Saves a draft of the PayGo loan application locally.
     * @param application The PayGo loan application data.
     * @return [Result] containing [Unit] if successful, or an error.
     */
    suspend fun saveDraftPayGoApplication(application: PayGoLoanApplication): Result<Unit>

    /**
     * Retrieves a draft PayGo loan application for the specified [userId].
     * @param userId The user ID.
     * @return [Result] containing the draft [PayGoLoanApplication] if found, or null if not present.
     */
    suspend fun getDraftPayGoApplication(userId: String): Result<PayGoLoanApplication?>

    /**
     * Deletes a draft PayGo loan application by [applicationId].
     * @param applicationId The ID of the draft application to delete.
     * @return [Result] containing [Unit] if successful, or an error.
     */
    suspend fun deleteDraftPayGoApplication(applicationId: String): Result<Unit>

    // ========== GENERAL LOAN METHODS ==========

    /**
     * Retrieves the user's loan history with optional filtering and pagination.
     * @param filter The filter for loan history (default: "all").
     * @param page The page number (default: 1).
     * @param limit The number of items per page (default: 20).
     * @return [Result] containing [LoanHistoryResponse] if successful, or an error.
     */
    suspend fun getLoanHistory(
        filter: String = "all",
        page: Int = 1,
        limit: Int = 20,
    ): Result<LoanHistoryResponse>

    /**
     * Retrieves the details for a specific loan by [loanId].
     * @param loanId The loan ID.
     * @return [Result] containing [LoanDetails] if successful, or an error.
     */
    suspend fun getLoanDetails(loanId: String): Result<LoanDetails>

    /**
     * Retrieves the user's current active loans.
     * @return [Result] containing a list of [Loan] if successful, or an error.
     */
    suspend fun getCurrentLoans(): Result<List<Loan>>

    /**
     * Downloads the loan agreement document for the specified [loanId].
     * @param loanId The loan ID.
     * @return [Result] containing the agreement as a [ByteArray] if successful, or an error.
     */
    suspend fun downloadLoanAgreement(loanId: String): Result<ByteArray>

    /**
     * Withdraws a loan application by [applicationId].
     * @param applicationId The application ID to withdraw.
     * @return [Result] containing [Unit] if successful, or an error.
     */
    suspend fun withdrawApplication(applicationId: String): Result<Unit>

    /**
     * Synchronizes loans from the remote server to local storage.
     * @return [Result] containing [Unit] if successful, or an error.
     */
    suspend fun syncLoansFromRemote(): Result<Unit>

    // ========== LOCAL/CACHE METHODS ==========

    /**
     * Observes updates to the user's loans as a [Flow].
     * @return A [Flow] emitting the current list of [Loan] whenever updates occur.
     */
    fun observeLoanUpdates(): Flow<List<Loan>>

    /**
     * Observes the status of a loan application by [applicationId] as a [Flow].
     * @param applicationId The application ID.
     * @return A [Flow] emitting the current [ApplicationStatus] for the application.
     */
    fun observeApplicationStatus(applicationId: String): Flow<ApplicationStatus>

    /**
     * Retrieves the cached loans from local storage.
     * @return [Result] containing a list of [Loan] if successful, or an error.
     */
    suspend fun getCachedLoans(): Result<List<Loan>>

    /**
     * Retrieves the cached details for a specific loan by [loanId].
     * @param loanId The loan ID.
     * @return [Result] containing [LoanDetails] if found, or null if not present.
     */
    suspend fun getCachedLoanDetails(loanId: String): Result<LoanDetails?>

    // ========== VALIDATION METHODS ==========

    /**
     * Validates a cash loan application before submission.
     * @param application The cash loan application data.
     * @return [ValidationResult] indicating validity, errors, and warnings.
     */
    fun validateCashLoanApplication(application: CashLoanApplication): ValidationResult

    /**
     * Validates a PayGo loan application before submission.
     * @param application The PayGo loan application data.
     * @return [ValidationResult] indicating validity, errors, and warnings.
     */
    fun validatePayGoApplication(application: PayGoLoanApplication): ValidationResult
}
