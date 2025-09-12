package com.soshopay.android.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.soshopay.android.data.local.entities.EarlyPayoffCalculationEntity
import com.soshopay.android.data.local.entities.PaymentDashboardEntity
import com.soshopay.android.data.local.entities.PaymentMethodEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PaymentDashboardDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPaymentDashboard(dashboard: PaymentDashboardEntity)

    @Query("SELECT * FROM payment_dashboard WHERE id = 'payment_dashboard'")
    suspend fun getPaymentDashboard(): PaymentDashboardEntity?

    @Query("SELECT * FROM payment_dashboard WHERE id = 'payment_dashboard'")
    fun observePaymentDashboard(): Flow<PaymentDashboardEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPaymentMethods(methods: List<PaymentMethodEntity>)

    @Query("SELECT * FROM payment_methods WHERE isActive = 1 ORDER BY name ASC")
    suspend fun getPaymentMethods(): List<PaymentMethodEntity>

    @Query("DELETE FROM payment_methods")
    suspend fun deleteAllPaymentMethods()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEarlyPayoffCalculation(calculation: EarlyPayoffCalculationEntity)

    @Query("SELECT * FROM early_payoff_calculations WHERE loanId = :loanId")
    suspend fun getEarlyPayoffCalculation(loanId: String): EarlyPayoffCalculationEntity?

    @Query("DELETE FROM early_payoff_calculations WHERE loanId = :loanId")
    suspend fun deleteEarlyPayoffCalculation(loanId: String)
}
