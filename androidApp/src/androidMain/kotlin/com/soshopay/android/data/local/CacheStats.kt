package com.soshopay.android.data.local

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ========== CACHE STATISTICS DATA CLASS ==========
data class CacheStats(
    val cacheType: String,
    val isEnabled: Boolean,
    val lastUpdated: Long,
    val expiryTime: Long,
    val version: Int,
    val isExpired: Boolean,
    val isStale: Boolean,
    val sizeBytes: Long,
) {
    val lastUpdatedFormatted: String
        get() =
            if (lastUpdated > 0) {
                SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                    .format(Date(lastUpdated))
            } else {
                "Never"
            }

    val expiryFormatted: String
        get() =
            if (expiryTime > 0) {
                SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                    .format(Date(expiryTime))
            } else {
                "No expiry"
            }

    val sizeMB: Double
        get() = sizeBytes / (1024.0 * 1024.0)

    val status: String
        get() =
            when {
                !isEnabled -> "Disabled"
                isExpired -> "Expired"
                isStale -> "Stale"
                else -> "Valid"
            }
}
