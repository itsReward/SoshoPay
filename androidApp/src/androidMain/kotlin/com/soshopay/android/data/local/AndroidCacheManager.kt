package com.soshopay.android.data.local

import android.content.Context
import android.content.SharedPreferences
import com.soshopay.data.local.CacheManager
import java.io.File

class AndroidCacheManager(
    context: Context,
) : CacheManager {
    private val sharedPrefs: SharedPreferences =
        context.getSharedPreferences(
            CACHE_PREFS_NAME,
            Context.MODE_PRIVATE,
        )

    private val cacheDir: File = File(context.cacheDir, "soshopay_cache")

    init {
        // Ensure cache directory exists
        if (!cacheDir.exists()) {
            cacheDir.mkdirs()
        }
    }

    override suspend fun clearAllCache() {
        // Clear SharedPreferences cache metadata
        sharedPrefs.edit().clear().apply()

        // Clear cache directory
        if (cacheDir.exists() && cacheDir.isDirectory) {
            cacheDir.deleteRecursively()
            cacheDir.mkdirs()
        }
    }

    override suspend fun clearLoanCache() {
        // Clear loan-related cache entries
        val editor = sharedPrefs.edit()

        // Remove all keys that start with loan cache prefixes
        val allKeys = sharedPrefs.all.keys
        allKeys.forEach { key ->
            when {
                key.startsWith(CACHE_PREFIX_LOANS) -> editor.remove(key)
                key.startsWith(CACHE_PREFIX_FORM_DATA) -> editor.remove(key)
                key.startsWith(CACHE_PREFIX_PRODUCTS) -> editor.remove(key)
                key.startsWith("loan_") -> editor.remove(key)
            }
        }

        editor.apply()

        // Clear loan-related files
        clearCacheFiles("loan")
    }

    override suspend fun clearPaymentCache() {
        // Clear payment-related cache entries
        val editor = sharedPrefs.edit()

        // Remove all keys that start with payment cache prefixes
        val allKeys = sharedPrefs.all.keys
        allKeys.forEach { key ->
            when {
                key.startsWith(CACHE_PREFIX_PAYMENTS) -> editor.remove(key)
                key.startsWith(CACHE_PREFIX_DASHBOARD) -> editor.remove(key)
                key.startsWith(CACHE_PREFIX_METHODS) -> editor.remove(key)
                key.startsWith("payment_") -> editor.remove(key)
            }
        }

        editor.apply()

        // Clear payment-related files
        clearCacheFiles("payment")
    }

    override suspend fun getCacheSize(): Long =
        if (cacheDir.exists()) {
            calculateDirectorySize(cacheDir)
        } else {
            0L
        }

    override suspend fun isCacheExpired(cacheType: String): Boolean {
        val expiryTime = getCacheExpiry(cacheType)
        return if (expiryTime > 0) {
            System.currentTimeMillis() > expiryTime
        } else {
            false // No expiry set, cache is valid
        }
    }

    override suspend fun setCacheExpiry(
        cacheType: String,
        expiryTime: Long,
    ) {
        sharedPrefs
            .edit()
            .putLong(getCacheExpiryKey(cacheType), expiryTime)
            .apply()
    }

    // ========== ADDITIONAL UTILITY METHODS ==========

    fun getCacheExpiry(cacheType: String): Long = sharedPrefs.getLong(getCacheExpiryKey(cacheType), 0L)

    fun setCacheTimestamp(
        cacheType: String,
        timestamp: Long = System.currentTimeMillis(),
    ) {
        sharedPrefs
            .edit()
            .putLong(getCacheTimestampKey(cacheType), timestamp)
            .apply()
    }

    fun getCacheTimestamp(cacheType: String): Long = sharedPrefs.getLong(getCacheTimestampKey(cacheType), 0L)

    fun isCacheStale(
        cacheType: String,
        maxAgeMillis: Long,
    ): Boolean {
        val cacheTimestamp = getCacheTimestamp(cacheType)
        return (System.currentTimeMillis() - cacheTimestamp) > maxAgeMillis
    }

    fun setCacheMetadata(
        cacheType: String,
        key: String,
        value: String,
    ) {
        sharedPrefs
            .edit()
            .putString(getCacheMetadataKey(cacheType, key), value)
            .apply()
    }

    fun getCacheMetadata(
        cacheType: String,
        key: String,
        defaultValue: String = "",
    ): String = sharedPrefs.getString(getCacheMetadataKey(cacheType, key), defaultValue) ?: defaultValue

    fun setCacheVersion(
        cacheType: String,
        version: Int,
    ) {
        sharedPrefs
            .edit()
            .putInt(getCacheVersionKey(cacheType), version)
            .apply()
    }

    fun getCacheVersion(cacheType: String): Int = sharedPrefs.getInt(getCacheVersionKey(cacheType), 1)

    fun invalidateCache(cacheType: String) {
        val editor = sharedPrefs.edit()

        // Remove all keys related to this cache type
        val allKeys = sharedPrefs.all.keys
        allKeys.forEach { key ->
            if (key.contains(cacheType)) {
                editor.remove(key)
            }
        }

        editor.apply()

        // Clear related files
        clearCacheFiles(cacheType)
    }

    fun setCacheEnabled(
        cacheType: String,
        enabled: Boolean,
    ) {
        sharedPrefs
            .edit()
            .putBoolean(getCacheEnabledKey(cacheType), enabled)
            .apply()
    }

    fun isCacheEnabled(cacheType: String): Boolean = sharedPrefs.getBoolean(getCacheEnabledKey(cacheType), true)

    // ========== CACHE STATISTICS ==========

    suspend fun getCacheStats(cacheType: String): CacheStats =
        CacheStats(
            cacheType = cacheType,
            isEnabled = isCacheEnabled(cacheType),
            lastUpdated = getCacheTimestamp(cacheType),
            expiryTime = getCacheExpiry(cacheType),
            version = getCacheVersion(cacheType),
            isExpired = isCacheExpired(cacheType),
            isStale = isCacheStale(cacheType, CacheManager.SYNC_INTERVAL_LOANS),
            sizeBytes = getCacheSizeForType(cacheType),
        )

    suspend fun getAllCacheStats(): List<CacheStats> =
        listOf(
            getCacheStats(CacheManager.CACHE_LOANS),
            getCacheStats(CacheManager.CACHE_PAYMENTS),
            getCacheStats(CacheManager.CACHE_DASHBOARD),
            getCacheStats(CacheManager.CACHE_FORM_DATA),
            getCacheStats(CacheManager.CACHE_PRODUCTS),
            getCacheStats(CacheManager.CACHE_METHODS),
        )

    // ========== PRIVATE HELPER METHODS ==========

    private fun clearCacheFiles(cacheType: String) {
        if (cacheDir.exists() && cacheDir.isDirectory) {
            cacheDir.listFiles()?.forEach { file ->
                if (file.name.contains(cacheType, ignoreCase = true)) {
                    file.delete()
                }
            }
        }
    }

    private fun calculateDirectorySize(directory: File): Long {
        var size = 0L
        if (directory.exists() && directory.isDirectory) {
            directory.listFiles()?.forEach { file ->
                size +=
                    if (file.isDirectory) {
                        calculateDirectorySize(file)
                    } else {
                        file.length()
                    }
            }
        }
        return size
    }

    private fun getCacheSizeForType(cacheType: String): Long {
        var size = 0L
        if (cacheDir.exists() && cacheDir.isDirectory) {
            cacheDir.listFiles()?.forEach { file ->
                if (file.name.contains(cacheType, ignoreCase = true)) {
                    size +=
                        if (file.isDirectory) {
                            calculateDirectorySize(file)
                        } else {
                            file.length()
                        }
                }
            }
        }
        return size
    }

    // ========== KEY GENERATION METHODS ==========

    private fun getCacheExpiryKey(cacheType: String): String = "${CACHE_PREFIX_EXPIRY}$cacheType"

    private fun getCacheTimestampKey(cacheType: String): String = "${CACHE_PREFIX_TIMESTAMP}$cacheType"

    private fun getCacheVersionKey(cacheType: String): String = "${CACHE_PREFIX_VERSION}$cacheType"

    private fun getCacheEnabledKey(cacheType: String): String = "${CACHE_PREFIX_ENABLED}$cacheType"

    private fun getCacheMetadataKey(
        cacheType: String,
        key: String,
    ): String = "${CACHE_PREFIX_METADATA}${cacheType}_$key"

    // ========== CONSTANTS ==========

    companion object {
        private const val CACHE_PREFS_NAME = "soshopay_cache_prefs"

        private const val CACHE_PREFIX_EXPIRY = "cache_expiry_"
        private const val CACHE_PREFIX_TIMESTAMP = "cache_timestamp_"
        private const val CACHE_PREFIX_VERSION = "cache_version_"
        private const val CACHE_PREFIX_ENABLED = "cache_enabled_"
        private const val CACHE_PREFIX_METADATA = "cache_metadata_"

        private const val CACHE_PREFIX_LOANS = "loans_"
        private const val CACHE_PREFIX_PAYMENTS = "payments_"
        private const val CACHE_PREFIX_DASHBOARD = "dashboard_"
        private const val CACHE_PREFIX_FORM_DATA = "form_data_"
        private const val CACHE_PREFIX_PRODUCTS = "products_"
        private const val CACHE_PREFIX_METHODS = "methods_"
    }
}
