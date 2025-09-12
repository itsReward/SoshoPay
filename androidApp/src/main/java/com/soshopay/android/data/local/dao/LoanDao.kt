package com.soshopay.android.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.soshopay.android.data.local.entities.LoanDetailsEntity
import com.soshopay.android.data.local.entities.LoanEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LoanDao {
    @Query("SELECT * FROM loans WHERE id = :loanId")
    suspend fun getLoanById(loanId: String): LoanEntity?

    @Query("SELECT * FROM loans WHERE userId = :userId ORDER BY createdAt DESC")
    suspend fun getLoansByUserId(userId: String): List<LoanEntity>

    @Query("SELECT * FROM loans ORDER BY createdAt DESC")
    suspend fun getAllLoans(): List<LoanEntity>

    @Query("SELECT * FROM loans ORDER BY createdAt DESC")
    fun observeLoans(): Flow<List<LoanEntity>>

    @Query("SELECT * FROM loans WHERE id = :loanId")
    fun observeLoanById(loanId: String): Flow<LoanEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLoans(loans: List<LoanEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLoan(loan: LoanEntity)

    @Query("DELETE FROM loans WHERE id = :loanId")
    suspend fun deleteLoan(loanId: String)

    @Query("DELETE FROM loans")
    suspend fun deleteAllLoans()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLoanDetails(loanDetails: LoanDetailsEntity)

    @Query("SELECT * FROM loan_details WHERE loanId = :loanId")
    suspend fun getLoanDetailsById(loanId: String): LoanDetailsEntity?

    @Query("DELETE FROM loan_details WHERE loanId = :loanId")
    suspend fun deleteLoanDetails(loanId: String)
}
