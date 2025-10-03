package com.soshopay.data.repository

import com.soshopay.data.local.CacheManager
import com.soshopay.data.local.LocalPaymentStorage
import com.soshopay.data.remote.ApiResponse
import com.soshopay.data.remote.EarlyPayoffProcessResponse
import com.soshopay.data.remote.PaymentApiService
import com.soshopay.data.remote.PaymentProcessResponse
import com.soshopay.data.remote.PaymentStatusResponse
import com.soshopay.domain.model.*
import com.soshopay.domain.repository.Result
import io.mockk.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.test.*

class PaymentRepositoryImplTest {
    private lateinit var paymentApiService: PaymentApiService
    private lateinit var localStorage: LocalPaymentStorage
    private lateinit var cacheManager: CacheManager
    private lateinit var repository: PaymentRepositoryImpl

    @BeforeTest
    fun setup() {
        paymentApiService = mockk()
        localStorage = mockk()
        cacheManager = mockk()
        repository =
            PaymentRepositoryImpl(
                paymentApiService = paymentApiService,
                localStorage = localStorage,
                cacheManager = cacheManager,
            )
    }

    @Test
    fun `getPaymentDashboard returns cached data when cache is valid`() =
        runTest {
            // Given
            val cachedDashboard = mockk<PaymentDashboard>()
            coEvery { localStorage.getPaymentDashboard() } returns cachedDashboard
            coEvery { localStorage.shouldSync(CacheManager.CACHE_DASHBOARD, CacheManager.SYNC_INTERVAL_DASHBOARD) } returns false

            // When
            val result = repository.getPaymentDashboard()

            // Then
            assertTrue(result is com.soshopay.domain.repository.Result.Success)
            assertEquals(cachedDashboard, (result as Result.Success).data)
            coVerify(exactly = 0) { paymentApiService.getPaymentDashboard() }
        }

    @Test
    fun `getPaymentDashboard fetches from API when cache is invalid`() =
        runTest {
            // Given
            val apiDashboard = mockk<PaymentDashboard>()
            coEvery { localStorage.getPaymentDashboard() } returns null
            coEvery { localStorage.shouldSync(CacheManager.CACHE_DASHBOARD, CacheManager.SYNC_INTERVAL_DASHBOARD) } returns true
            coEvery { paymentApiService.getPaymentDashboard() } returns ApiResponse.Success(apiDashboard)
            coEvery { localStorage.savePaymentDashboard(any()) } just Runs
            coEvery { localStorage.updateLastSyncTime(any(), any()) } just Runs

            // When
            val result = repository.getPaymentDashboard()

            // Then
            assertTrue(result is Result.Success)
            assertEquals(apiDashboard, (result as Result.Success).data)
            coVerify { localStorage.savePaymentDashboard(apiDashboard) }
        }

    @Test
    fun `getPaymentHistory returns API data and caches it`() =
        runTest {
            // Given
            val payments = listOf(mockk<Payment>())
            val apiResponse =
                PaymentHistoryResponse(
                    payments = payments,
                    currentPage = 1,
                    totalPages = 1,
                    totalCount = 1,
                    hasNext = false,
                    hasPrevious = false,
                )
            coEvery { paymentApiService.getPaymentHistory(1, 20) } returns ApiResponse.Success(apiResponse)
            coEvery { localStorage.insertPayments(any()) } just Runs
            coEvery { localStorage.updateLastSyncTime(any(), any()) } just Runs

            // When
            val result = repository.getPaymentHistory(1, 20)

            // Then
            assertTrue(result is Result.Success)
            assertEquals(apiResponse, (result as Result.Success).data)
            coVerify { localStorage.insertPayments(payments) }
        }

    @Test
    fun `processPayment validates request and saves to local storage`() =
        runTest {
            // Given
            val request =
                mockk<PaymentRequest> {
                    every { loanId } returns "test-loan"
                    every { amount } returns 100.0
                    every { paymentMethod } returns "MPESA"
                    every { phoneNumber } returns "1234567890"
                    every { validate() } returns ValidationResult(isValid = true)
                }

            val paymentId = "test-payment-id"
            val response =
                mockk<PaymentProcessResponse> {
                    every { this@mockk.paymentId } returns paymentId
                }

            coEvery { paymentApiService.processPayment(request) } returns ApiResponse.Success(response)
            coEvery { localStorage.insertPayment(any()) } just Runs

            // When
            val result = repository.processPayment(request)

            // Then
            assertTrue(result is Result.Success)
            assertEquals(paymentId, (result as Result.Success).data)

            val paymentSlot = slot<Payment>()
            coVerify { localStorage.insertPayment(capture(paymentSlot)) }

            val capturedPayment = paymentSlot.captured
            assertEquals(paymentId, capturedPayment.paymentId)
            assertEquals(PaymentStatus.PROCESSING, capturedPayment.status)
            assertEquals("test-loan", capturedPayment.loanId)
            assertEquals(100.0, capturedPayment.amount)
            assertEquals("MPESA", capturedPayment.method)
            assertEquals("1234567890", capturedPayment.phoneNumber)
        }

