package com.soshopay.android.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.soshopay.android.data.local.entities.CashLoanFormDataEntity
import com.soshopay.android.data.local.entities.PayGoCategoriesEntity
import com.soshopay.android.data.local.entities.PayGoProductEntity

@Dao
interface FormDataDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCashLoanFormData(formData: CashLoanFormDataEntity)

    @Query("SELECT * FROM cash_loan_form_data WHERE id = 'cash_loan_form_data'")
    suspend fun getCashLoanFormData(): CashLoanFormDataEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayGoCategories(categories: PayGoCategoriesEntity)

    @Query("SELECT * FROM paygo_categories WHERE id = 'paygo_categories'")
    suspend fun getPayGoCategories(): PayGoCategoriesEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayGoProducts(products: List<PayGoProductEntity>)

    @Query("SELECT * FROM paygo_products WHERE category = :category AND isAvailable = 1")
    suspend fun getPayGoProductsByCategory(category: String): List<PayGoProductEntity>

    @Query("SELECT * FROM paygo_products WHERE isAvailable = 1")
    suspend fun getAllPayGoProducts(): List<PayGoProductEntity>

    @Query("DELETE FROM paygo_products")
    suspend fun deleteAllPayGoProducts()
}
