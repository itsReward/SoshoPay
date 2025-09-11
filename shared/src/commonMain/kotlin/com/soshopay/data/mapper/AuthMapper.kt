package com.soshopay.data.mapper

import com.soshopay.data.remote.dto.*
import com.soshopay.domain.model.*
import kotlinx.datetime.Instant

object AuthMapper {
    fun mapToAuthToken(response: AuthResponse): AuthToken {
        val expiresAt = parseDateTime(response.accessExpiresAt)
        val createdAt =
            kotlinx.datetime.Clock.System
                .now()
                .toEpochMilliseconds()
        val expiresIn = (expiresAt - createdAt) / 1000 // Convert to seconds

        return AuthToken(
            accessToken = response.accessToken,
            refreshToken = response.refreshToken,
            tokenType = response.accessTokenType,
            expiresIn = expiresIn,
            userId = response.client.id,
            createdAt = createdAt,
        )
    }

    fun mapToAuthToken(response: SetPinResponse): AuthToken {
        val expiresAt = parseDateTime(response.expiresAt)
        val createdAt =
            kotlinx.datetime.Clock.System
                .now()
                .toEpochMilliseconds()
        val expiresIn = (expiresAt - createdAt) / 1000 // Convert to seconds

        return AuthToken(
            accessToken = response.token,
            refreshToken = "", // Set PIN doesn't return refresh token
            tokenType = response.tokenType,
            expiresIn = expiresIn,
            userId = response.client.id,
            createdAt = createdAt,
        )
    }

    fun mapToAuthToken(response: RefreshTokenResponse): AuthToken {
        val expiresAt = parseDateTime(response.accessExpiresAt)
        val createdAt =
            kotlinx.datetime.Clock.System
                .now()
                .toEpochMilliseconds()
        val expiresIn = (expiresAt - createdAt) / 1000 // Convert to seconds

        return AuthToken(
            accessToken = response.accessToken,
            refreshToken = response.refreshToken,
            tokenType = "Bearer",
            expiresIn = expiresIn,
            userId = "", // Will be set from current user
            createdAt = createdAt,
        )
    }

    fun mapToOtpSession(
        response: OtpResponse,
        phoneNumber: String,
    ): OtpSession {
        val createdAt =
            kotlinx.datetime.Clock.System
                .now()
                .toEpochMilliseconds()
        val expiresAt = createdAt + (response.expiresIn * 1000L)

        return OtpSession(
            id = response.otpId,
            phoneNumber = phoneNumber,
            otpCode = "", // OTP code is not returned by API
            expiresAt = expiresAt,
            createdAt = createdAt,
        )
    }

    fun mapToTempToken(response: TempTokenResponse): String = response.token

    private fun parseDateTime(dateTimeString: String): Long =
        try {
            Instant.parse(dateTimeString).toEpochMilliseconds()
        } catch (e: Exception) {
            kotlinx.datetime.Clock.System
                .now()
                .toEpochMilliseconds() + (3600 * 1000) // Default to 1 hour
        }
}
