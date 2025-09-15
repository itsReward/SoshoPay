package com.soshopay.domain.storage

import com.soshopay.domain.model.AuthToken

/**
 * Abstraction for secure storage and management of authentication and refresh tokens in the SoshoPay domain.
 *
 * Implementations of this interface provide methods to persist, retrieve, validate, and clear authentication tokens
 * and refresh tokens. This enables secure session management, token renewal, and user authentication workflows.
 *
 * Usage:
 * - Use [saveAuthToken] to persist a new authentication token after login.
 * - Use [getAuthToken] to retrieve the current authentication token for API requests.
 * - Use [clearAuthToken] and [clearAllTokens] to remove tokens on logout or session expiration.
 * - Use [saveRefreshToken] and [getRefreshToken] for token renewal flows.
 * - Use [isTokenValid] to check if the current token is still valid for authenticated operations.
 */
interface TokenStorage {
    /**
     * Saves the given authentication [token] to secure storage.
     * @param token The [AuthToken] to save.
     * @return True if the token was saved successfully, false otherwise.
     */
    suspend fun saveAuthToken(token: AuthToken): Boolean

    /**
     * Retrieves the current authentication token from secure storage.
     * @return The [AuthToken] if present, or null if not found.
     */
    suspend fun getAuthToken(): AuthToken?

    /**
     * Clears the current authentication token from secure storage.
     * @return True if the token was cleared successfully, false otherwise.
     */
    suspend fun clearAuthToken(): Boolean

    /**
     * Saves the given refresh [token] to secure storage for token renewal.
     * @param token The refresh token string to save.
     * @return True if the token was saved successfully, false otherwise.
     */
    suspend fun saveRefreshToken(token: String): Boolean

    /**
     * Retrieves the current refresh token from secure storage.
     * @return The refresh token string if present, or null if not found.
     */
    suspend fun getRefreshToken(): String?

    /**
     * Clears all authentication and refresh tokens from secure storage.
     * @return True if all tokens were cleared successfully, false otherwise.
     */
    suspend fun clearAllTokens(): Boolean

    /**
     * Checks if the current authentication token is valid (not expired or revoked).
     * @return True if the token is valid, false otherwise.
     */
    suspend fun isTokenValid(): Boolean
}
