package com.soshopay.android.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.soshopay.android.data.local.entities.PaymentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PaymentDao {
    @Query("SELECT * FROM payments WHERE id = :paymentId")
    suspend fun getPaymentById(paymentId: String): PaymentEntity?

    @Query("SELECT * FROM payments WHERE loanId = :loanId ORDER BY createdAt DESC")
    suspend fun getPaymentsByLoanId(loanId: String): List<PaymentEntity>

    @Query("SELECT * FROM payments WHERE userId = :userId ORDER BY createdAt DESC")
    suspend fun getPaymentsByUserId(userId: String): List<PaymentEntity>

    @Query("SELECT * FROM payments ORDER BY createdAt DESC")
    suspend fun getAllPayments(): List<PaymentEntity>

    @Query("SELECT * FROM payments ORDER BY createdAt DESC")
    fun observePayments(): Flow<List<PaymentEntity>>

    @Query("SELECT * FROM payments WHERE id = :paymentId")
    fun observePaymentById(paymentId: String): Flow<PaymentEntity?>

    @Query("SELECT * FROM payments WHERE loanId = :loanId ORDER BY createdAt DESC")
    fun observePaymentsByLoanId(loanId: String): Flow<List<PaymentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayments(payments: List<PaymentEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: PaymentEntity)

    @Query("DELETE FROM payments WHERE id = :paymentId")
    suspend fun deletePayment(paymentId: String)

    @Query("DELETE FROM payments")
    suspend fun deleteAllPayments()
}
