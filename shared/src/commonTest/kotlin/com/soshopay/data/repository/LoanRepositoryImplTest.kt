package com.soshopay.data.repository

import com.soshopay.data.local.CacheManager
import com.soshopay.data.local.LocalLoanStorage
import com.soshopay.data.remote.ApiResponse
import com.soshopay.data.remote.CashLoanApplicationResponse
import com.soshopay.data.remote.LoanApiService
import com.soshopay.data.remote.PayGoProductsResponse
import com.soshopay.domain.model.*
import com.soshopay.domain.repository.Result
import io.mockk.*
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlin.test.*

class LoanRepositoryImplTest {
    private lateinit var loanApiService: LoanApiService
    private lateinit var localStorage: LocalLoanStorage
    private lateinit var cacheManager: CacheManager
    private lateinit var repository: LoanRepositoryImpl

    @BeforeTest
    fun setup() {
        loanApiService = mockk()
        localStorage = mockk()
        cacheManager = mockk()
        repository =
            LoanRepositoryImpl(
                loanApiService = loanApiService,
                localStorage = localStorage,
                cacheManager = cacheManager,
            )
    }

    @Test
    fun `getCashLoanFormData returns cached data when cache is valid`() =
        runTest {
            // Given
            val cachedData = mockk<CashLoanFormData>()
            coEvery { localStorage.getCashLoanFormData() } returns cachedData
            coEvery { localStorage.shouldSync("form_data", CacheManager.SYNC_INTERVAL_FORM_DATA) } returns false

            // When
            val result = repository.getCashLoanFormData()

            // Then
            assertTrue(result is Result.Success)
            assertEquals(cachedData, (result as Result.Success).data)
            coVerify(exactly = 0) { loanApiService.getCashLoanFormData() }
        }

    @Test
    fun `getCashLoanFormData fetches from API when cache is invalid`() =
        runTest {
            // Given
            val apiData = mockk<CashLoanFormData>()
            val apiResponse = ApiResponse.Success<CashLoanFormData>(apiData)
            coEvery { localStorage.getCashLoanFormData() } returns null
            coEvery { localStorage.shouldSync("form_data", CacheManager.SYNC_INTERVAL_FORM_DATA) } returns true
            coEvery { loanApiService.getCashLoanFormData() } returns apiResponse
            coEvery { localStorage.saveCashLoanFormData(any()) } just Runs
            coEvery { localStorage.updateLastSyncTime(any(), any()) } just Runs

            // When
            val result = repository.getCashLoanFormData()

            // Then
            assertTrue(result is Result.Success)
            assertEquals(apiData, (result as Result.Success).data)
            coVerify { localStorage.saveCashLoanFormData(apiData) }
            coVerify { localStorage.updateLastSyncTime("form_data", any()) }
        }

    @Test
    fun `calculateCashLoanTerms returns success on valid request`() =
        runTest {
            // Given
            val request =
                CashLoanCalculationRequest(
                    loanAmount = 199.92,
                    repaymentPeriod = "3456786543",
                    employerIndustry = "Telecoms",
                    collateralValue = 478.23,
                    monthlyIncome = 24.24,
                )
            val terms = mockk<CashLoanTerms>()
            val apiResponse = ApiResponse.Success(terms)
            coEvery { loanApiService.calculateCashLoanTerms(request) } returns apiResponse

            // When
            val result = repository.calculateCashLoanTerms(request)

            // Then
            assertTrue(result is com.soshopay.domain.repository.Result.Success)
            assertEquals(terms, (result as com.soshopay.domain.repository.Result.Success).data)
        }

    @Test
    fun `submitCashLoanApplication validates before submission`() =
        runTest {
            // Given - Use a real CashLoanApplication object instead of a mock
            val application =
                CashLoanApplication(
                    id = "test-app-id",
                    loanAmount = 1000.0,
                    repaymentPeriod = "12",
                    loanPurpose = "Business",
                    employerIndustry = "Technology",
                    collateralValue = 2000.0,
                    collateralDetails = "Car",
                    acceptedTerms = true,
                )

            val applicationId = "test-app-id"
            val apiResponse =
                ApiResponse.Success(
                    CashLoanApplicationResponse(
                        applicationId,
                        "statusId",
                        "message",
                        "389798232",
                    ),
                )

            coEvery { loanApiService.submitCashLoanApplication(application) } returns apiResponse
            coEvery { localStorage.deleteDraftCashLoanApplication(eq(application.id)) } just Runs

            // When
            val result = repository.submitCashLoanApplication(application)

            // Then
            assertTrue(result is Result.Success)
            assertEquals(applicationId, (result as Result.Success).data)
            coVerify(exactly = 1) { loanApiService.submitCashLoanApplication(application) }
            coVerify(exactly = 1) { localStorage.deleteDraftCashLoanApplication(eq(application.id)) }
        }

    @Test
    fun `saveDraftCashLoanApplication saves to local storage`() =
        runTest {
            // Given
            val application = mockk<CashLoanApplication>()
            coEvery { localStorage.saveDraftCashLoanApplication(application) } just Runs

            // When
            val result = repository.saveDraftCashLoanApplication(application)

            // Then
            assertTrue(result is Result.Success)
            coVerify { localStorage.saveDraftCashLoanApplication(application) }
        }

    @Test
    fun `getDraftCashLoanApplication returns cached draft`() =
        runTest {
            // Given
            val userId = "test-user"
            val draft = mockk<CashLoanApplication>()
            coEvery { localStorage.getDraftCashLoanApplication(userId) } returns draft

            // When
            val result = repository.getDraftCashLoanApplication(userId)

            // Then
            assertTrue(result is Result.Success)
            assertEquals(draft, (result as Result.Success).data)
        }

    @Test
    fun `getPayGoCategories returns cached categories when valid`() =
        runTest {
            // Given
            val cachedCategories = listOf("Electronics", "Furniture")
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
    fun `getCategoryProducts fetches from API when cache is invalid`() =
        runTest {
            // Given
            val categoryId = "cat-123"
            val apiProducts = listOf(mockk<PayGoProduct>(), mockk<PayGoProduct>())
            val apiResponse =
                mockk<ApiResponse.Success<PayGoProductsResponse>> {
                    every { isSuccess() } returns true
                    every { getOrNull() } returns
                        mockk {
                            every { products } returns apiProducts
                        }
                }
            coEvery { localStorage.getPayGoProductsByCategory(categoryId) } returns emptyList()
            coEvery { localStorage.shouldSync("products_$categoryId", CacheManager.SYNC_INTERVAL_FORM_DATA) } returns true
            coEvery { loanApiService.getCategoryProducts(categoryId) } returns apiResponse
            coEvery { localStorage.insertPayGoProducts(any()) } just Runs
            coEvery { localStorage.updateLastSyncTime(any(), any()) } just Runs

            // When
            val result = repository.getCategoryProducts(categoryId)

            // Then
            assertTrue(result is Result.Success)
            assertEquals(apiProducts, (result as Result.Success).data)
            coVerify { localStorage.insertPayGoProducts(apiProducts) }
            coVerify { localStorage.updateLastSyncTime("products_$categoryId", any()) }
        }

    @Test
    fun `deleteDraftCashLoanApplication removes from local storage`() =
        runTest {
            // Given
            val applicationId = "test-app-id"
            coEvery { localStorage.deleteDraftCashLoanApplication(applicationId) } just Runs

            // When
            val result = repository.deleteDraftCashLoanApplication(applicationId)

            // Then
            assertTrue(result is Result.Success)
            coVerify { localStorage.deleteDraftCashLoanApplication(applicationId) }
        }
}
