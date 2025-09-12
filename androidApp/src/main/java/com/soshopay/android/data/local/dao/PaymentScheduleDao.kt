package com.soshopay.android.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.soshopay.android.data.local.entities.PaymentScheduleEntity

@Dao
interface PaymentScheduleDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPaymentSchedules(schedules: List<PaymentScheduleEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updatePaymentSchedule(schedule: PaymentScheduleEntity)

    @Query("SELECT * FROM payment_schedules WHERE loanId = :loanId ORDER BY paymentNumber ASC")
    suspend fun getPaymentSchedulesByLoanId(loanId: String): List<PaymentScheduleEntity>

    @Query("DELETE FROM payment_schedules WHERE loanId = :loanId")
    suspend fun deletePaymentSchedulesByLoanId(loanId: String)
}
