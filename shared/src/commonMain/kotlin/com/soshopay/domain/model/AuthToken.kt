package com.soshopay.domain.model

import kotlinx.serialization.Serializable

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
    fun isExpired(): Boolean {
        val currentTime =
            kotlinx.datetime.Clock.System
                .now()
                .toEpochMilliseconds()
        val expiryTime = createdAt + (expiresIn * 1000)
        return currentTime >= expiryTime
    }

    fun needsRefresh(bufferMinutes: Int = 5): Boolean {
        val currentTime =
            kotlinx.datetime.Clock.System
                .now()
                .toEpochMilliseconds()
        val refreshTime = createdAt + ((expiresIn - bufferMinutes * 60) * 1000)
        return currentTime >= refreshTime
    }

    fun getRemainingTime(): Long {
        val currentTime =
            kotlinx.datetime.Clock.System
                .now()
                .toEpochMilliseconds()
        val expiryTime = createdAt + (expiresIn * 1000)
        return maxOf(0, expiryTime - currentTime)
    }
}
