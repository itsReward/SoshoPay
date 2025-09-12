package com.soshopay.data.local

import com.soshopay.domain.model.EarlyPayoffCalculation
import com.soshopay.domain.model.Payment
import com.soshopay.domain.model.PaymentDashboard
import com.soshopay.domain.model.PaymentMethodInfo
import com.soshopay.domain.model.PaymentReceipt
import com.soshopay.domain.model.PaymentSchedule
import kotlinx.coroutines.flow.Flow

// ========== LOCAL PAYMENT STORAGE ==========
interface LocalPaymentStorage {
    // ========== PAYMENTS ==========
    suspend fun insertPayments(payments: List<Payment>)

    suspend fun insertPayment(payment: Payment)

    suspend fun updatePayment(payment: Payment)

    suspend fun getPaymentById(paymentId: String): Payment?

    suspend fun getPaymentsByLoanId(loanId: String): List<Payment>

    suspend fun getPaymentsByUserId(userId: String): List<Payment>

    suspend fun getAllPayments(): List<Payment>

    suspend fun deletePayment(paymentId: String)

    suspend fun deleteAllPayments()

    fun observePayments(): Flow<List<Payment>>

    fun observePaymentById(paymentId: String): Flow<Payment?>

    fun observePaymentsByLoanId(loanId: String): Flow<List<Payment>>

    // ========== PAYMENT DASHBOARD ==========
    suspend fun savePaymentDashboard(dashboard: PaymentDashboard)

    suspend fun getPaymentDashboard(): PaymentDashboard?

    fun observePaymentDashboard(): Flow<PaymentDashboard?>

    // ========== PAYMENT METHODS ==========
    suspend fun savePaymentMethods(methods: List<PaymentMethodInfo>)

    suspend fun getPaymentMethods(): List<PaymentMethodInfo>

    suspend fun deletePaymentMethods()

    // ========== PAYMENT SCHEDULES ==========
    suspend fun insertPaymentSchedules(
        loanId: String,
        schedules: List<PaymentSchedule>,
    )

    suspend fun getPaymentSchedules(loanId: String): List<PaymentSchedule>

    suspend fun updatePaymentSchedule(
        loanId: String,
        schedule: PaymentSchedule,
    )

    suspend fun deletePaymentSchedules(loanId: String)

    // ========== RECEIPTS ==========
    suspend fun savePaymentReceipt(receipt: PaymentReceipt)

    suspend fun getPaymentReceipt(receiptNumber: String): PaymentReceipt?

    suspend fun getAllReceipts(): List<PaymentReceipt>

    suspend fun deleteReceipt(receiptNumber: String)

    // ========== EARLY PAYOFF CALCULATIONS ==========
    suspend fun saveEarlyPayoffCalculation(calculation: EarlyPayoffCalculation)

    suspend fun getEarlyPayoffCalculation(loanId: String): EarlyPayoffCalculation?

    suspend fun deleteEarlyPayoffCalculation(loanId: String)

    // ========== SYNC METADATA ==========
    suspend fun updateLastSyncTime(
        syncType: String,
        timestamp: Long,
    )

    suspend fun getLastSyncTime(syncType: String): Long?

    suspend fun shouldSync(
        syncType: String,
        intervalMillis: Long,
    ): Boolean
}
