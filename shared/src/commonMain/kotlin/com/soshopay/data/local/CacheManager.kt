package com.soshopay.data.local

// ========== CACHE MANAGER ==========
interface CacheManager {
    suspend fun clearAllCache()

    suspend fun clearLoanCache()

    suspend fun clearPaymentCache()

    suspend fun getCacheSize(): Long

    suspend fun isCacheExpired(cacheType: String): Boolean

    suspend fun setCacheExpiry(
        cacheType: String,
        expiryTime: Long,
    )

    // Cache types constants
    companion object {
        const val CACHE_LOANS = "loans"
        const val CACHE_PAYMENTS = "payments"
        const val CACHE_DASHBOARD = "dashboard"
        const val CACHE_FORM_DATA = "form_data"
        const val CACHE_PRODUCTS = "products"
        const val CACHE_METHODS = "methods"

        // Sync intervals (in milliseconds)
        const val SYNC_INTERVAL_LOANS = 15 * 60 * 1000L // 15 minutes
        const val SYNC_INTERVAL_PAYMENTS = 5 * 60 * 1000L // 5 minutes
        const val SYNC_INTERVAL_DASHBOARD = 2 * 60 * 1000L // 2 minutes
        const val SYNC_INTERVAL_FORM_DATA = 60 * 60 * 1000L // 1 hour
    }
}
