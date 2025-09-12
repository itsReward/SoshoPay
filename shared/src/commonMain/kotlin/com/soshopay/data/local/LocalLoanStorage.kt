package com.soshopay.data.local

import com.soshopay.domain.model.CashLoanApplication
import com.soshopay.domain.model.CashLoanFormData
import com.soshopay.domain.model.Loan
import com.soshopay.domain.model.LoanDetails
import com.soshopay.domain.model.PayGoLoanApplication
import com.soshopay.domain.model.PayGoProduct
import kotlinx.coroutines.flow.Flow

// ========== LOCAL LOAN STORAGE ==========
interface LocalLoanStorage {
    // ========== DRAFT APPLICATIONS ==========
    suspend fun saveDraftCashLoanApplication(application: CashLoanApplication)

    suspend fun getDraftCashLoanApplication(userId: String): CashLoanApplication?

    suspend fun deleteDraftCashLoanApplication(applicationId: String)

    suspend fun saveDraftPayGoApplication(application: PayGoLoanApplication)

    suspend fun getDraftPayGoApplication(userId: String): PayGoLoanApplication?

    suspend fun deleteDraftPayGoApplication(applicationId: String)

    // ========== LOANS ==========
    suspend fun insertLoans(loans: List<Loan>)

    suspend fun insertLoan(loan: Loan)

    suspend fun updateLoan(loan: Loan)

    suspend fun getLoanById(loanId: String): Loan?

    suspend fun getLoansByUserId(userId: String): List<Loan>

    suspend fun getAllLoans(): List<Loan>

    suspend fun deleteLoan(loanId: String)

    suspend fun deleteAllLoans()

    fun observeLoans(): Flow<List<Loan>>

    fun observeLoanById(loanId: String): Flow<Loan?>

    // ========== LOAN DETAILS ==========
    suspend fun insertLoanDetails(loanDetails: LoanDetails)

    suspend fun getLoanDetails(loanId: String): LoanDetails?

    suspend fun deleteLoanDetails(loanId: String)

    // ========== PAYGO PRODUCTS ==========
    suspend fun insertPayGoProducts(products: List<PayGoProduct>)

    suspend fun getPayGoProductsByCategory(category: String): List<PayGoProduct>

    suspend fun getAllPayGoProducts(): List<PayGoProduct>

    suspend fun deletePayGoProducts()

    // ========== FORM DATA CACHE ==========
    suspend fun saveCashLoanFormData(formData: CashLoanFormData)

    suspend fun getCashLoanFormData(): CashLoanFormData?

    suspend fun savePayGoCategories(categories: List<String>)

    suspend fun getPayGoCategories(): List<String>

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
