package com.soshopay.platform.storage

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.soshopay.domain.model.AuthToken
import com.soshopay.domain.storage.TokenStorage
import com.soshopay.domain.util.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class AndroidTokenStorage(
    private val context: Context,
) : TokenStorage {
    private val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

    private val encryptedSharedPreferences: SharedPreferences by lazy {
        EncryptedSharedPreferences.create(
            "soshopay_secure_prefs",
            masterKeyAlias,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )
    }

    private val json =
        Json {
            ignoreUnknownKeys = true
            encodeDefaults = true
        }

    companion object {
        private const val KEY_AUTH_TOKEN = "auth_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_TOKEN_CREATED_AT = "token_created_at"
    }

    override suspend fun saveAuthToken(token: AuthToken): Boolean =
        withContext(Dispatchers.IO) {
            try {
                val tokenJson = json.encodeToString(token)
                encryptedSharedPreferences
                    .edit()
                    .putString(KEY_AUTH_TOKEN, tokenJson)
                    .putLong(KEY_TOKEN_CREATED_AT, System.currentTimeMillis())
                    .apply()

                Logger.d("Auth token saved successfully", "TOKEN_STORAGE")
                true
            } catch (e: Exception) {
                Logger.e("Failed to save auth token", "TOKEN_STORAGE", e)
                false
            }
        }

    override suspend fun getAuthToken(): AuthToken? =
        withContext(Dispatchers.IO) {
            try {
                val tokenJson = encryptedSharedPreferences.getString(KEY_AUTH_TOKEN, null)
                tokenJson?.let {
                    json.decodeFromString<AuthToken>(it)
                }
            } catch (e: Exception) {
                Logger.e("Failed to retrieve auth token", "TOKEN_STORAGE", e)
                null
            }
        }

    override suspend fun clearAuthToken(): Boolean =
        withContext(Dispatchers.IO) {
            try {
                encryptedSharedPreferences
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

    override suspend fun saveRefreshToken(token: String): Boolean =
        withContext(Dispatchers.IO) {
            try {
                encryptedSharedPreferences
                    .edit()
                    .putString(KEY_REFRESH_TOKEN, token)
                    .apply()

                Logger.d("Refresh token saved successfully", "TOKEN_STORAGE")
                true
            } catch (e: Exception) {
                Logger.e("Failed to save refresh token", "TOKEN_STORAGE", e)
                false
            }
        }

    override suspend fun getRefreshToken(): String? =
        withContext(Dispatchers.IO) {
            try {
                encryptedSharedPreferences.getString(KEY_REFRESH_TOKEN, null)
            } catch (e: Exception) {
                Logger.e("Failed to retrieve refresh token", "TOKEN_STORAGE", e)
                null
            }
        }

    override suspend fun clearAllTokens(): Boolean =
        withContext(Dispatchers.IO) {
            try {
                encryptedSharedPreferences
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

    override suspend fun isTokenValid(): Boolean =
        withContext(Dispatchers.IO) {
            try {
                val token = getAuthToken()
                val createdAt = encryptedSharedPreferences.getLong(KEY_TOKEN_CREATED_AT, 0)

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
