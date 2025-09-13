package com.soshopay.android.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.soshopay.android.data.local.entities.DraftCashLoanEntity
import com.soshopay.android.data.local.entities.DraftPayGoLoanEntity

@Dao
interface DraftApplicationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDraftCashLoan(draft: DraftCashLoanEntity)

    @Query("SELECT * FROM draft_cash_loans WHERE userId = :userId LIMIT 1")
    suspend fun getDraftCashLoanByUserId(userId: String): DraftCashLoanEntity?

    @Query("DELETE FROM draft_cash_loans WHERE id = :applicationId")
    suspend fun deleteDraftCashLoan(applicationId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDraftPayGoLoan(draft: DraftPayGoLoanEntity)

    @Query("SELECT * FROM draft_paygo_loans WHERE userId = :userId LIMIT 1")
    suspend fun getDraftPayGoLoanByUserId(userId: String): DraftPayGoLoanEntity?

    @Query("DELETE FROM draft_paygo_loans WHERE id = :applicationId")
    suspend fun deleteDraftPayGoLoan(applicationId: String)
}
