package com.soshopay.android.data.local

import com.soshopay.android.data.local.dao.DraftApplicationDao
import com.soshopay.android.data.local.dao.FormDataDao
import com.soshopay.android.data.local.dao.LoanDao
import com.soshopay.android.data.local.dao.SyncMetadataDao
import com.soshopay.android.data.local.entities.CashLoanFormDataEntity
import com.soshopay.android.data.local.entities.DraftCashLoanEntity
import com.soshopay.android.data.local.entities.DraftPayGoLoanEntity
import com.soshopay.android.data.local.entities.LoanDetailsEntity
import com.soshopay.android.data.local.entities.LoanEntity
import com.soshopay.android.data.local.entities.PayGoCategoriesEntity
import com.soshopay.android.data.local.entities.PayGoProductEntity
import com.soshopay.android.data.local.entities.SyncMetadataEntity
import com.soshopay.data.local.LocalLoanStorage
import com.soshopay.domain.model.CashLoanApplication
import com.soshopay.domain.model.CashLoanFormData
import com.soshopay.domain.model.Loan
import com.soshopay.domain.model.LoanDetails
import com.soshopay.domain.model.PayGoLoanApplication
import com.soshopay.domain.model.PayGoProduct
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class AndroidLocalLoanStorage(
    private val loanDao: LoanDao,
    private val draftDao: DraftApplicationDao,
    private val formDataDao: FormDataDao,
    private val syncMetadataDao: SyncMetadataDao,
) : LocalLoanStorage {
    // ========== DRAFT APPLICATIONS ==========
    override suspend fun saveDraftCashLoanApplication(application: CashLoanApplication) {
        val entity =
            DraftCashLoanEntity(
                id = application.id.ifEmpty { "draft_cash_${System.currentTimeMillis()}" },
                userId = application.userId,
                applicationData = Json.encodeToString(application),
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
            )
        draftDao.insertDraftCashLoan(entity)
    }

    override suspend fun getDraftCashLoanApplication(userId: String): CashLoanApplication? =
        draftDao.getDraftCashLoanByUserId(userId)?.let { entity ->
            Json.decodeFromString<CashLoanApplication>(entity.applicationData)
        }

    override suspend fun deleteDraftCashLoanApplication(applicationId: String) {
        draftDao.deleteDraftCashLoan(applicationId)
    }

    override suspend fun saveDraftPayGoApplication(application: PayGoLoanApplication) {
        val entity =
            DraftPayGoLoanEntity(
                id = application.id.ifEmpty { "draft_paygo_${System.currentTimeMillis()}" },
                userId = application.userId,
                applicationData = Json.encodeToString(application),
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
            )
        draftDao.insertDraftPayGoLoan(entity)
    }

    override suspend fun getDraftPayGoApplication(userId: String): PayGoLoanApplication? =
        draftDao.getDraftPayGoLoanByUserId(userId)?.let { entity ->
            Json.decodeFromString<PayGoLoanApplication>(entity.applicationData)
        }

    override suspend fun deleteDraftPayGoApplication(applicationId: String) {
        draftDao.deleteDraftPayGoLoan(applicationId)
    }

    // ========== LOANS ==========
    override suspend fun insertLoans(loans: List<Loan>) {
        val entities =
            loans.map { loan ->
                LoanEntity(
                    id = loan.id,
                    userId = loan.userId,
                    applicationId = loan.applicationId,
                    loanType = loan.loanType.name,
                    originalAmount = loan.originalAmount,
                    totalAmount = loan.totalAmount,
                    remainingBalance = loan.remainingBalance,
                    interestRate = loan.interestRate,
                    repaymentPeriod = loan.repaymentPeriod,
                    disbursementDate = loan.disbursementDate,
                    maturityDate = loan.maturityDate,
                    status = loan.status.name,
                    nextPaymentDate = loan.nextPaymentDate,
                    nextPaymentAmount = loan.nextPaymentAmount,
                    paymentsCompleted = loan.paymentsCompleted,
                    totalPayments = loan.totalPayments,
                    productName = loan.productName,
                    loanPurpose = loan.loanPurpose,
                    installationDate = loan.installationDate,
                    rejectionReason = loan.rejectionReason,
                    rejectionDate = loan.rejectionDate,
                    createdAt = loan.createdAt,
                    updatedAt = loan.updatedAt,
                )
            }
        loanDao.insertLoans(entities)
    }

    override suspend fun insertLoan(loan: Loan) {
        insertLoans(listOf(loan))
    }

    override suspend fun updateLoan(loan: Loan) {
        insertLoan(loan) // Room's onConflictStrategy.REPLACE handles updates
    }

    override suspend fun getLoanById(loanId: String): Loan? = loanDao.getLoanById(loanId)?.toDomain()

    override suspend fun getLoansByUserId(userId: String): List<Loan> = loanDao.getLoansByUserId(userId).map { it.toDomain() }

    override suspend fun getAllLoans(): List<Loan> = loanDao.getAllLoans().map { it.toDomain() }

    override suspend fun deleteLoan(loanId: String) {
        loanDao.deleteLoan(loanId)
    }

    override suspend fun deleteAllLoans() {
        loanDao.deleteAllLoans()
    }

    override fun observeLoans(): Flow<List<Loan>> =
        loanDao.observeLoans().map { entities ->
            entities.map { it.toDomain() }
        }

    override fun observeLoanById(loanId: String): Flow<Loan?> =
        loanDao.observeLoanById(loanId).map { entity ->
            entity?.toDomain()
        }

    // ========== LOAN DETAILS ==========
    override suspend fun insertLoanDetails(loanDetails: LoanDetails) {
        val entity =
            LoanDetailsEntity(
                loanId = loanDetails.loan.id,
                loanDetailsData = Json.encodeToString(loanDetails),
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
            )
        loanDao.insertLoanDetails(entity)
    }

    override suspend fun getLoanDetails(loanId: String): LoanDetails? =
        loanDao.getLoanDetailsById(loanId)?.let { entity ->
            Json.decodeFromString<LoanDetails>(entity.loanDetailsData)
        }

    override suspend fun deleteLoanDetails(loanId: String) {
        loanDao.deleteLoanDetails(loanId)
    }

    // ========== PAYGO PRODUCTS ==========
    override suspend fun insertPayGoProducts(products: List<PayGoProduct>) {
        val entities =
            products.map { product ->
                PayGoProductEntity(
                    id = product.id,
                    name = product.name,
                    category = product.category,
                    price = product.price,
                    description = product.description,
                    specifications = product.specifications,
                    image = product.image,
                    installationFee = product.installationFee,
                    isAvailable = product.isAvailable,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis(),
                )
            }
        formDataDao.insertPayGoProducts(entities)
    }

    override suspend fun getPayGoProductsByCategory(category: String): List<PayGoProduct> =
        formDataDao.getPayGoProductsByCategory(category).map {
            it.toDomain()
        }

    override suspend fun getAllPayGoProducts(): List<PayGoProduct> = formDataDao.getAllPayGoProducts().map { it.toDomain() }

    override suspend fun deletePayGoProducts() {
        formDataDao.deleteAllPayGoProducts()
    }

    // ========== FORM DATA CACHE ==========
    override suspend fun saveCashLoanFormData(formData: CashLoanFormData) {
        val entity =
            CashLoanFormDataEntity(
                id = "cash_loan_form_data",
                formDataJson = Json.encodeToString(formData),
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
            )
        formDataDao.insertCashLoanFormData(entity)
    }

    override suspend fun getCashLoanFormData(): CashLoanFormData? =
        formDataDao.getCashLoanFormData()?.let { entity ->
            Json.decodeFromString<CashLoanFormData>(entity.formDataJson)
        }

    override suspend fun savePayGoCategories(categories: List<String>) {
        val entity =
            PayGoCategoriesEntity(
                id = "paygo_categories",
                categoriesJson = Json.encodeToString(categories),
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
            )
        formDataDao.insertPayGoCategories(entity)
    }

    override suspend fun getPayGoCategories(): List<String> =
        formDataDao.getPayGoCategories()?.let { entity ->
            Json.decodeFromString<List<String>>(entity.categoriesJson)
        } ?: emptyList()

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
