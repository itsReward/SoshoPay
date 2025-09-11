package com.soshopay.domain.storage

import com.soshopay.domain.model.User
import kotlinx.coroutines.flow.Flow

interface ProfileCache {
    suspend fun saveUser(user: User): Boolean

    suspend fun getUser(userId: String): User?

    suspend fun getCurrentUser(): User?

    suspend fun updateUser(user: User): Boolean

    suspend fun clearUser(): Boolean

    fun observeUser(): Flow<User?>

    // Cache metadata
    suspend fun setLastProfileSync(timestamp: Long): Boolean

    suspend fun getLastProfileSync(): Long

    suspend fun isProfileCacheValid(maxAgeHours: Int = 24): Boolean
}
