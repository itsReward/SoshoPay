package com.soshopay.data.repository

import com.soshopay.data.local.CacheManager
import com.soshopay.data.local.LocalLoanStorage
import com.soshopay.data.remote.LoanApiService
import com.soshopay.domain.model.*
import com.soshopay.domain.repository.LoanRepository
import com.soshopay.domain.repository.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock

class LoanRepositoryImpl(
    private val loanApiService: LoanApiService,
    private val localStorage: LocalLoanStorage,
    private val cacheManager: CacheManager,
) : LoanRepository {
    // ========== CASH LOAN METHODS ==========
    override suspend fun getCashLoanFormData(): Result<CashLoanFormData> {
        return try {
            // Check cache first
            val cachedData = localStorage.getCashLoanFormData()
            val shouldSync = localStorage.shouldSync("form_data", CacheManager.SYNC_INTERVAL_FORM_DATA)

            if (cachedData != null && !shouldSync) {
                return Result.Success(cachedData)
            }

            // Fetch from API
            val apiResponse = loanApiService.getCashLoanFormData()
            if (apiResponse.isSuccess()) {
                val formData = apiResponse.getOrNull()!!
                localStorage.saveCashLoanFormData(formData)
                localStorage.updateLastSyncTime("form_data", Clock.System.now().toEpochMilliseconds())
                Result.Success(formData)
            } else {
                // Return cached data if API fails and cache exists
                cachedData?.let { Result.Success(it) }
                    ?: Result.Error(Exception(apiResponse.getErrorOrNull() ?: "Failed to get form data"))
            }
        } catch (e: Exception) {
            // Try to return cached data on exception
            val cachedData = localStorage.getCashLoanFormData()
            cachedData?.let { Result.Success(it) } ?: Result.Error(e)
        }
    }

    override suspend fun calculateCashLoanTerms(request: CashLoanCalculationRequest): Result<CashLoanTerms> =
        try {
            val apiResponse = loanApiService.calculateCashLoanTerms(request)
            if (apiResponse.isSuccess()) {
                Result.Success(apiResponse.getOrNull()!!)
            } else {
                Result.Error(Exception(apiResponse.getErrorOrNull() ?: "Failed to calculate terms"))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }

    override suspend fun submitCashLoanApplication(application: CashLoanApplication): Result<String> {
        return try {
            // Validate application first
            val validation = validateCashLoanApplication(application)
            if (!validation.isValid) {
                return Result.Error(Exception(validation.getErrorMessage() ?: "Invalid application"))
            }

            val apiResponse = loanApiService.submitCashLoanApplication(application)
            if (apiResponse.isSuccess()) {
                val response = apiResponse.getOrNull()!!
                // Clear draft after successful submission
                deleteDraftCashLoanApplication(application.id)
                Result.Success(response.applicationId)
            } else {
                Result.Error(Exception(apiResponse.getErrorOrNull() ?: "Failed to submit application"))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun saveDraftCashLoanApplication(application: CashLoanApplication): Result<Unit> =
        try {
            localStorage.saveDraftCashLoanApplication(application)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }

    override suspend fun getDraftCashLoanApplication(userId: String): Result<CashLoanApplication?> =
        try {
            val draft = localStorage.getDraftCashLoanApplication(userId)
            Result.Success(draft)
        } catch (e: Exception) {
            Result.Error(e)
        }

    override suspend fun deleteDraftCashLoanApplication(applicationId: String): Result<Unit> =
        try {
            localStorage.deleteDraftCashLoanApplication(applicationId)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }

    // ========== PAYGO LOAN METHODS ==========
    override suspend fun getPayGoCategories(): Result<List<String>> {
        return try {
            // Check cache first
            val cachedCategories = localStorage.getPayGoCategories()
            val shouldSync = localStorage.shouldSync("categories", CacheManager.SYNC_INTERVAL_FORM_DATA)

            if (cachedCategories.isNotEmpty() && !shouldSync) {
                return Result.Success(cachedCategories)
            }

            // Fetch from API
            val apiResponse = loanApiService.getPayGoCategories()
            if (apiResponse.isSuccess()) {
                val response = apiResponse.getOrNull()!!
                localStorage.savePayGoCategories(response.categories)
                localStorage.updateLastSyncTime("categories", Clock.System.now().toEpochMilliseconds())
                Result.Success(response.categories)
            } else {
                // Return cached data if API fails and cache exists
                if (cachedCategories.isNotEmpty()) {
                    Result.Success(cachedCategories)
                } else {
                    Result.Error(Exception(apiResponse.getErrorOrNull() ?: "Failed to get categories"))
                }
            }
        } catch (e: Exception) {
            // Try to return cached data on exception
            val cachedCategories = localStorage.getPayGoCategories()
            if (cachedCategories.isNotEmpty()) {
                Result.Success(cachedCategories)
            } else {
                Result.Error(e)
            }
        }
    }

    override suspend fun getCategoryProducts(categoryId: String): Result<List<PayGoProduct>> {
        return try {
            // Check cache first
            val cachedProducts = localStorage.getPayGoProductsByCategory(categoryId)
            val shouldSync = localStorage.shouldSync("products_$categoryId", CacheManager.SYNC_INTERVAL_FORM_DATA)

            if (cachedProducts.isNotEmpty() && !shouldSync) {
                return Result.Success(cachedProducts)
            }

            // Fetch from API
            val apiResponse = loanApiService.getCategoryProducts(categoryId)
            if (apiResponse.isSuccess()) {
                val response = apiResponse.getOrNull()!!
                localStorage.insertPayGoProducts(response.products)
                localStorage.updateLastSyncTime("products_$categoryId", Clock.System.now().toEpochMilliseconds())
                Result.Success(response.products)
            } else {
                // Return cached data if API fails and cache exists
                if (cachedProducts.isNotEmpty()) {
                    Result.Success(cachedProducts)
                } else {
                    Result.Error(Exception(apiResponse.getErrorOrNull() ?: "Failed to get products"))
                }
            }
        } catch (e: Exception) {
            // Try to return cached data on exception
            val cachedProducts = localStorage.getPayGoProductsByCategory(categoryId)
            if (cachedProducts.isNotEmpty()) {
                Result.Success(cachedProducts)
            } else {
                Result.Error(e)
            }
        }
    }

    override suspend fun calculatePayGoTerms(request: PayGoCalculationRequest): Result<PayGoLoanTerms> =
        try {
            val apiResponse = loanApiService.calculatePayGoTerms(request)
            if (apiResponse.isSuccess()) {
                Result.Success(apiResponse.getOrNull()!!)
            } else {
                Result.Error(Exception(apiResponse.getErrorOrNull() ?: "Failed to calculate PayGo terms"))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }

    override suspend fun submitPayGoApplication(application: PayGoLoanApplication): Result<String> {
        return try {
            // Validate application first
            val validation = validatePayGoApplication(application)
            if (!validation.isValid) {
                return Result.Error(Exception(validation.getErrorMessage() ?: "Invalid application"))
            }

            val apiResponse = loanApiService.submitPayGoApplication(application)
            if (apiResponse.isSuccess()) {
                val response = apiResponse.getOrNull()!!
                // Clear draft after successful submission
                deleteDraftPayGoApplication(application.id)
                Result.Success(response.applicationId)
            } else {
                Result.Error(Exception(apiResponse.getErrorOrNull() ?: "Failed to submit PayGo application"))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun saveDraftPayGoApplication(application: PayGoLoanApplication): Result<Unit> =
        try {
            localStorage.saveDraftPayGoApplication(application)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }

    override suspend fun getDraftPayGoApplication(userId: String): Result<PayGoLoanApplication?> =
        try {
            val draft = localStorage.getDraftPayGoApplication(userId)
            Result.Success(draft)
        } catch (e: Exception) {
            Result.Error(e)
        }

    override suspend fun deleteDraftPayGoApplication(applicationId: String): Result<Unit> =
        try {
            localStorage.deleteDraftPayGoApplication(applicationId)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }

    // ========== GENERAL LOAN METHODS ==========
    override suspend fun getLoanHistory(
        filter: String,
        page: Int,
        limit: Int,
    ): Result<LoanHistoryResponse> =
        try {
            val apiResponse = loanApiService.getLoanHistory(filter, page, limit)
            if (apiResponse.isSuccess()) {
                val response = apiResponse.getOrNull()!!
                // Cache the loans for offline access
                localStorage.insertLoans(response.loans)
                localStorage.updateLastSyncTime(CacheManager.CACHE_LOANS, Clock.System.now().toEpochMilliseconds())
                Result.Success(response)
            } else {
                // Try to return cached data if API fails
                if (page == 1) {
                    val cachedLoans = localStorage.getAllLoans()
                    if (cachedLoans.isNotEmpty()) {
                        val cachedResponse =
                            LoanHistoryResponse(
                                loans = cachedLoans,
                                currentPage = 1,
                                totalPages = 1,
                                totalCount = cachedLoans.size,
                                hasNext = false,
                                hasPrevious = false,
                            )
                        Result.Success(cachedResponse)
                    } else {
                        Result.Error(Exception(apiResponse.getErrorOrNull() ?: "Failed to get loan history"))
                    }
                } else {
                    Result.Error(Exception(apiResponse.getErrorOrNull() ?: "Failed to get loan history"))
                }
            }
        } catch (e: Exception) {
            // Try cached data on exception
            if (page == 1) {
                val cachedLoans = localStorage.getAllLoans()
                if (cachedLoans.isNotEmpty()) {
                    val cachedResponse =
                        LoanHistoryResponse(
                            loans = cachedLoans,
                            currentPage = 1,
                            totalPages = 1,
                            totalCount = cachedLoans.size,
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

    override suspend fun getLoanDetails(loanId: String): Result<LoanDetails> {
        return try {
            // Check cache first
            val cachedDetails = localStorage.getLoanDetails(loanId)
            val shouldSync = localStorage.shouldSync("loan_details_$loanId", CacheManager.SYNC_INTERVAL_LOANS)

            if (cachedDetails != null && !shouldSync) {
                return Result.Success(cachedDetails)
            }

            // Fetch from API
            val apiResponse = loanApiService.getLoanDetails(loanId)
            if (apiResponse.isSuccess()) {
                val loanDetails = apiResponse.getOrNull()!!
                localStorage.insertLoanDetails(loanDetails)
                localStorage.updateLastSyncTime("loan_details_$loanId", Clock.System.now().toEpochMilliseconds())
                Result.Success(loanDetails)
            } else {
                // Return cached data if API fails and cache exists
                cachedDetails?.let { Result.Success(it) }
                    ?: Result.Error(Exception(apiResponse.getErrorOrNull() ?: "Failed to get loan details"))
            }
        } catch (e: Exception) {
            // Try to return cached data on exception
            val cachedDetails = localStorage.getLoanDetails(loanId)
            cachedDetails?.let { Result.Success(it) } ?: Result.Error(e)
        }
    }

    override suspend fun getCurrentLoans(): Result<List<Loan>> =
        try {
            val apiResponse = loanApiService.getCurrentLoans()
            if (apiResponse.isSuccess()) {
                val response = apiResponse.getOrNull()!!
                localStorage.insertLoans(response.loans)
                localStorage.updateLastSyncTime("current_loans", Clock.System.now().toEpochMilliseconds())
                Result.Success(response.loans)
            } else {
                // Try to return cached active loans
                val cachedLoans = localStorage.getAllLoans().filter { it.isActive() }
                if (cachedLoans.isNotEmpty()) {
                    Result.Success(cachedLoans)
                } else {
                    Result.Error(Exception(apiResponse.getErrorOrNull() ?: "Failed to get current loans"))
                }
            }
        } catch (e: Exception) {
            // Try cached data on exception
            val cachedLoans = localStorage.getAllLoans().filter { it.isActive() }
            if (cachedLoans.isNotEmpty()) {
                Result.Success(cachedLoans)
            } else {
                Result.Error(e)
            }
        }

    override suspend fun downloadLoanAgreement(loanId: String): Result<ByteArray> =
        try {
            val apiResponse = loanApiService.downloadLoanAgreement(loanId)
            if (apiResponse.isSuccess()) {
                Result.Success(apiResponse.getOrNull()!!)
            } else {
                Result.Error(Exception(apiResponse.getErrorOrNull() ?: "Failed to download loan agreement"))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }

    override suspend fun withdrawApplication(applicationId: String): Result<Unit> =
        try {
            val apiResponse = loanApiService.withdrawApplication(applicationId)
            if (apiResponse.isSuccess()) {
                // Refresh loan data after withdrawal
                syncLoansFromRemote()
                Result.Success(Unit)
            } else {
                Result.Error(Exception(apiResponse.getErrorOrNull() ?: "Failed to withdraw application"))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }

    override suspend fun syncLoansFromRemote(): Result<Unit> =
        try {
            val apiResponse = loanApiService.getLoanHistory()
            if (apiResponse.isSuccess()) {
                val response = apiResponse.getOrNull()!!
                localStorage.insertLoans(response.loans)
                localStorage.updateLastSyncTime(CacheManager.CACHE_LOANS, Clock.System.now().toEpochMilliseconds())
                Result.Success(Unit)
            } else {
                Result.Error(Exception(apiResponse.getErrorOrNull() ?: "Failed to sync loans"))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }

    // ========== LOCAL/CACHE METHODS ==========
    override fun observeLoanUpdates(): Flow<List<Loan>> = localStorage.observeLoans()

    override fun observeApplicationStatus(applicationId: String): Flow<ApplicationStatus> =
        flow {
            // This would require a more complex implementation with periodic API checks
            // For now, we'll emit the current status from cache
            val loans = localStorage.getAllLoans()
            val loan = loans.find { it.applicationId == applicationId }
            loan?.let {
                // Map LoanStatus to ApplicationStatus
                val appStatus =
                    when (it.status) {
                        LoanStatus.PENDING_DISBURSEMENT -> ApplicationStatus.APPROVED
                        LoanStatus.ACTIVE -> ApplicationStatus.APPROVED
                        LoanStatus.COMPLETED -> ApplicationStatus.APPROVED
                        LoanStatus.DEFAULTED -> ApplicationStatus.APPROVED
                        LoanStatus.CANCELLED -> ApplicationStatus.CANCELLED
                    }
                emit(appStatus)
            }
        }

    override suspend fun getCachedLoans(): Result<List<Loan>> =
        try {
            val loans = localStorage.getAllLoans()
            Result.Success(loans)
        } catch (e: Exception) {
            Result.Error(e)
        }

    override suspend fun getCachedLoanDetails(loanId: String): Result<LoanDetails?> =
        try {
            val details = localStorage.getLoanDetails(loanId)
            Result.Success(details)
        } catch (e: Exception) {
            Result.Error(e)
        }

    // ========== VALIDATION METHODS ==========
    override fun validateCashLoanApplication(application: CashLoanApplication): ValidationResult {
        val errors = mutableListOf<String>()
        val warnings = mutableListOf<String>()

        // Amount validation
        if (application.loanAmount <= 0) {
            errors.add("Loan amount must be greater than zero")
        }

        // Repayment period validation
        if (application.repaymentPeriod.isBlank()) {
            errors.add("Repayment period is required")
        }

        // Loan purpose validation
        if (application.loanPurpose.isBlank()) {
            errors.add("Loan purpose is required")
        }

        // Employer industry validation
        if (application.employerIndustry.isBlank()) {
            errors.add("Employer industry is required")
        }

        // Collateral validation
        if (application.collateralValue <= 0) {
            errors.add("Collateral value must be greater than zero")
        }

        if (application.collateralDetails.isBlank()) {
            errors.add("Collateral details are required")
        }

        // Terms validation
        if (!application.acceptedTerms) {
            errors.add("You must accept the loan terms to proceed")
        }

        // Warnings
        if (application.collateralValue < application.loanAmount) {
            warnings.add("Collateral value is less than loan amount")
        }

        return ValidationResult(
            isValid = errors.isEmpty(),
            errors = errors,
            warnings = warnings,
        )
    }

    override fun validatePayGoApplication(application: PayGoLoanApplication): ValidationResult {
        val errors = mutableListOf<String>()
        val warnings = mutableListOf<String>()

        // Product validation
        if (application.productId.isBlank()) {
            errors.add("Product selection is required")
        }

        // Usage validation
        if (application.usagePerDay.isBlank()) {
            errors.add("Usage per day is required")
        }

        // Repayment period validation
        if (application.repaymentPeriod.isBlank()) {
            errors.add("Repayment period is required")
        }

        // Salary band validation
        if (application.salaryBand.isBlank()) {
            errors.add("Salary band is required")
        }

        // Guarantor validation
        if (!application.guarantor.isComplete()) {
            errors.add("Complete guarantor information is required")
        }

        // Terms validation
        if (!application.acceptedTerms) {
            errors.add("You must accept the loan terms to proceed")
        }

        return ValidationResult(
            isValid = errors.isEmpty(),
            errors = errors,
            warnings = warnings,
        )
    }
}
