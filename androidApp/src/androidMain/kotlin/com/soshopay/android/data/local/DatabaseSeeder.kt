package com.soshopay.android.data.local

import com.soshopay.android.data.local.entities.PaymentMethodEntity
import com.soshopay.android.data.local.entities.SyncMetadataEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DatabaseSeeder(
    private val database: SoshoPayDatabase,
) {
    suspend fun seedInitialData() =
        withContext(Dispatchers.IO) {
            // Seed any initial data if needed
            seedPaymentMethods()
            seedSyncMetadata()
        }

    private suspend fun seedPaymentMethods() {
        val existingMethods = database.paymentDashboardDao().getPaymentMethods()
        if (existingMethods.isEmpty()) {
            val defaultMethods =
                listOf(
                    PaymentMethodEntity(
                        id = "ecocash",
                        name = "EcoCash",
                        type = "ECOCASH",
                        isActive = true,
                        description = "Pay using your EcoCash mobile wallet",
                        processingTime = "2-5 minutes",
                        minimumAmount = 1.0,
                        maximumAmount = 5000.0,
                        fees = 0.0,
                        createdAt = System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis(),
                    ),
                )
            database.paymentDashboardDao().insertPaymentMethods(defaultMethods)
        }
    }

    private suspend fun seedSyncMetadata() {
        // Initialize sync metadata with default values
        val defaultSyncTypes =
            listOf(
                "loans",
                "payments",
                "dashboard",
                "form_data",
                "products",
                "methods",
            )

        defaultSyncTypes.forEach { syncType ->
            val existing = database.syncMetadataDao().getLastSyncTime(syncType)
            if (existing == null) {
                database.syncMetadataDao().insertOrUpdate(
                    SyncMetadataEntity(
                        syncType = syncType,
                        lastSyncTime = 0L,
                        updatedAt = System.currentTimeMillis(),
                    ),
                )
            }
        }
    }

    suspend fun clearAllData() =
        withContext(Dispatchers.IO) {
            database.clearAllTables()
        }
}
