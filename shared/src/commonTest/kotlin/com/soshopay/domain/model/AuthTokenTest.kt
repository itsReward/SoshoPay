package com.soshopay.domain.model

import kotlinx.datetime.Clock
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class AuthTokenTest {
    private lateinit var authToken: AuthToken
    private val testAccessToken = "test_access_token"
    private val testRefreshToken = "test_refresh_token"
    private val testUserId = "test_user_id"
    private val testExpiresIn = 3600L // 1 hour in seconds

    @BeforeTest
    fun setup() {
        // Create a fresh AuthToken instance before each test
        authToken =
            AuthToken(
                accessToken = testAccessToken,
                refreshToken = testRefreshToken,
                userId = testUserId,
                expiresIn = testExpiresIn,
                createdAt = Clock.System.now().toEpochMilliseconds(),
            )
    }

    @Test
    fun `test token creation with default values`() {
        assertEquals(testAccessToken, authToken.accessToken)
        assertEquals(testRefreshToken, authToken.refreshToken)
        assertEquals("Bearer", authToken.tokenType)
        assertEquals(testExpiresIn, authToken.expiresIn)
        assertNull(authToken.scope)
        assertEquals(testUserId, authToken.userId)
    }

    @Test
    fun `test isExpired returns false for valid token`() {
        assertFalse(authToken.isExpired())
    }

    @Test
    fun `test isExpired returns true for expired token`() {
        // Create a token that was created 2 hours ago (expired)
        val expiredToken =
            AuthToken(
                accessToken = testAccessToken,
                refreshToken = testRefreshToken,
                userId = testUserId,
                expiresIn = 3600, // 1 hour
                createdAt =
                    Clock.System
                        .now()
                        .minus(2.hours)
                        .toEpochMilliseconds(),
            )
        assertTrue(expiredToken.isExpired())
    }

    @Test
    fun `test needsRefresh with default buffer`() {
        // Create a token that will expire in 3 minutes
        val almostExpiredToken =
            AuthToken(
                accessToken = testAccessToken,
                refreshToken = testRefreshToken,
                userId = testUserId,
                expiresIn = 180, // 3 minutes
                createdAt = Clock.System.now().toEpochMilliseconds(),
            )
        // Should need refresh since default buffer is 5 minutes
        assertTrue(almostExpiredToken.needsRefresh())
    }

    @Test
    fun `test needsRefresh with custom buffer`() {
        // Create a token that will expire in 10 minutes
        val token =
            AuthToken(
                accessToken = testAccessToken,
                refreshToken = testRefreshToken,
                userId = testUserId,
                expiresIn = 600, // 10 minutes
                createdAt = Clock.System.now().toEpochMilliseconds(),
            )
        // Should not need refresh with 2-minute buffer
        assertFalse(token.needsRefresh(bufferMinutes = 2))
        // Should need refresh with 15-minute buffer
        assertTrue(token.needsRefresh(bufferMinutes = 15))
    }

    @Test
    fun `test getRemainingTime for valid token`() {
        val remainingTime = authToken.getRemainingTime()
        assertTrue(remainingTime > 0)
        assertTrue(remainingTime <= testExpiresIn * 1000)
    }

    @Test
    fun `test getRemainingTime for expired token returns zero`() {
        val expiredToken =
            AuthToken(
                accessToken = testAccessToken,
                refreshToken = testRefreshToken,
                userId = testUserId,
                expiresIn = 1, // 1 second
                createdAt =
                    Clock.System
                        .now()
                        .minus(2.seconds)
                        .toEpochMilliseconds(),
            )
        assertEquals(0, expiredToken.getRemainingTime())
    }

    @Test
    fun `test token with custom scope`() {
        val customScope = "read write"
        val tokenWithScope =
            AuthToken(
                accessToken = testAccessToken,
                refreshToken = testRefreshToken,
                userId = testUserId,
                expiresIn = testExpiresIn,
                scope = customScope,
                createdAt = Clock.System.now().toEpochMilliseconds(),
            )
        assertEquals(customScope, tokenWithScope.scope)
    }
}