    @Test
    fun `getPaymentStatus updates local storage with new status`() =
        runTest {
            // Given
            val paymentId = "test-payment-id"
            val existingPayment = mockk<Payment>()
            val updatedPayment = mockk<Payment>()
            val statusResponse =
                mockk<PaymentStatusResponse> {
                    every { status } returns PaymentStatus.SUCCESSFUL
                    every { receiptNumber } returns "receipt-123"
                    every { failureReason } returns null
                }

            coEvery { localStorage.getPaymentById(paymentId) } returns existingPayment
            coEvery { paymentApiService.getPaymentStatus(paymentId) } returns ApiResponse.Success(statusResponse)
            every {
                existingPayment.copy(
                    status = PaymentStatus.SUCCESSFUL,
                    receiptNumber = "receipt-123",
                    failureReason = null,
                    updatedAt = any(),
                )
            } returns updatedPayment
            coEvery { localStorage.updatePayment(updatedPayment) } just Runs

            // When
            val result = repository.getPaymentStatus(paymentId)

            // Then
            assertTrue(result is Result.Success)
            assertEquals(PaymentStatus.SUCCESSFUL, (result as Result.Success).data)
            coVerify { localStorage.updatePayment(updatedPayment) }
        }

    @Test
    fun `calculateEarlyPayoff returns cached calculation if fresh`() =
        runTest {
            // Given
            val loanId = "test-loan"
            val cachedCalculation =
                mockk<EarlyPayoffCalculation> {
                    every { calculatedAt } returns Clock.System.now().toEpochMilliseconds()
                }
            coEvery { localStorage.getEarlyPayoffCalculation(loanId) } returns cachedCalculation

            // When
            val result = repository.calculateEarlyPayoff(loanId)

            // Then
            assertTrue(result is Result.Success)
            assertEquals(cachedCalculation, (result as Result.Success).data)
            coVerify(exactly = 0) { paymentApiService.calculateEarlyPayoff(any()) }
        }

    @Test
    fun `observePaymentStatus emits status updates`() =
        runTest {
            // Given
            val paymentId = "test-payment"
            val payment =
                mockk<Payment> {
                    every { status } returns PaymentStatus.SUCCESSFUL
                }
            every { localStorage.observePaymentById(paymentId) } returns flowOf(payment)

            // When
            val result = repository.observePaymentStatus(paymentId).first()

            // Then
            assertEquals(PaymentStatus.SUCCESSFUL, result)
        }

    @Test
    fun `getPaymentMethods returns cached methods when valid`() =
        runTest {
            // Given
            val cachedMethods = listOf(mockk<PaymentMethodInfo>())
            coEvery { localStorage.getPaymentMethods() } returns cachedMethods
            coEvery { localStorage.shouldSync(CacheManager.CACHE_METHODS, CacheManager.SYNC_INTERVAL_FORM_DATA) } returns false

            // When
            val result = repository.getPaymentMethods()

            // Then
            assertTrue(result is Result.Success)
            assertEquals(cachedMethods, (result as Result.Success).data)
            coVerify(exactly = 0) { paymentApiService.getPaymentMethods() }
        }

    @Test
    fun `downloadReceipt returns receipt bytes`() =
        runTest {
            // Given
            val receiptNumber = "receipt-123"
            val receiptBytes = ByteArray(10)
            coEvery { paymentApiService.downloadReceipt(receiptNumber) } returns ApiResponse.Success(receiptBytes)

            // When
            val result = repository.downloadReceipt(receiptNumber)

            // Then
            assertTrue(result is Result.Success)
            assertEquals(receiptBytes, (result as Result.Success).data)
        }

    @Test
    fun `processEarlyPayoff validates request and updates cache`() =
        runTest {
            // Given
            val loanId = "test-loan"
            val request = mockk<PaymentRequest>()

            // Mock successful validation
            every { request.validate() } returns ValidationResult(isValid = true)
            every { request.paymentMethod } returns "MPESA"
            every { request.phoneNumber } returns "1234567890"

            val paymentId = "test-payment-id"
            val apiResponse =
                mockk<EarlyPayoffProcessResponse> {
                    every { this@mockk.paymentId } returns paymentId
                    every { earlyPayoffAmount } returns 1000.0
                }

            coEvery { paymentApiService.processEarlyPayoff(loanId, request) } returns ApiResponse.Success(apiResponse)
            coEvery { localStorage.insertPayment(any()) } just Runs
            coEvery { localStorage.deleteEarlyPayoffCalculation(loanId) } just Runs

            // Mock additional required dependencies
            val timestamp = 123456789L
            val fixedClock = mockk<Clock>()
            every { fixedClock.now() } returns Instant.fromEpochMilliseconds(timestamp)
            mockkObject(Clock.System)
            every { Clock.System.now() } returns fixedClock.now()

            // When
            val result = repository.processEarlyPayoff(loanId, request)

            // Then
            assertTrue(result is Result.Success)
            assertEquals(paymentId, (result as Result.Success).data)
            coVerify { localStorage.insertPayment(any()) }
            coVerify { localStorage.deleteEarlyPayoffCalculation(loanId) }
        }

    @Test
    fun `syncPaymentsFromRemote updates local storage`() =
        runTest {
            // Given
            val payments = listOf(mockk<Payment>())
            val apiResponse =
                PaymentHistoryResponse(
                    payments = payments,
                    currentPage = 1,
                    totalPages = 1,
                    totalCount = 1,
                    hasNext = false,
                    hasPrevious = false,
                )
            coEvery { paymentApiService.getPaymentHistory() } returns ApiResponse.Success(apiResponse)
            coEvery { localStorage.insertPayments(any()) } just Runs
            coEvery { localStorage.updateLastSyncTime(any(), any()) } just Runs

            // When
            val result = repository.syncPaymentsFromRemote()

            // Then
            assertTrue(result is Result.Success)
            coVerify { localStorage.insertPayments(payments) }
            coVerify { localStorage.updateLastSyncTime(CacheManager.CACHE_PAYMENTS, any()) }
        }
}
