package com.soshopay.android.data.local

import com.soshopay.android.data.local.dao.PaymentDao
import com.soshopay.android.data.local.dao.PaymentDashboardDao
import com.soshopay.android.data.local.dao.PaymentReceiptDao
import com.soshopay.android.data.local.dao.PaymentScheduleDao
import com.soshopay.android.data.local.dao.SyncMetadataDao
import com.soshopay.android.data.local.entities.EarlyPayoffCalculationEntity
import com.soshopay.android.data.local.entities.PaymentDashboardEntity
import com.soshopay.android.data.local.entities.PaymentEntity
import com.soshopay.android.data.local.entities.PaymentMethodEntity
import com.soshopay.android.data.local.entities.PaymentReceiptEntity
import com.soshopay.android.data.local.entities.PaymentScheduleEntity
import com.soshopay.android.data.local.entities.SyncMetadataEntity
import com.soshopay.data.local.LocalPaymentStorage
import com.soshopay.domain.model.EarlyPayoffCalculation
import com.soshopay.domain.model.Payment
import com.soshopay.domain.model.PaymentDashboard
import com.soshopay.domain.model.PaymentMethodInfo
import com.soshopay.domain.model.PaymentReceipt
import com.soshopay.domain.model.PaymentSchedule
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class AndroidLocalPaymentStorage(
    private val paymentDao: PaymentDao,
    private val dashboardDao: PaymentDashboardDao,
    private val scheduleDao: PaymentScheduleDao,
    private val receiptDao: PaymentReceiptDao,
    private val syncMetadataDao: SyncMetadataDao,
) : LocalPaymentStorage {
    // ========== PAYMENTS ==========
    override suspend fun insertPayments(payments: List<Payment>) {
        val entities =
            payments.map { payment ->
                PaymentEntity(
                    id = payment.id,
                    userId = payment.userId,
                    loanId = payment.loanId,
                    paymentId = payment.paymentId,
                    amount = payment.amount,
                    method = payment.method,
                    phoneNumber = payment.phoneNumber,
                    receiptNumber = payment.receiptNumber,
                    status = payment.status.name,
                    processedAt = payment.processedAt,
                    failureReason = payment.failureReason,
                    createdAt = payment.createdAt,
                    principal = payment.principal,
                    interest = payment.interest,
                    penalties = payment.penalties,
                    updatedAt = payment.updatedAt,
                )
            }
        paymentDao.insertPayments(entities)
    }

    override suspend fun insertPayment(payment: Payment) {
        insertPayments(listOf(payment))
    }

    override suspend fun updatePayment(payment: Payment) {
        insertPayment(payment) // Room's onConflictStrategy.REPLACE handles updates
    }

    override suspend fun getPaymentById(paymentId: String): Payment? = paymentDao.getPaymentById(paymentId)?.toDomain()

    override suspend fun getPaymentsByLoanId(loanId: String): List<Payment> = paymentDao.getPaymentsByLoanId(loanId).map { it.toDomain() }

    override suspend fun getPaymentsByUserId(userId: String): List<Payment> = paymentDao.getPaymentsByUserId(userId).map { it.toDomain() }

    override suspend fun getAllPayments(): List<Payment> = paymentDao.getAllPayments().map { it.toDomain() }

    override suspend fun deletePayment(paymentId: String) {
        paymentDao.deletePayment(paymentId)
    }

    override suspend fun deleteAllPayments() {
        paymentDao.deleteAllPayments()
    }

    override fun observePayments(): Flow<List<Payment>> =
        paymentDao.observePayments().map { entities ->
            entities.map { it.toDomain() }
        }

    override fun observePaymentById(paymentId: String): Flow<Payment?> =
        paymentDao.observePaymentById(paymentId).map { entity ->
            entity?.toDomain()
        }

    override fun observePaymentsByLoanId(loanId: String): Flow<List<Payment>> =
        paymentDao.observePaymentsByLoanId(loanId).map { entities ->
            entities.map { it.toDomain() }
        }

    // ========== PAYMENT DASHBOARD ==========
    override suspend fun savePaymentDashboard(dashboard: PaymentDashboard) {
        val entity =
            PaymentDashboardEntity(
                id = "payment_dashboard",
                dashboardData = Json.encodeToString(dashboard),
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
            )
        dashboardDao.insertPaymentDashboard(entity)
    }

    override suspend fun getPaymentDashboard(): PaymentDashboard? =
        dashboardDao.getPaymentDashboard()?.let { entity ->
            Json.decodeFromString<PaymentDashboard>(entity.dashboardData)
        }

    override fun observePaymentDashboard(): Flow<PaymentDashboard?> =
        dashboardDao.observePaymentDashboard().map { entity ->
            entity?.let { Json.decodeFromString<PaymentDashboard>(it.dashboardData) }
        }

    // ========== PAYMENT METHODS ==========
    override suspend fun savePaymentMethods(methods: List<PaymentMethodInfo>) {
        val entities =
            methods.map { method ->
                PaymentMethodEntity(
                    id = method.id,
                    name = method.name,
                    type = method.type.name,
                    isActive = method.isActive,
                    description = method.description,
                    processingTime = method.processingTime,
                    minimumAmount = method.minimumAmount,
                    maximumAmount = method.maximumAmount,
                    fees = method.fees,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis(),
                )
            }
        dashboardDao.insertPaymentMethods(entities)
    }

    override suspend fun getPaymentMethods(): List<PaymentMethodInfo> = dashboardDao.getPaymentMethods().map { it.toDomain() }

    override suspend fun deletePaymentMethods() {
        dashboardDao.deleteAllPaymentMethods()
    }

    // ========== PAYMENT SCHEDULES ==========
    override suspend fun insertPaymentSchedules(
        loanId: String,
        schedules: List<PaymentSchedule>,
    ) {
        val entities =
            schedules.map { schedule ->
                PaymentScheduleEntity(
                    id = "${loanId}_${schedule.paymentNumber}",
                    loanId = loanId,
                    paymentNumber = schedule.paymentNumber,
                    dueDate = schedule.dueDate,
                    amount = schedule.amount,
                    principal = schedule.principal,
                    interest = schedule.interest,
                    status = schedule.status.name,
                    paidDate = schedule.paidDate,
                    receiptNumber = schedule.receiptNumber,
                    penalties = schedule.penalties,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis(),
                )
            }
        scheduleDao.insertPaymentSchedules(entities)
    }

    override suspend fun getPaymentSchedules(loanId: String): List<PaymentSchedule> =
        scheduleDao.getPaymentSchedulesByLoanId(loanId).map {
            it.toDomain()
        }

    override suspend fun updatePaymentSchedule(
        loanId: String,
        schedule: PaymentSchedule,
    ) {
        val entity =
            PaymentScheduleEntity(
                id = "${loanId}_${schedule.paymentNumber}",
                loanId = loanId,
                paymentNumber = schedule.paymentNumber,
                dueDate = schedule.dueDate,
                amount = schedule.amount,
                principal = schedule.principal,
                interest = schedule.interest,
                status = schedule.status.name,
                paidDate = schedule.paidDate,
                receiptNumber = schedule.receiptNumber,
                penalties = schedule.penalties,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
            )
        scheduleDao.updatePaymentSchedule(entity)
    }

    override suspend fun deletePaymentSchedules(loanId: String) {
        scheduleDao.deletePaymentSchedulesByLoanId(loanId)
    }

    // ========== RECEIPTS ==========
    override suspend fun savePaymentReceipt(receipt: PaymentReceipt) {
        val entity =
            PaymentReceiptEntity(
                receiptNumber = receipt.receiptNumber,
                paymentId = receipt.paymentId,
                loanId = receipt.loanId,
                amount = receipt.amount,
                paymentMethod = receipt.paymentMethod,
                phoneNumber = receipt.phoneNumber,
                processedAt = receipt.processedAt,
                customerName = receipt.customerName,
                loanType = receipt.loanType.name,
                productName = receipt.productName,
                transactionReference = receipt.transactionReference,
                principal = receipt.principal,
                interest = receipt.interest,
                penalties = receipt.penalties,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
            )
        receiptDao.insertPaymentReceipt(entity)
    }

    override suspend fun getPaymentReceipt(receiptNumber: String): PaymentReceipt? =
        receiptDao.getPaymentReceiptByNumber(receiptNumber)?.toDomain()

    override suspend fun getAllReceipts(): List<PaymentReceipt> = receiptDao.getAllPaymentReceipts().map { it.toDomain() }

    override suspend fun deleteReceipt(receiptNumber: String) {
        receiptDao.deletePaymentReceipt(receiptNumber)
    }

    // ========== EARLY PAYOFF CALCULATIONS ==========
    override suspend fun saveEarlyPayoffCalculation(calculation: EarlyPayoffCalculation) {
        val entity =
            EarlyPayoffCalculationEntity(
                loanId = calculation.loanId,
                currentBalance = calculation.currentBalance,
                earlyPayoffAmount = calculation.earlyPayoffAmount,
                savingsAmount = calculation.savingsAmount,
                calculatedAt = calculation.calculatedAt,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
            )
        dashboardDao.insertEarlyPayoffCalculation(entity)
    }

    override suspend fun getEarlyPayoffCalculation(loanId: String): EarlyPayoffCalculation? =
        dashboardDao.getEarlyPayoffCalculation(loanId)?.toDomain()

    override suspend fun deleteEarlyPayoffCalculation(loanId: String) {
        dashboardDao.deleteEarlyPayoffCalculation(loanId)
    }

    // ========== SYNC METADATA ==========
    override suspend fun updateLastSyncTime(
        syncType: String,
        timestamp: Long,
    ) {
        val entity =
            SyncMetadataEntity(
                syncType = syncType,
                lastSyncTime = timestamp,
                updatedAt = System.currentTimeMillis(),
            )
        syncMetadataDao.insertOrUpdate(entity)
    }

    override suspend fun getLastSyncTime(syncType: String): Long? = syncMetadataDao.getLastSyncTime(syncType)

    override suspend fun shouldSync(
        syncType: String,
        intervalMillis: Long,
    ): Boolean {
        val lastSync = getLastSyncTime(syncType) ?: 0
        return (System.currentTimeMillis() - lastSync) > intervalMillis
    }
}
