package com.soshopay.domain.model

import kotlinx.serialization.Serializable

/**
 * Represents an authentication token for a user session in the SoshoPay domain.
 *
 * This data class encapsulates all relevant information about the user's authentication state,
 * including the access token, refresh token, token type, expiration, scope, user ID, and creation time.
 *
 * @property accessToken The access token string used for authenticating API requests.
 * @property refreshToken The refresh token string used to obtain a new access token when the current one expires.
 * @property tokenType The type of the token, typically "Bearer".
 * @property expiresIn The number of seconds until the access token expires, counted from [createdAt].
 * @property scope The scope of the token, if applicable (may be null).
 * @property userId The unique identifier of the user associated with this token.
 * @property createdAt The timestamp (in milliseconds since epoch) when the token was created. Defaults to the current system time.
 */
@Serializable
data class AuthToken(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String = "Bearer",
    val expiresIn: Long, // seconds
    val scope: String? = null,
    val userId: String,
    val createdAt: Long =
        kotlinx.datetime.Clock.System
            .now()
            .toEpochMilliseconds(),
) {
    /**
     * Checks if the access token has expired.
     *
     * @return true if the current time is greater than or equal to the expiry time, false otherwise.
     */
    fun isExpired(): Boolean {
        val currentTime =
            kotlinx.datetime.Clock.System
                .now()
                .toEpochMilliseconds()
        val expiryTime = createdAt + (expiresIn * 1000)
        return currentTime >= expiryTime
    }

    /**
     * Determines if the token needs to be refreshed, based on a buffer period before actual expiry.
     *
     * @param bufferMinutes The number of minutes before expiry to consider the token as needing refresh. Default is 5 minutes.
     * @return true if the current time is within the buffer period before expiry, false otherwise.
     */
    fun needsRefresh(bufferMinutes: Int = 5): Boolean {
        val currentTime =
            kotlinx.datetime.Clock.System
                .now()
                .toEpochMilliseconds()
        val refreshTime = createdAt + ((expiresIn - bufferMinutes * 60) * 1000)
        return currentTime >= refreshTime
    }

    /**
     * Returns the remaining time in milliseconds until the token expires.
     *
     * @return The number of milliseconds remaining until expiry, or 0 if already expired.
     */
    fun getRemainingTime(): Long {
        val currentTime =
            kotlinx.datetime.Clock.System
                .now()
                .toEpochMilliseconds()
        val expiryTime = createdAt + (expiresIn * 1000)
        return maxOf(0, expiryTime - currentTime)
    }
}
