package com.soshopay.domain.storage

import kotlinx.coroutines.flow.Flow

interface UserPreferences {
    suspend fun saveUserPreference(
        key: String,
        value: String,
    ): Boolean

    suspend fun getUserPreference(key: String): String?

    suspend fun removeUserPreference(key: String): Boolean

    suspend fun clearAllPreferences(): Boolean

    fun observePreferences(): Flow<Map<String, String>>

    // Convenience methods for common preferences
    suspend fun setBiometricEnabled(enabled: Boolean): Boolean

    suspend fun isBiometricEnabled(): Boolean

    suspend fun setNotificationsEnabled(enabled: Boolean): Boolean

    suspend fun isNotificationsEnabled(): Boolean

    suspend fun setLastSyncTime(timestamp: Long): Boolean

    suspend fun getLastSyncTime(): Long

    suspend fun setDeviceId(deviceId: String): Boolean

    suspend fun getDeviceId(): String?
}
