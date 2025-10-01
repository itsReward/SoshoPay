package com.soshopay.data.repository

import com.soshopay.data.mapper.AuthMapper
import com.soshopay.data.remote.api.AuthApiService
import com.soshopay.data.remote.dto.AuthResponse
import com.soshopay.data.remote.dto.ClientDto
import com.soshopay.data.remote.dto.OtpResponse
import com.soshopay.data.remote.dto.RefreshTokenResponse
import com.soshopay.domain.model.AuthToken
import com.soshopay.domain.model.OtpSession
import com.soshopay.domain.model.User
import com.soshopay.domain.storage.ProfileCache
import com.soshopay.domain.storage.TokenStorage
import com.soshopay.domain.storage.UserPreferences
import com.soshopay.domain.util.Result
import com.soshopay.domain.util.SoshoPayException
import io.mockk.*
import kotlinx.coroutines.runBlocking
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AuthRepositoryImplTest {
    private lateinit var authApiService: AuthApiService
    private lateinit var tokenStorage: TokenStorage
    private lateinit var profileCache: ProfileCache
    private lateinit var userPreferences: UserPreferences
    private lateinit var repository: AuthRepositoryImpl

    @BeforeTest
    fun setup() {
        authApiService = mockk()
        tokenStorage = mockk()
        profileCache = mockk()
        userPreferences = mockk()
        repository =
            AuthRepositoryImpl(
                authApiService,
                tokenStorage,
                profileCache,
                userPreferences,
            )
    }

    @Test
    fun `sendOtp with invalid phone number returns validation error`() =
        runBlocking {
            // Given an invalid phone number
            val invalidPhone = "123"

            // When sendOtp is called
            val result = repository.sendOtp(invalidPhone)

            // Then verify it returns validation error
            assertTrue(result is Result.Error)
            assertTrue(result.exception is SoshoPayException.ValidationException)
        }

    @Test
    fun `sendOtp with valid phone number succeeds`() =
        runBlocking {
            // Given
            val validPhone = "0771234567"
            val deviceId = "test-device-id"
            val normalizedPhone = "263771234567"
            val mockResponse = mockk<OtpResponse>()
            val mockOtpSession = OtpSession("session-id", normalizedPhone, "3942", 300000L)

            // Setup mocks
            coEvery { userPreferences.getDeviceId() } returns deviceId
            coEvery { authApiService.sendOtp(normalizedPhone, deviceId) } returns Result.Success(mockResponse)
            every { AuthMapper.mapToOtpSession(mockResponse, normalizedPhone) } returns mockOtpSession

            // When
            val result = repository.sendOtp(validPhone)

            // Then
            assertTrue(result is Result.Success)
            assertEquals(mockOtpSession, result.data)
        }

    @Test
    fun `login with valid credentials succeeds`() =
        runBlocking {
            // Given
            val phone = "0771234567"
            val pin = "123456"
            val deviceId = "test-device-id"
            val normalizedPhone = "263771234567"
            val mockAuthResponse = mockk<AuthResponse>()
            val mockAuthToken = AuthToken("access-token", "refresh-token", "user-id", 2134567890, "user-id", "user-id")
            val mockClientDto = mockk<ClientDto>()
            val mockUser = mockk<User>()

            // Setup mocks
            coEvery { userPreferences.getDeviceId() } returns deviceId
            coEvery { authApiService.login(normalizedPhone, pin, deviceId) } returns Result.Success(mockAuthResponse)
            every { AuthMapper.mapToAuthToken(mockAuthResponse) } returns mockAuthToken
            coEvery { tokenStorage.saveAuthToken(mockAuthToken) } returns true
            every { mockAuthResponse.client } returns mockClientDto
            every {
                com.soshopay.data.mapper.ProfileMapper
                    .mapToUser(mockClientDto)
            } returns mockUser
            coEvery { profileCache.saveUser(mockUser) } returns true
            coEvery { profileCache.setLastProfileSync(any()) } returns true

            // When
            val result = repository.login(phone, pin)

            // Then
            assertTrue(result is Result.Success)
            assertEquals(mockAuthToken, result.data)
        }

    @Test
    fun `logout clears all local data`() =
        runBlocking {
            // Given
            val deviceId = "test-device-id"
            coEvery { userPreferences.getDeviceId() } returns deviceId
            coEvery { authApiService.logout(deviceId) } returns Result.Success(Unit)
            coEvery { tokenStorage.clearAllTokens() } returns true
            coEvery { profileCache.clearUser() } returns true

            // When
            val result = repository.logout()

            // Then
            assertTrue(result is Result.Success)
            coVerify { tokenStorage.clearAllTokens() }
            coVerify { profileCache.clearUser() }
        }

    @Test
    fun `refreshToken with valid tokens succeeds`() =
        runBlocking {
            // Given
            val currentToken = AuthToken("old-access", "refresh-token", "user-id", 0, "user-id", "user-id")
            val refreshTokenString = "refresh-token"
            val refreshedTokenData = RefreshTokenResponse("access-token", "refresh-token", "9023425342", "498092094")
            val mappedToken = AuthToken("new-access", "new-refresh", "mapped-user-id", 0, "mapped-user-id", "mapped-user-id")
            val expectedToken = mappedToken.copy(userId = currentToken.userId)

            coEvery { tokenStorage.getAuthToken() } returns currentToken
            coEvery { tokenStorage.getRefreshToken() } returns refreshTokenString
            coEvery { authApiService.refreshToken(refreshTokenString) } returns Result.Success(refreshedTokenData)
            every { AuthMapper.mapToAuthToken(refreshedTokenData) } returns mappedToken
            coEvery { tokenStorage.saveAuthToken(expectedToken) } returns true

            // When
            val result = repository.refreshToken()

            // Then
            assertTrue(result is Result.Success)
            assertEquals(expectedToken, result.data)
            coVerify { tokenStorage.saveAuthToken(expectedToken) }
        }
}
