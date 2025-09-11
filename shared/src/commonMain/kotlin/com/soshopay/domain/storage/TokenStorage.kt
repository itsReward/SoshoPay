package com.soshopay.domain.storage

import com.soshopay.domain.model.AuthToken

interface TokenStorage {
    suspend fun saveAuthToken(token: AuthToken): Boolean

    suspend fun getAuthToken(): AuthToken?

    suspend fun clearAuthToken(): Boolean

    suspend fun saveRefreshToken(token: String): Boolean

    suspend fun getRefreshToken(): String?

    suspend fun clearAllTokens(): Boolean

    suspend fun isTokenValid(): Boolean
}
