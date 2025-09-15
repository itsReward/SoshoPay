package com.soshopay.domain.storage

import com.soshopay.domain.model.User
import kotlinx.coroutines.flow.Flow

/**
 * Abstraction for caching and observing user profile data in the SoshoPay domain.
 *
 * Implementations of this interface provide methods to persist, retrieve, update, and clear user profiles,
 * as well as observe profile changes and manage cache metadata such as last sync time and cache validity.
 * This enables efficient access to user data and supports offline scenarios.
 */
interface ProfileCache {
    /**
     * Saves the given [user] to the cache.
     * @param user The user profile to save.
     * @return True if the user was saved successfully, false otherwise.
     */
    suspend fun saveUser(user: User): Boolean

    /**
     * Retrieves the user profile for the specified [userId] from the cache.
     * @param userId The ID of the user to retrieve.
     * @return The [User] if found, or null if not present in the cache.
     */
    suspend fun getUser(userId: String): User?

    /**
     * Retrieves the currently active user profile from the cache.
     * @return The current [User], or null if not present.
     */
    suspend fun getCurrentUser(): User?

    /**
     * Updates the given [user] profile in the cache.
     * @param user The user profile to update.
     * @return True if the update was successful, false otherwise.
     */
    suspend fun updateUser(user: User): Boolean

    /**
     * Clears the cached user profile data.
     * @return True if the cache was cleared successfully, false otherwise.
     */
    suspend fun clearUser(): Boolean

    /**
     * Observes changes to the cached user profile as a [Flow].
     * @return A [Flow] emitting the current [User] or null when the profile changes.
     */
    fun observeUser(): Flow<User?>

    /**
     * Sets the last profile sync timestamp in the cache.
     * @param timestamp The time of the last sync (in milliseconds since epoch).
     * @return True if the timestamp was saved successfully, false otherwise.
     */
    suspend fun setLastProfileSync(timestamp: Long): Boolean

    /**
     * Retrieves the last profile sync timestamp from the cache.
     * @return The timestamp of the last sync (in milliseconds since epoch).
     */
    suspend fun getLastProfileSync(): Long

    /**
     * Checks if the profile cache is valid based on the given [maxAgeHours].
     * @param maxAgeHours The maximum age (in hours) for the cache to be considered valid (default: 24).
     * @return True if the cache is valid, false otherwise.
     */
    suspend fun isProfileCacheValid(maxAgeHours: Int = 24): Boolean
}
