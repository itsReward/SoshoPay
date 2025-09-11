package com.soshopay.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class OtpSession(
    val id: String,
    val phoneNumber: String,
    val otpCode: String,
    val expiresAt: Long,
    val isUsed: Boolean = false,
    val attempts: Int = 0,
    val maxAttempts: Int = 3,
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
        return currentTime >= expiresAt
    }

    fun canRetry(): Boolean = attempts < maxAttempts && !isUsed && !isExpired()

    fun incrementAttempts(): OtpSession = copy(attempts = attempts + 1)

    fun getRemainingTime(): Long {
        val currentTime =
            kotlinx.datetime.Clock.System
                .now()
                .toEpochMilliseconds()
        return maxOf(0, expiresAt - currentTime)
    }

    fun getRemainingAttempts(): Int = maxOf(0, maxAttempts - attempts)
}
