package com.soshopay.platform.storage

import android.content.Context
import com.soshopay.domain.model.User
import com.soshopay.domain.storage.ProfileCache
import com.soshopay.domain.util.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class AndroidProfileCache(
    private val context: Context,
) : ProfileCache {
    private val sharedPreferences =
        context.getSharedPreferences(
            "soshopay_profile_cache",
            Context.MODE_PRIVATE,
        )

    private val json =
        Json {
            ignoreUnknownKeys = true
            encodeDefaults = true
        }

    private val _userFlow = MutableStateFlow<User?>(null)

    companion object {
        private const val KEY_CURRENT_USER = "current_user"
        private const val KEY_LAST_PROFILE_SYNC = "last_profile_sync"
    }

    init {
        // Initialize flow with cached user
        CoroutineScope(Dispatchers.IO).launch {
            _userFlow.value = getCurrentUser()
        }
    }

    override suspend fun saveUser(user: User): Boolean =
        withContext(Dispatchers.IO) {
            try {
                val userJson = json.encodeToString(user)
                sharedPreferences
                    .edit()
                    .putString(KEY_CURRENT_USER, userJson)
                    .apply()

                _userFlow.value = user
                Logger.d("User cached successfully: ${user.id}", "PROFILE_CACHE")
                true
            } catch (e: Exception) {
                Logger.e("Failed to cache user", "PROFILE_CACHE", e)
                false
            }
        }

    override suspend fun getUser(userId: String): User? =
        withContext(Dispatchers.IO) {
            try {
                val currentUser = getCurrentUser()
                if (currentUser?.id == userId) currentUser else null
            } catch (e: Exception) {
                Logger.e("Failed to get cached user", "PROFILE_CACHE", e)
                null
            }
        }

    override suspend fun getCurrentUser(): User? =
        withContext(Dispatchers.IO) {
            try {
                val userJson = sharedPreferences.getString(KEY_CURRENT_USER, null)
                userJson?.let {
                    json.decodeFromString<User>(it)
                }
            } catch (e: Exception) {
                Logger.e("Failed to get current cached user", "PROFILE_CACHE", e)
                null
            }
        }

    override suspend fun updateUser(user: User): Boolean = saveUser(user)

    override suspend fun clearUser(): Boolean =
        withContext(Dispatchers.IO) {
            try {
                sharedPreferences
                    .edit()
                    .remove(KEY_CURRENT_USER)
                    .remove(KEY_LAST_PROFILE_SYNC)
                    .apply()

                _userFlow.value = null
                Logger.d("User cache cleared successfully", "PROFILE_CACHE")
                true
            } catch (e: Exception) {
                Logger.e("Failed to clear user cache", "PROFILE_CACHE", e)
                false
            }
        }

    override fun observeUser(): Flow<User?> = _userFlow.asStateFlow()

    override suspend fun setLastProfileSync(timestamp: Long): Boolean =
        withContext(Dispatchers.IO) {
            try {
                sharedPreferences
                    .edit()
                    .putLong(KEY_LAST_PROFILE_SYNC, timestamp)
                    .apply()
                true
            } catch (e: Exception) {
                Logger.e("Failed to set last profile sync time", "PROFILE_CACHE", e)
                false
            }
        }

    override suspend fun getLastProfileSync(): Long =
        withContext(Dispatchers.IO) {
            try {
                sharedPreferences.getLong(KEY_LAST_PROFILE_SYNC, 0L)
            } catch (e: Exception) {
                Logger.e("Failed to get last profile sync time", "PROFILE_CACHE", e)
                0L
            }
        }

    override suspend fun isProfileCacheValid(maxAgeHours: Int): Boolean =
        withContext(Dispatchers.IO) {
            try {
                val lastSync = getLastProfileSync()
                if (lastSync == 0L) return@withContext false

                val currentTime = System.currentTimeMillis()
                val maxAge = maxAgeHours * 60 * 60 * 1000L

                (currentTime - lastSync) < maxAge
            } catch (e: Exception) {
                Logger.e("Failed to check profile cache validity", "PROFILE_CACHE", e)
                false
            }
        }
}
