package com.soshopay.domain.storage

import kotlinx.coroutines.flow.Flow

/**
 * Abstraction for managing user preferences in the SoshoPay domain.
 *
 * Implementations of this interface provide methods to persist, retrieve, observe, and clear user preferences,
 * supporting both generic key-value pairs and convenience methods for common settings such as biometrics,
 * notifications, last sync time, and device ID. This enables flexible and efficient management of user-specific
 * settings and supports reactive updates via [Flow].
 *
 * Usage examples:
 * - Use [saveUserPreference] and [getUserPreference] for custom key-value preferences.
 * - Use [setBiometricEnabled] and [isBiometricEnabled] for biometric authentication settings.
 * - Use [setNotificationsEnabled] and [isNotificationsEnabled] for notification preferences.
 * - Use [setLastSyncTime] and [getLastSyncTime] to track data synchronization.
 * - Use [setDeviceId] and [getDeviceId] to persist device identifiers.
 * - Use [observePreferences] to reactively observe all preference changes.
 */
interface UserPreferences {
    /**
     * Saves a user preference as a key-value pair.
     * @param key The preference key.
     * @param value The preference value.
     * @return True if the preference was saved successfully, false otherwise.
     */
    suspend fun saveUserPreference(
        key: String,
        value: String,
    ): Boolean

    /**
     * Retrieves the value for the specified preference [key].
     * @param key The preference key.
     * @return The preference value if present, or null if not found.
     */
    suspend fun getUserPreference(key: String): String?

    /**
     * Removes the specified preference [key] from storage.
     * @param key The preference key to remove.
     * @return True if the preference was removed successfully, false otherwise.
     */
    suspend fun removeUserPreference(key: String): Boolean

    /**
     * Clears all user preferences from storage.
     * @return True if all preferences were cleared successfully, false otherwise.
     */
    suspend fun clearAllPreferences(): Boolean

    /**
     * Observes all user preferences as a [Flow] of key-value pairs.
     * @return A [Flow] emitting the current preferences map whenever changes occur.
     */
    fun observePreferences(): Flow<Map<String, String>>

    /**
     * Enables or disables biometric authentication for the user.
     * @param enabled True to enable biometrics, false to disable.
     * @return True if the preference was saved successfully, false otherwise.
     */
    suspend fun setBiometricEnabled(enabled: Boolean): Boolean

    /**
     * Checks if biometric authentication is enabled for the user.
     * @return True if biometrics are enabled, false otherwise.
     */
    suspend fun isBiometricEnabled(): Boolean

    /**
     * Enables or disables notifications for the user.
     * @param enabled True to enable notifications, false to disable.
     * @return True if the preference was saved successfully, false otherwise.
     */
    suspend fun setNotificationsEnabled(enabled: Boolean): Boolean

    /**
     * Checks if notifications are enabled for the user.
     * @return True if notifications are enabled, false otherwise.
     */
    suspend fun isNotificationsEnabled(): Boolean

    /**
     * Sets the last sync timestamp for user data.
     * @param timestamp The time of the last sync (in milliseconds since epoch).
     * @return True if the timestamp was saved successfully, false otherwise.
     */
    suspend fun setLastSyncTime(timestamp: Long): Boolean

    /**
     * Retrieves the last sync timestamp for user data.
     * @return The timestamp of the last sync (in milliseconds since epoch).
     */
    suspend fun getLastSyncTime(): Long

    /**
     * Sets the device ID associated with the user.
     * @param deviceId The device identifier to save.
     * @return True if the device ID was saved successfully, false otherwise.
     */
    suspend fun setDeviceId(deviceId: String): Boolean

    /**
     * Retrieves the device ID associated with the user.
     * @return The device ID if present, or null if not found.
     */
    suspend fun getDeviceId(): String?
}
