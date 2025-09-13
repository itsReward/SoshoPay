package com.soshopay.android.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.soshopay.android.data.local.entities.PaymentReceiptEntity

@Dao
interface PaymentReceiptDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPaymentReceipt(receipt: PaymentReceiptEntity)

    @Query("SELECT * FROM payment_receipts WHERE receiptNumber = :receiptNumber")
    suspend fun getPaymentReceiptByNumber(receiptNumber: String): PaymentReceiptEntity?

    @Query("SELECT * FROM payment_receipts ORDER BY processedAt DESC")
    suspend fun getAllPaymentReceipts(): List<PaymentReceiptEntity>

    @Query("DELETE FROM payment_receipts WHERE receiptNumber = :receiptNumber")
    suspend fun deletePaymentReceipt(receiptNumber: String)
}
