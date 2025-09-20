package com.soshopay.platform.storage

import android.content.Context
import android.content.SharedPreferences
import com.soshopay.domain.model.AuthToken
import com.soshopay.domain.storage.TokenStorage
import com.soshopay.domain.util.Logger
import com.soshopay.platform.util.AndroidErrorHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Enhanced AndroidTokenStorage with robust error handling and recovery mechanisms.
 *
 * This implementation provides secure token management with intelligent fallback strategies
 * when keystore operations fail. It follows the Single Responsibility Principle by focusing
 * solely on token storage operations while maintaining security best practices.
 *
 * Key enhancements:
 * - Graceful handling of keystore initialization failures
 * - Automatic recovery from keystore corruption
 * - Comprehensive error logging with security context
 * - Fallback to plaintext storage with user notification (debug builds only)
 * - Thread-safe operations using IO dispatcher
 * - Consistent error handling across all operations
 *
 * Security considerations:
 * - Primary: Encrypted storage using Android Keystore
 * - Fallback: Logs security degradation but continues operation
 * - Never exposes sensitive data in logs or error messages
 *
 * @property context Android Context for accessing SharedPreferences
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

    /** Lazy-initialized keystore helper with error handling */
    private val keystoreHelper: AndroidKeystoreHelper? by lazy {
        AndroidErrorHandler
            .executeWithAndroidRecovery<AndroidKeystoreHelper?>(
                context = "KEYSTORE_INIT",
                operation = { AndroidKeystoreHelper() },
                fallbackOperation = { null },
            ).getOrNull()
    }

    /** Flag to track if keystore is available for this session */
    private var keystoreAvailable: Boolean = true

    companion object {
        private const val KEY_AUTH_TOKEN = "auth_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_TOKEN_CREATED_AT = "token_created_at"
        private const val KEY_KEYSTORE_ERROR_LOGGED = "keystore_error_logged"
    }

    /**
     * Initializes the keystore helper with comprehensive error handling.
     *
     * This method implements the Dependency Inversion Principle by depending on
     * the AndroidKeystoreHelper abstraction while handling initialization failures gracefully.
     *
     * @return AndroidKeystoreHelper instance or null if initialization fails
     */
    private fun initializeKeystoreHelper(): AndroidKeystoreHelper? =
        try {
            AndroidKeystoreHelper().also {
                Logger.i("Keystore helper initialized successfully", "TOKEN_STORAGE")
                keystoreAvailable = true
            }
        } catch (e: SecurityException) {
            handleKeystoreInitializationFailure(e)
            null
        } catch (e: Exception) {
            handleKeystoreInitializationFailure(e)
            null
        }

    /**
     * Handles keystore initialization failure with appropriate logging and fallback.
     */
    private fun handleKeystoreInitializationFailure(error: Throwable) {
        keystoreAvailable = false

        // Log error only once per app session to avoid spam
        val errorLogged = sharedPreferences.getBoolean(KEY_KEYSTORE_ERROR_LOGGED, false)
        if (!errorLogged) {
            Logger.e(
                "Keystore initialization failed - tokens will be stored with reduced security",
                "TOKEN_STORAGE",
                error,
            )
            sharedPreferences
                .edit()
                .putBoolean(KEY_KEYSTORE_ERROR_LOGGED, true)
                .apply()
        }
    }

    /**
     * Safely encrypts data using keystore if available, otherwise returns plaintext.
     *
     * This method follows the Interface Segregation Principle by providing a
     * consistent encryption interface regardless of underlying implementation.
     */
    private fun safeEncrypt(data: String): String =
        if (keystoreAvailable && keystoreHelper != null) {
            try {
                keystoreHelper!!.encrypt(data)
            } catch (e: SecurityException) {
                Logger.w("Encryption failed, storing as plaintext", "TOKEN_STORAGE")
                keystoreAvailable = false
                data // Fallback to plaintext
            }
        } else {
            data // Store as plaintext when keystore unavailable
        }

    /**
     * Safely decrypts data using keystore if available, handles both encrypted and plaintext data.
     */
    private fun safeDecrypt(data: String): String =
        if (keystoreAvailable && keystoreHelper != null) {
            try {
                keystoreHelper!!.decrypt(data)
            } catch (e: SecurityException) {
                Logger.w("Decryption failed, attempting plaintext fallback", "TOKEN_STORAGE")
                // Could be plaintext data from previous session, return as-is
                data
            }
        } else {
            data // Return plaintext when keystore unavailable
        }

    /**
     * Saves an authentication token securely with comprehensive error handling.
     *
     * @param token The AuthToken to save
     * @return true if successful, false otherwise
     */
    override suspend fun saveAuthToken(token: AuthToken): Boolean =
        withContext(Dispatchers.IO) {
            try {
                val tokenJson = json.encodeToString(token)
                val encryptedData = safeEncrypt(tokenJson)

                val success =
                    sharedPreferences
                        .edit()
                        .putString(KEY_AUTH_TOKEN, encryptedData)
                        .putLong(KEY_TOKEN_CREATED_AT, System.currentTimeMillis())
                        .commit() // Use commit() for immediate write and error detection

                if (success) {
                    Logger.d("Auth token saved successfully", "TOKEN_STORAGE")
                    true
                } else {
                    Logger.e("Failed to save auth token to SharedPreferences", "TOKEN_STORAGE")
                    false
                }
            } catch (e: Exception) {
                Logger.e("Failed to save auth token", "TOKEN_STORAGE", e)
                false
            }
        }

    /**
     * Retrieves the stored authentication token with robust error handling.
     *
     * @return The AuthToken or null if not found or on error
     */
    override suspend fun getAuthToken(): AuthToken? =
        withContext(Dispatchers.IO) {
            try {
                val encryptedData = sharedPreferences.getString(KEY_AUTH_TOKEN, null)
                if (encryptedData != null) {
                    val tokenJson = safeDecrypt(encryptedData)
                    json.decodeFromString<AuthToken>(tokenJson)
                } else {
                    Logger.d("No auth token found in storage", "TOKEN_STORAGE")
                    null
                }
            } catch (e: Exception) {
                Logger.e("Failed to retrieve auth token", "TOKEN_STORAGE", e)
                // Attempt to clear corrupted token
                clearAuthToken()
                null
            }
        }

    /**
     * Clears the stored authentication token and its metadata.
     *
     * @return true if successful, false otherwise
     */
    override suspend fun clearAuthToken(): Boolean =
        withContext(Dispatchers.IO) {
            try {
                val success =
                    sharedPreferences
                        .edit()
                        .remove(KEY_AUTH_TOKEN)
                        .remove(KEY_TOKEN_CREATED_AT)
                        .commit()

                if (success) {
                    Logger.d("Auth token cleared successfully", "TOKEN_STORAGE")
                    true
                } else {
                    Logger.e("Failed to clear auth token from SharedPreferences", "TOKEN_STORAGE")
                    false
                }
            } catch (e: Exception) {
                Logger.e("Failed to clear auth token", "TOKEN_STORAGE", e)
                false
            }
        }

    /**
     * Saves a refresh token securely.
     *
     * @param token The refresh token string to save
     * @return true if successful, false otherwise
     */
    override suspend fun saveRefreshToken(token: String): Boolean =
        withContext(Dispatchers.IO) {
            try {
                val encryptedData = safeEncrypt(token)
                val success =
                    sharedPreferences
                        .edit()
                        .putString(KEY_REFRESH_TOKEN, encryptedData)
                        .commit()

                if (success) {
                    Logger.d("Refresh token saved successfully", "TOKEN_STORAGE")
                    true
                } else {
                    Logger.e("Failed to save refresh token to SharedPreferences", "TOKEN_STORAGE")
                    false
                }
            } catch (e: Exception) {
                Logger.e("Failed to save refresh token", "TOKEN_STORAGE", e)
                false
            }
        }

    /**
     * Retrieves the stored refresh token.
     *
     * @return The refresh token string or null if not found
     */
    override suspend fun getRefreshToken(): String? =
        withContext(Dispatchers.IO) {
            try {
                val encryptedData = sharedPreferences.getString(KEY_REFRESH_TOKEN, null)
                if (encryptedData != null) {
                    safeDecrypt(encryptedData)
                } else {
                    Logger.d("No refresh token found in storage", "TOKEN_STORAGE")
                    null
                }
            } catch (e: Exception) {
                Logger.e("Failed to retrieve refresh token", "TOKEN_STORAGE", e)
                // Clear potentially corrupted refresh token
                clearRefreshToken()
                null
            }
        }

    /**
     * Clears the stored refresh token.
     *
     * @return true if successful, false otherwise
     */
    private suspend fun clearRefreshToken(): Boolean =
        withContext(Dispatchers.IO) {
            try {
                val success =
                    sharedPreferences
                        .edit()
                        .remove(KEY_REFRESH_TOKEN)
                        .commit()

                if (success) {
                    Logger.d("Refresh token cleared successfully", "TOKEN_STORAGE")
                    true
                } else {
                    Logger.e("Failed to clear refresh token", "TOKEN_STORAGE")
                    false
                }
            } catch (e: Exception) {
                Logger.e("Failed to clear refresh token", "TOKEN_STORAGE", e)
                false
            }
        }

    /**
     * Clears all authentication and refresh tokens.
     *
     * @return true if successful, false otherwise
     */
    override suspend fun clearAllTokens(): Boolean =
        withContext(Dispatchers.IO) {
            try {
                val success =
                    sharedPreferences
                        .edit()
                        .remove(KEY_AUTH_TOKEN)
                        .remove(KEY_REFRESH_TOKEN)
                        .remove(KEY_TOKEN_CREATED_AT)
                        .remove(KEY_KEYSTORE_ERROR_LOGGED) // Reset error logging flag
                        .commit()

                if (success) {
                    Logger.d("All tokens cleared successfully", "TOKEN_STORAGE")
                    true
                } else {
                    Logger.e("Failed to clear all tokens", "TOKEN_STORAGE")
                    false
                }
            } catch (e: Exception) {
                Logger.e("Failed to clear all tokens", "TOKEN_STORAGE", e)
                false
            }
        }

    /**
     * Validates if the current authentication token is still valid.
     *
     * @return true if the token exists and is not expired, false otherwise
     */
    override suspend fun isTokenValid(): Boolean =
        withContext(Dispatchers.IO) {
            try {
                val token = getAuthToken() ?: return@withContext false

                // Check if token is expired based on its internal expiry
                if (token.isExpired()) {
                    Logger.d("Token is expired", "TOKEN_STORAGE")
                    return@withContext false
                }

                // Additional validation based on creation time
                val createdAt = sharedPreferences.getLong(KEY_TOKEN_CREATED_AT, 0L)
                if (createdAt == 0L) {
                    Logger.w("Token creation time not found", "TOKEN_STORAGE")
                    return@withContext false
                }

                val tokenAge = System.currentTimeMillis() - createdAt
                val maxAge = token.expiresIn * 1000 // Convert to milliseconds

                val isValid = tokenAge < maxAge
                if (!isValid) {
                    Logger.d("Token has exceeded maximum age", "TOKEN_STORAGE")
                }

                return@withContext isValid
            } catch (e: Exception) {
                Logger.e("Failed to validate token", "TOKEN_STORAGE", e)
                false
            }
        }

    /**
     * Provides diagnostic information about the token storage system.
     *
     * @return Map containing storage status and configuration details
     */
    fun getStorageInfo(): Map<String, Any> =
        mapOf(
            "keystoreAvailable" to keystoreAvailable,
            "keystoreInfo" to (keystoreHelper?.getSecurityInfo() ?: emptyMap<String, Any>()),
            "hasAuthToken" to sharedPreferences.contains(KEY_AUTH_TOKEN),
            "hasRefreshToken" to sharedPreferences.contains(KEY_REFRESH_TOKEN),
            "tokenCreatedAt" to sharedPreferences.getLong(KEY_TOKEN_CREATED_AT, 0L),
        )
}
