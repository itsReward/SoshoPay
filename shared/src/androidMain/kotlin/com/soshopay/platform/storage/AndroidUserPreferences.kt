package com.soshopay.platform.storage

import android.content.Context
import android.content.SharedPreferences
import com.soshopay.domain.storage.UserPreferences
import com.soshopay.domain.util.DeviceUtils
import com.soshopay.domain.util.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

class AndroidUserPreferences(
    private val context: Context,
) : UserPreferences {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(
            "soshopay_user_prefs",
            Context.MODE_PRIVATE,
        )

    private val _preferencesFlow = MutableStateFlow<Map<String, String>>(emptyMap())

    companion object {
        private const val KEY_BIOMETRIC_ENABLED = "biometric_enabled"
        private const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"
        private const val KEY_LAST_SYNC_TIME = "last_sync_time"
        private const val KEY_DEVICE_ID = "device_id"
    }

    init {
        // Initialize flow with current preferences
        _preferencesFlow.value = getAllPreferences()

        // Listen for preference changes
        sharedPreferences.registerOnSharedPreferenceChangeListener { _, _ ->
            _preferencesFlow.value = getAllPreferences()
        }
    }

    private fun getAllPreferences(): Map<String, String> =
        sharedPreferences.all
            .mapNotNull { (key, value) ->
                if (value is String) key to value else null
            }.toMap()

    override suspend fun saveUserPreference(
        key: String,
        value: String,
    ): Boolean =
        withContext(Dispatchers.IO) {
            try {
                sharedPreferences.edit().putString(key, value).apply()
                Logger.d("User preference saved: $key", "USER_PREFS")
                true
            } catch (e: Exception) {
                Logger.e("Failed to save user preference: $key", "USER_PREFS", e)
                false
            }
        }

    override suspend fun getUserPreference(key: String): String? =
        withContext(Dispatchers.IO) {
            try {
                sharedPreferences.getString(key, null)
            } catch (e: Exception) {
                Logger.e("Failed to get user preference: $key", "USER_PREFS", e)
                null
            }
        }

    override suspend fun removeUserPreference(key: String): Boolean =
        withContext(Dispatchers.IO) {
            try {
                sharedPreferences.edit().remove(key).apply()
                Logger.d("User preference removed: $key", "USER_PREFS")
                true
            } catch (e: Exception) {
                Logger.e("Failed to remove user preference: $key", "USER_PREFS", e)
                false
            }
        }

    override suspend fun clearAllPreferences(): Boolean =
        withContext(Dispatchers.IO) {
            try {
                sharedPreferences.edit().clear().apply()
                Logger.d("All user preferences cleared", "USER_PREFS")
                true
            } catch (e: Exception) {
                Logger.e("Failed to clear all preferences", "USER_PREFS", e)
                false
            }
        }

    override fun observePreferences(): Flow<Map<String, String>> = _preferencesFlow.asStateFlow()

    override suspend fun setBiometricEnabled(enabled: Boolean): Boolean = saveUserPreference(KEY_BIOMETRIC_ENABLED, enabled.toString())

    override suspend fun isBiometricEnabled(): Boolean = getUserPreference(KEY_BIOMETRIC_ENABLED)?.toBooleanStrictOrNull() ?: false

    override suspend fun setNotificationsEnabled(enabled: Boolean): Boolean =
        saveUserPreference(KEY_NOTIFICATIONS_ENABLED, enabled.toString())

    override suspend fun isNotificationsEnabled(): Boolean = getUserPreference(KEY_NOTIFICATIONS_ENABLED)?.toBooleanStrictOrNull() ?: true

    override suspend fun setLastSyncTime(timestamp: Long): Boolean = saveUserPreference(KEY_LAST_SYNC_TIME, timestamp.toString())

    override suspend fun getLastSyncTime(): Long = getUserPreference(KEY_LAST_SYNC_TIME)?.toLongOrNull() ?: 0L

    override suspend fun setDeviceId(deviceId: String): Boolean = saveUserPreference(KEY_DEVICE_ID, deviceId)

    override suspend fun getDeviceId(): String? = getUserPreference(KEY_DEVICE_ID)
}
