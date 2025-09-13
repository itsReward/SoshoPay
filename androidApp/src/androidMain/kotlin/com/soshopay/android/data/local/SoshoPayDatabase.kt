// File: androidApp/src/androidMain/kotlin/com/soshopay/android/data/local/SoshoPayDatabase.kt
package com.soshopay.android.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.soshopay.android.data.local.dao.DraftApplicationDao
import com.soshopay.android.data.local.dao.FormDataDao
import com.soshopay.android.data.local.dao.LoanDao
import com.soshopay.android.data.local.dao.PaymentDao
import com.soshopay.android.data.local.dao.PaymentDashboardDao
import com.soshopay.android.data.local.dao.PaymentReceiptDao
import com.soshopay.android.data.local.dao.PaymentScheduleDao
import com.soshopay.android.data.local.dao.SyncMetadataDao
import com.soshopay.android.data.local.entities.CashLoanFormDataEntity
import com.soshopay.android.data.local.entities.DraftCashLoanEntity
import com.soshopay.android.data.local.entities.DraftPayGoLoanEntity
import com.soshopay.android.data.local.entities.EarlyPayoffCalculationEntity
import com.soshopay.android.data.local.entities.LoanDetailsEntity
import com.soshopay.android.data.local.entities.LoanEntity
import com.soshopay.android.data.local.entities.PayGoCategoriesEntity
import com.soshopay.android.data.local.entities.PayGoProductEntity
import com.soshopay.android.data.local.entities.PaymentDashboardEntity
import com.soshopay.android.data.local.entities.PaymentEntity
import com.soshopay.android.data.local.entities.PaymentMethodEntity
import com.soshopay.android.data.local.entities.PaymentReceiptEntity
import com.soshopay.android.data.local.entities.PaymentScheduleEntity
import com.soshopay.android.data.local.entities.SyncMetadataEntity

@Database(
    entities = [
        // Loan entities
        LoanEntity::class,
        LoanDetailsEntity::class,
        DraftCashLoanEntity::class,
        DraftPayGoLoanEntity::class,
        PayGoProductEntity::class,
        CashLoanFormDataEntity::class,
        PayGoCategoriesEntity::class,
        // Payment entities
        PaymentEntity::class,
        PaymentDashboardEntity::class,
        PaymentMethodEntity::class,
        PaymentScheduleEntity::class,
        PaymentReceiptEntity::class,
        EarlyPayoffCalculationEntity::class,
        // Shared entities
        SyncMetadataEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
@TypeConverters(DatabaseConverters::class)
abstract class SoshoPayDatabase : RoomDatabase() {
    // Loan DAOs
    abstract fun loanDao(): LoanDao

    abstract fun draftApplicationDao(): DraftApplicationDao

    abstract fun formDataDao(): FormDataDao

    // Payment DAOs
    abstract fun paymentDao(): PaymentDao

    abstract fun paymentDashboardDao(): PaymentDashboardDao

    abstract fun paymentScheduleDao(): PaymentScheduleDao

    abstract fun paymentReceiptDao(): PaymentReceiptDao

    // Shared DAOs
    abstract fun syncMetadataDao(): SyncMetadataDao

    companion object {
        private const val DATABASE_NAME = "soshopay_database"

        @Volatile
        private var INSTANCE: SoshoPayDatabase? = null

        fun getInstance(context: Context): SoshoPayDatabase =
            INSTANCE ?: synchronized(this) {
                val instance =
                    Room
                        .databaseBuilder(
                            context.applicationContext,
                            SoshoPayDatabase::class.java,
                            DATABASE_NAME,
                        ).fallbackToDestructiveMigration() // For development - remove in production
                        .build()
                INSTANCE = instance
                instance
            }

        // For testing purposes
        fun getInMemoryDatabase(context: Context): SoshoPayDatabase =
            Room
                .inMemoryDatabaseBuilder(
                    context.applicationContext,
                    SoshoPayDatabase::class.java,
                ).build()
    }
}
