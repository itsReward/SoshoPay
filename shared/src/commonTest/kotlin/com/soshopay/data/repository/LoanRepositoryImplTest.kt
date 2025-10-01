package com.soshopay.data.repository

import com.soshopay.data.local.CacheManager
import com.soshopay.data.local.LocalLoanStorage
import com.soshopay.data.remote.LoanApiService
import com.soshopay.domain.model.*
import com.soshopay.domain.repository.Result
import io.mockk.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Clock
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class LoanRepositoryImplTest {
    private lateinit var repository: LoanRepositoryImpl
    private lateinit var loanApiService: LoanApiService
    private lateinit var localStorage: LocalLoanStorage
    private lateinit var cacheManager: CacheManager

    @Before
    fun setup() {
        loanApiService = mockk()
        localStorage = mockk()
        cacheManager = mockk()
        repository = LoanRepositoryImpl(loanApiService, localStorage, cacheManager)
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    // ========== CASH LOAN FORM DATA TESTS ==========

    @Test
    fun `getCashLoanFormData returns cached data when valid and not expired`() =
        runBlocking {
            // Given
            val cachedFormData =
                CashLoanFormData(
                    repaymentPeriods = listOf("3 months", "6 months"),
                    loanPurposes = listOf("Business", "Personal"),
                    industries = listOf("Technology", "Finance"),
                )
            coEvery { localStorage.getCashLoanFormData() } returns cachedFormData
            coEvery { localStorage.shouldSync("form_data", CacheManager.SYNC_INTERVAL_FORM_DATA) } returns false

            // When
            val result = repository.getCashLoanFormData()

            // Then
            assertTrue(result is Result.Success)
            assertEquals(cachedFormData, (result as Result.Success).data)
            coVerify(exactly = 0) { loanApiService.getCashLoanFormData() }
        }

    @Test
    fun `getCashLoanFormData fetches from API when cache expired`() =
        runBlocking {
            // Given
            val apiFormData =
                CashLoanFormData(
                    repaymentPeriods = listOf("3 months", "6 months"),
                    loanPurposes = listOf("Business", "Personal"),
                    industries = listOf("Technology", "Finance"),
                )
            val apiResponse = mockk<com.soshopay.data.remote.ApiResponse<CashLoanFormData>>()

            coEvery { localStorage.getCashLoanFormData() } returns null
            coEvery { localStorage.shouldSync("form_data", CacheManager.SYNC_INTERVAL_FORM_DATA) } returns true
            every { apiResponse.isSuccess() } returns true
            every { apiResponse.getOrNull() } returns apiFormData
            coEvery { loanApiService.getCashLoanFormData() } returns apiResponse
            coEvery { localStorage.saveCashLoanFormData(apiFormData) } just Runs
            coEvery { localStorage.updateLastSyncTime("form_data", any()) } just Runs

            // When
            val result = repository.getCashLoanFormData()

            // Then
            assertTrue(result is Result.Success)
            assertEquals(apiFormData, (result as Result.Success).data)
            coVerify { localStorage.saveCashLoanFormData(apiFormData) }
            coVerify { localStorage.updateLastSyncTime("form_data", any()) }
        }

    @Test
    fun `getCashLoanFormData returns cached data when API fails`() =
        runBlocking {
            // Given
            val cachedFormData =
                CashLoanFormData(
                    repaymentPeriods = listOf("3 months"),
                    loanPurposes = listOf("Business"),
                    industries = listOf("Technology"),
                )
            val apiResponse = mockk<com.soshopay.data.remote.ApiResponse<CashLoanFormData>>()

            coEvery { localStorage.getCashLoanFormData() } returns cachedFormData
            coEvery { localStorage.shouldSync("form_data", CacheManager.SYNC_INTERVAL_FORM_DATA) } returns true
            every { apiResponse.isSuccess() } returns false
            every { apiResponse.getErrorOrNull() } returns "Network error"
            coEvery { loanApiService.getCashLoanFormData() } returns apiResponse

            // When
            val result = repository.getCashLoanFormData()

            // Then
            assertTrue(result is Result.Success)
            assertEquals(cachedFormData, (result as Result.Success).data)
        }

    @Test
    fun `getCashLoanFormData returns error when API fails and no cache`() =
        runBlocking {
            // Given
            val apiResponse = mockk<com.soshopay.data.remote.ApiResponse<CashLoanFormData>>()

            coEvery { localStorage.getCashLoanFormData() } returns null
            coEvery { localStorage.shouldSync("form_data", CacheManager.SYNC_INTERVAL_FORM_DATA) } returns true
            every { apiResponse.isSuccess() } returns false
            every { apiResponse.getErrorOrNull() } returns "Network error"
            coEvery { loanApiService.getCashLoanFormData() } returns apiResponse

            // When
            val result = repository.getCashLoanFormData()

            // Then
            assertTrue(result is Result.Error)
        }

    // ========== CASH LOAN CALCULATION TESTS ==========

    @Test
    fun `calculateCashLoanTerms returns terms on success`() =
        runBlocking {
            // Given
            val request = CashLoanCalculationRequest(loanAmount = 10000.0, repaymentPeriod = "6 months")
            val terms =
                CashLoanTerms(
                    loanAmount = 10000.0,
                    interestRate = 15.0,
                    totalRepayment = 11500.0,
                    monthlyPayment = 1916.67,
                )
            val apiResponse = mockk<com.soshopay.data.remote.ApiResponse<CashLoanTerms>>()

            every { apiResponse.isSuccess() } returns true
            every { apiResponse.getOrNull() } returns terms
            coEvery { loanApiService.calculateCashLoanTerms(request) } returns apiResponse

            // When
            val result = repository.calculateCashLoanTerms(request)

            // Then
            assertTrue(result is Result.Success)
            assertEquals(terms, (result as Result.Success).data)
        }

    @Test
    fun `calculateCashLoanTerms returns error on API failure`() =
        runBlocking {
            // Given
            val request = CashLoanCalculationRequest(loanAmount = 10000.0, repaymentPeriod = "6 months")
            val apiResponse = mockk<com.soshopay.data.remote.ApiResponse<CashLoanTerms>>()

            every { apiResponse.isSuccess() } returns false
            every { apiResponse.getErrorOrNull() } returns "Calculation failed"
            coEvery { loanApiService.calculateCashLoanTerms(request) } returns apiResponse

            // When
            val result = repository.calculateCashLoanTerms(request)

            // Then
            assertTrue(result is Result.Error)
        }

    // ========== CASH LOAN APPLICATION TESTS ==========

    @Test
    fun `submitCashLoanApplication succeeds with valid application`() =
        runBlocking {
            // Given
            val application =
                CashLoanApplication(
                    id = "app-123",
                    loanAmount = 10000.0,
                    repaymentPeriod = "6 months",
                    loanPurpose = "Business",
                    employerIndustry = "Technology",
                    collateralValue = 12000.0,
                    collateralDetails = "Car",
                    acceptedTerms = true,
                )
            val apiResponse = mockk<com.soshopay.data.remote.ApiResponse<CashLoanApplicationResponse>>()
            val response = CashLoanApplicationResponse(applicationId = "app-123")

            every { apiResponse.isSuccess() } returns true
            every { apiResponse.getOrNull() } returns response
            coEvery { loanApiService.submitCashLoanApplication(application) } returns apiResponse
            coEvery { localStorage.deleteDraftCashLoanApplication("app-123") } just Runs

            // When
            val result = repository.submitCashLoanApplication(application)

            // Then
            assertTrue(result is Result.Success)
            assertEquals("app-123", (result as Result.Success).data)
            coVerify { localStorage.deleteDraftCashLoanApplication("app-123") }
        }

    @Test
    fun `submitCashLoanApplication fails validation with invalid application`() =
        runBlocking {
            // Given
            val application =
                CashLoanApplication(
                    id = "app-123",
                    loanAmount = -1000.0, // Invalid amount
                    repaymentPeriod = "",
                    loanPurpose = "",
                    employerIndustry = "",
                    collateralValue = 0.0,
                    collateralDetails = "",
                    acceptedTerms = false,
                )

            // When
            val result = repository.submitCashLoanApplication(application)

            // Then
            assertTrue(result is Result.Error)
            coVerify(exactly = 0) { loanApiService.submitCashLoanApplication(any()) }
        }

    @Test
    fun `saveDraftCashLoanApplication saves successfully`() =
        runBlocking {
            // Given
            val application =
                CashLoanApplication(
                    id = "draft-123",
                    loanAmount = 5000.0,
                    repaymentPeriod = "3 months",
                    loanPurpose = "Personal",
                    employerIndustry = "Finance",
                    collateralValue = 6000.0,
                    collateralDetails = "Jewelry",
                    acceptedTerms = false,
                )

            coEvery { localStorage.saveDraftCashLoanApplication(application) } just Runs

            // When
            val result = repository.saveDraftCashLoanApplication(application)

            // Then
            assertTrue(result is Result.Success)
            coVerify { localStorage.saveDraftCashLoanApplication(application) }
        }

    @Test
    fun `getDraftCashLoanApplication returns draft when exists`() =
        runBlocking {
            // Given
            val userId = "user-123"
            val draft =
                CashLoanApplication(
                    id = "draft-123",
                    loanAmount = 5000.0,
                    repaymentPeriod = "3 months",
                    loanPurpose = "Personal",
                    employerIndustry = "Finance",
                    collateralValue = 6000.0,
                    collateralDetails = "Jewelry",
                    acceptedTerms = false,
                )

            coEvery { localStorage.getDraftCashLoanApplication(userId) } returns draft

            // When
            val result = repository.getDraftCashLoanApplication(userId)

            // Then
            assertTrue(result is Result.Success)
            assertEquals(draft, (result as Result.Success).data)
        }

    @Test
    fun `deleteDraftCashLoanApplication deletes successfully`() =
        runBlocking {
            // Given
            val applicationId = "draft-123"
            coEvery { localStorage.deleteDraftCashLoanApplication(applicationId) } just Runs

            // When
            val result = repository.deleteDraftCashLoanApplication(applicationId)

            // Then
            assertTrue(result is Result.Success)
            coVerify { localStorage.deleteDraftCashLoanApplication(applicationId) }
        }

    // ========== PAYGO TESTS ==========

    @Test
    fun `getPayGoCategories returns cached categories when valid`() =
        runBlocking {
            // Given
            val cachedCategories = listOf("Solar", "Appliances", "Electronics")

            coEvery { localStorage.getPayGoCategories() } returns cachedCategories
            coEvery { localStorage.shouldSync("categories", CacheManager.SYNC_INTERVAL_FORM_DATA) } returns false

            // When
            val result = repository.getPayGoCategories()

            // Then
            assertTrue(result is Result.Success)
            assertEquals(cachedCategories, (result as Result.Success).data)
            coVerify(exactly = 0) { loanApiService.getPayGoCategories() }
        }

    @Test
    fun `getCategoryProducts fetches from API when cache expired`() =
        runBlocking {
            // Given
            val categoryId = "solar"
            val products =
                listOf(
                    PayGoProduct(id = "prod-1", name = "Solar Panel", price = 500.0, category = "solar"),
                    PayGoProduct(id = "prod-2", name = "Solar Battery", price = 300.0, category = "solar"),
                )
            val apiResponse = mockk<com.soshopay.data.remote.ApiResponse<PayGoProductsResponse>>()
            val response = PayGoProductsResponse(products = products)

            coEvery { localStorage.getPayGoProductsByCategory(categoryId) } returns emptyList()
            coEvery { localStorage.shouldSync("products_$categoryId", CacheManager.SYNC_INTERVAL_FORM_DATA) } returns true
            every { apiResponse.isSuccess() } returns true
            every { apiResponse.getOrNull() } returns response
            coEvery { loanApiService.getCategoryProducts(categoryId) } returns apiResponse
            coEvery { localStorage.insertPayGoProducts(products) } just Runs
            coEvery { localStorage.updateLastSyncTime("products_$categoryId", any()) } just Runs

            // When
            val result = repository.getCategoryProducts(categoryId)

            // Then
            assertTrue(result is Result.Success)
            assertEquals(products, (result as Result.Success).data)
            coVerify { localStorage.insertPayGoProducts(products) }
        }

    @Test
    fun `submitPayGoApplication succeeds with valid application`() =
        runBlocking {
            // Given
            val application =
                PayGoLoanApplication(
                    id = "paygo-123",
                    productId = "prod-1",
                    usagePerDay = "4 hours",
                    repaymentPeriod = "12 months",
                    salaryBand = "1000-2000",
                    guarantor = Guarantor(name = "John Doe", phone = "123456789", relationship = "Friend"),
                    acceptedTerms = true,
                )
            val apiResponse = mockk<com.soshopay.data.remote.ApiResponse<PayGoApplicationResponse>>()
            val response = PayGoApplicationResponse(applicationId = "paygo-123")

            every { application.guarantor.isComplete() } returns true
            every { apiResponse.isSuccess() } returns true
            every { apiResponse.getOrNull() } returns response
            coEvery { loanApiService.submitPayGoApplication(application) } returns apiResponse
            coEvery { localStorage.deleteDraftPayGoApplication("paygo-123") } just Runs

            // When
            val result = repository.submitPayGoApplication(application)

            // Then
            assertTrue(result is Result.Success)
            assertEquals("paygo-123", (result as Result.Success).data)
            coVerify { localStorage.deleteDraftPayGoApplication("paygo-123") }
        }

    // ========== LOAN HISTORY AND DETAILS TESTS ==========

    @Test
    fun `getLoanHistory returns loans from API and caches them`() =
        runBlocking {
            // Given
            val loans =
                listOf(
                    Loan(id = "loan-1", amount = 10000.0, status = LoanStatus.ACTIVE, applicationId = "app-1"),
                    Loan(id = "loan-2", amount = 5000.0, status = LoanStatus.COMPLETED, applicationId = "app-2"),
                )
            val historyResponse =
                LoanHistoryResponse(
                    loans = loans,
                    currentPage = 1,
                    totalPages = 1,
                    totalCount = 2,
                    hasNext = false,
                    hasPrevious = false,
                )
            val apiResponse = mockk<com.soshopay.data.remote.ApiResponse<LoanHistoryResponse>>()

            every { apiResponse.isSuccess() } returns true
            every { apiResponse.getOrNull() } returns historyResponse
            coEvery { loanApiService.getLoanHistory("all", 1, 20) } returns apiResponse
            coEvery { localStorage.insertLoans(loans) } just Runs
            coEvery { localStorage.updateLastSyncTime(CacheManager.CACHE_LOANS, any()) } just Runs

            // When
            val result = repository.getLoanHistory("all", 1, 20)

            // Then
            assertTrue(result is Result.Success)
            assertEquals(historyResponse, (result as Result.Success).data)
            coVerify { localStorage.insertLoans(loans) }
        }

    @Test
    fun `getLoanHistory returns cached loans when API fails on first page`() =
        runBlocking {
            // Given
            val cachedLoans =
                listOf(
                    Loan(id = "loan-1", amount = 10000.0, status = LoanStatus.ACTIVE, applicationId = "app-1"),
                )
            val apiResponse = mockk<com.soshopay.data.remote.ApiResponse<LoanHistoryResponse>>()

            every { apiResponse.isSuccess() } returns false
            every { apiResponse.getErrorOrNull() } returns "Network error"
            coEvery { loanApiService.getLoanHistory("all", 1, 20) } returns apiResponse
            coEvery { localStorage.getAllLoans() } returns cachedLoans

            // When
            val result = repository.getLoanHistory("all", 1, 20)

            // Then
            assertTrue(result is Result.Success)
            val response = (result as Result.Success).data
            assertEquals(cachedLoans, response.loans)
        }

    @Test
    fun `getLoanDetails returns cached details when valid`() =
        runBlocking {
            // Given
            val loanId = "loan-123"
            val details =
                LoanDetails(
                    id = loanId,
                    amount = 10000.0,
                    status = LoanStatus.ACTIVE,
                    interestRate = 15.0,
                    remainingBalance = 8000.0,
                )

            coEvery { localStorage.getLoanDetails(loanId) } returns details
            coEvery { localStorage.shouldSync("loan_details_$loanId", CacheManager.SYNC_INTERVAL_LOANS) } returns false

            // When
            val result = repository.getLoanDetails(loanId)

            // Then
            assertTrue(result is Result.Success)
            assertEquals(details, (result as Result.Success).data)
            coVerify(exactly = 0) { loanApiService.getLoanDetails(loanId) }
        }

    @Test
    fun `getCurrentLoans returns active loans from API`() =
        runBlocking {
            // Given
            val loans =
                listOf(
                    Loan(id = "loan-1", amount = 10000.0, status = LoanStatus.ACTIVE, applicationId = "app-1"),
                    Loan(id = "loan-2", amount = 5000.0, status = LoanStatus.PENDING_DISBURSEMENT, applicationId = "app-2"),
                )
            val response = CurrentLoansResponse(loans = loans)
            val apiResponse = mockk<com.soshopay.data.remote.ApiResponse<CurrentLoansResponse>>()

            every { apiResponse.isSuccess() } returns true
            every { apiResponse.getOrNull() } returns response
            coEvery { loanApiService.getCurrentLoans() } returns apiResponse
            coEvery { localStorage.insertLoans(loans) } just Runs
            coEvery { localStorage.updateLastSyncTime("current_loans", any()) } just Runs

            // When
            val result = repository.getCurrentLoans()

            // Then
            assertTrue(result is Result.Success)
            assertEquals(loans, (result as Result.Success).data)
        }

    // ========== VALIDATION TESTS ==========

    @Test
    fun `validateCashLoanApplication returns valid for correct application`() {
        // Given
        val application =
            CashLoanApplication(
                id = "app-123",
                loanAmount = 10000.0,
                repaymentPeriod = "6 months",
                loanPurpose = "Business",
                employerIndustry = "Technology",
                collateralValue = 12000.0,
                collateralDetails = "Car",
                acceptedTerms = true,
            )

        // When
        val result = repository.validateCashLoanApplication(application)

        // Then
        assertTrue(result.isValid)
        assertTrue(result.errors.isEmpty())
    }

    @Test
    fun `validateCashLoanApplication returns errors for invalid application`() {
        // Given
        val application =
            CashLoanApplication(
                id = "app-123",
                loanAmount = -1000.0,
                repaymentPeriod = "",
                loanPurpose = "",
                employerIndustry = "",
                collateralValue = 0.0,
                collateralDetails = "",
                acceptedTerms = false,
            )

        // When
        val result = repository.validateCashLoanApplication(application)

        // Then
        assertFalse(result.isValid)
        assertTrue(result.errors.contains("Loan amount must be greater than zero"))
        assertTrue(result.errors.contains("Repayment period is required"))
        assertTrue(result.errors.contains("You must accept the loan terms to proceed"))
    }

    @Test
    fun `validateCashLoanApplication adds warning when collateral less than loan amount`() {
        // Given
        val application =
            CashLoanApplication(
                id = "app-123",
                loanAmount = 10000.0,
                repaymentPeriod = "6 months",
                loanPurpose = "Business",
                employerIndustry = "Technology",
                collateralValue = 8000.0, // Less than loan amount
                collateralDetails = "Car",
                acceptedTerms = true,
            )

        // When
        val result = repository.validateCashLoanApplication(application)

        // Then
        assertTrue(result.isValid)
        assertTrue(result.warnings.contains("Collateral value is less than loan amount"))
    }

    @Test
    fun `validatePayGoApplication returns valid for correct application`() {
        // Given
        val guarantor = mockk<Guarantor>()
        every { guarantor.isComplete() } returns true

        val application =
            PayGoLoanApplication(
                id = "paygo-123",
                productId = "prod-1",
                usagePerDay = "4 hours",
                repaymentPeriod = "12 months",
                salaryBand = "1000-2000",
                guarantor = guarantor,
                acceptedTerms = true,
            )

        // When
        val result = repository.validatePayGoApplication(application)

        // Then
        assertTrue(result.isValid)
        assertTrue(result.errors.isEmpty())
    }

    @Test
    fun `validatePayGoApplication returns errors for invalid application`() {
        // Given
        val guarantor = mockk<Guarantor>()
        every { guarantor.isComplete() } returns false

        val application =
            PayGoLoanApplication(
                id = "paygo-123",
                productId = "",
                usagePerDay = "",
                repaymentPeriod = "",
                salaryBand = "",
                guarantor = guarantor,
                acceptedTerms = false,
            )

        // When
        val result = repository.validatePayGoApplication(application)

        // Then
        assertFalse(result.isValid)
        assertTrue(result.errors.contains("Product selection is required"))
        assertTrue(result.errors.contains("Complete guarantor information is required"))
        assertTrue(result.errors.contains("You must accept the loan terms to proceed"))
    }

    // ========== ADDITIONAL TESTS ==========

    @Test
    fun `withdrawApplication refreshes loans after successful withdrawal`() =
        runBlocking {
            // Given
            val applicationId = "app-123"
            val withdrawResponse = mockk<com.soshopay.data.remote.ApiResponse<Unit>>()
            val historyResponse = mockk<com.soshopay.data.remote.ApiResponse<LoanHistoryResponse>>()
            val loans = listOf<Loan>()
            val history = LoanHistoryResponse(loans, 1, 1, 0, false, false)

            every { withdrawResponse.isSuccess() } returns true
            every { historyResponse.isSuccess() } returns true
            every { historyResponse.getOrNull() } returns history
            coEvery { loanApiService.withdrawApplication(applicationId) } returns withdrawResponse
            coEvery { loanApiService.getLoanHistory() } returns historyResponse
            coEvery { localStorage.insertLoans(loans) } just Runs
            coEvery { localStorage.updateLastSyncTime(CacheManager.CACHE_LOANS, any()) } just Runs

            // When
            val result = repository.withdrawApplication(applicationId)

            // Then
            assertTrue(result is Result.Success)
            coVerify { loanApiService.getLoanHistory() }
        }

    @Test
    fun `observeLoanUpdates returns flow from localStorage`() =
        runBlocking {
            // Given
            val loans =
                listOf(
                    Loan(id = "loan-1", amount = 10000.0, status = LoanStatus.ACTIVE, applicationId = "app-1"),
                )

            every { localStorage.observeLoans() } returns flowOf(loans)

            // When
            val result = repository.observeLoanUpdates().first()

            // Then
            assertEquals(loans, result)
        }

    @Test
    fun `getCachedLoans returns loans from localStorage`() =
        runBlocking {
            // Given
            val loans =
                listOf(
                    Loan(id = "loan-1", amount = 10000.0, status = LoanStatus.ACTIVE, applicationId = "app-1"),
                )

            coEvery { localStorage.getAllLoans() } returns loans

            // When
            val result = repository.getCachedLoans()

            // Then
            assertTrue(result is Result.Success)
            assertEquals(loans, (result as Result.Success).data)
        }

    @Test
    fun `downloadLoanAgreement returns byte array on success`() =
        runBlocking {
            // Given
            val loanId = "loan-123"
            val agreementData = byteArrayOf(1, 2, 3, 4, 5)
            val apiResponse = mockk<com.soshopay.data.remote.ApiResponse<ByteArray>>()

            every { apiResponse.isSuccess() } returns true
            every { apiResponse.getOrNull() } returns agreementData
            coEvery { loanApiService.downloadLoanAgreement(loanId) } returns apiResponse

            // When
            val result = repository.downloadLoanAgreement(loanId)

            // Then
            assertTrue(result is Result.Success)
            assertEquals(agreementData, (result as Result.Success).data)
        }
}
