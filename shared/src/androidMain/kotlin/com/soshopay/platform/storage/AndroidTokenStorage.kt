package com.soshopay.platform.storage

import android.content.Context
import android.content.SharedPreferences
import com.soshopay.domain.model.AuthToken
import com.soshopay.domain.storage.TokenStorage
import com.soshopay.domain.util.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * AndroidTokenStorage securely manages authentication and refresh tokens using Android's SharedPreferences
 * and the Keystore system. It implements [TokenStorage] for Android, providing methods to save, retrieve,
 * validate, and clear tokens. All operations are performed on the IO dispatcher to avoid blocking the main thread.
 *
 * Features:
 * - Stores tokens in encrypted form using [AndroidKeystoreHelper]
 * - Persists tokens and their creation time in SharedPreferences
 * - Handles both authentication and refresh tokens
 * - Validates token expiration based on creation time and expiry duration
 * - Provides methods to clear tokens individually or all at once
 *
 * @property context Android Context used to access SharedPreferences
 */
class AndroidTokenStorage(
    private val context: Context,
) : TokenStorage {
    /** SharedPreferences instance for secure storage */
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(
            "soshopay_secure_prefs",
            Context.MODE_PRIVATE,
        )

    /** JSON serializer/deserializer for AuthToken objects */
    private val json =
        Json {
            ignoreUnknownKeys = true
            encodeDefaults = true
        }

    /** Helper for encryption/decryption using Android Keystore */
    private val keystoreHelper = AndroidKeystoreHelper()

    companion object {
        /** Key for storing the encrypted auth token */
        private const val KEY_AUTH_TOKEN = "auth_token"

        /** Key for storing the encrypted refresh token */
        private const val KEY_REFRESH_TOKEN = "refresh_token"

        /** Key for storing the token creation timestamp */
        private const val KEY_TOKEN_CREATED_AT = "token_created_at"
    }

    /**
     * Saves an authentication token securely.
     * @param token The [AuthToken] to save
     * @return true if successful, false otherwise
     */
    override suspend fun saveAuthToken(token: AuthToken): Boolean =
        withContext(Dispatchers.IO) {
            try {
                val tokenJson = json.encodeToString(token)
                val encryptedData = keystoreHelper.encrypt(tokenJson)

                sharedPreferences
                    .edit()
                    .putString(KEY_AUTH_TOKEN, encryptedData)
                    .putLong(KEY_TOKEN_CREATED_AT, System.currentTimeMillis())
                    .apply()

                Logger.d("Auth token saved successfully", "TOKEN_STORAGE")
                true
            } catch (e: Exception) {
                Logger.e("Failed to save auth token", "TOKEN_STORAGE", e)
                false
            }
        }

    /**
     * Retrieves the stored authentication token, if available.
     * @return The [AuthToken] or null if not found or on error
     */
    override suspend fun getAuthToken(): AuthToken? =
        withContext(Dispatchers.IO) {
            try {
                val encryptedData = sharedPreferences.getString(KEY_AUTH_TOKEN, null)
                encryptedData?.let {
                    val tokenJson = keystoreHelper.decrypt(it)
                    json.decodeFromString<AuthToken>(tokenJson)
                }
            } catch (e: Exception) {
                Logger.e("Failed to retrieve auth token", "TOKEN_STORAGE", e)
                null
            }
        }

    /**
     * Clears the stored authentication token and its creation time.
     * @return true if successful, false otherwise
     */
    override suspend fun clearAuthToken(): Boolean =
        withContext(Dispatchers.IO) {
            try {
                sharedPreferences
                    .edit()
                    .remove(KEY_AUTH_TOKEN)
                    .remove(KEY_TOKEN_CREATED_AT)
                    .apply()

                Logger.d("Auth token cleared successfully", "TOKEN_STORAGE")
                true
            } catch (e: Exception) {
                Logger.e("Failed to clear auth token", "TOKEN_STORAGE", e)
                false
            }
        }

    /**
     * Saves a refresh token securely.
     * @param token The refresh token string to save
     * @return true if successful, false otherwise
     */
    override suspend fun saveRefreshToken(token: String): Boolean =
        withContext(Dispatchers.IO) {
            try {
                val encryptedData = keystoreHelper.encrypt(token)
                sharedPreferences
                    .edit()
                    .putString(KEY_REFRESH_TOKEN, encryptedData)
                    .apply()

                Logger.d("Refresh token saved successfully", "TOKEN_STORAGE")
                true
            } catch (e: Exception) {
                Logger.e("Failed to save refresh token", "TOKEN_STORAGE", e)
                false
            }
        }

    /**
     * Retrieves the stored refresh token, if available.
     * @return The refresh token string or null if not found or on error
     */
    override suspend fun getRefreshToken(): String? =
        withContext(Dispatchers.IO) {
            try {
                val encryptedData = sharedPreferences.getString(KEY_REFRESH_TOKEN, null)
                encryptedData?.let {
                    keystoreHelper.decrypt(it)
                }
            } catch (e: Exception) {
                Logger.e("Failed to retrieve refresh token", "TOKEN_STORAGE", e)
                null
            }
        }

    /**
     * Clears all stored tokens and their metadata.
     * @return true if successful, false otherwise
     */
    override suspend fun clearAllTokens(): Boolean =
        withContext(Dispatchers.IO) {
            try {
                sharedPreferences
                    .edit()
                    .remove(KEY_AUTH_TOKEN)
                    .remove(KEY_REFRESH_TOKEN)
                    .remove(KEY_TOKEN_CREATED_AT)
                    .apply()

                Logger.d("All tokens cleared successfully", "TOKEN_STORAGE")
                true
            } catch (e: Exception) {
                Logger.e("Failed to clear all tokens", "TOKEN_STORAGE", e)
                false
            }
        }

    /**
     * Checks if the stored authentication token is still valid based on its creation time and expiration period.
     * @return true if the token exists and hasn't expired, false otherwise
     */
    override suspend fun isTokenValid(): Boolean =
        withContext(Dispatchers.IO) {
            try {
                val token = getAuthToken()
                val createdAt = sharedPreferences.getLong(KEY_TOKEN_CREATED_AT, 0)

                if (token == null || createdAt == 0L) return@withContext false

                val currentTime = System.currentTimeMillis()
                val tokenAge = currentTime - createdAt
                val isExpired = tokenAge >= token.expiresIn * 1000

                !isExpired
            } catch (e: Exception) {
                Logger.e("Failed to check token validity", "TOKEN_STORAGE", e)
                false
            }
        }
}
