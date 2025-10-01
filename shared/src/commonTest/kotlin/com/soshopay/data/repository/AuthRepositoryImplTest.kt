package com.soshopay.data.repository

import com.soshopay.data.mapper.AuthMapper
import com.soshopay.data.remote.ApiResponse
import com.soshopay.data.remote.api.AuthApiService
import com.soshopay.data.remote.dto.ClientDto
import com.soshopay.data.remote.dto.OtpResponse
import com.soshopay.domain.model.AuthToken
import com.soshopay.domain.model.ClientType
import com.soshopay.domain.model.OtpSession
import com.soshopay.domain.model.User
import com.soshopay.domain.storage.ProfileCache
import com.soshopay.domain.storage.TokenStorage
import com.soshopay.domain.storage.UserPreferences
import com.soshopay.domain.util.Result
import com.soshopay.domain.util.SoshoPayException
import io.mockk.*
import kotlinx.coroutines.runBlocking
import kotlin.test.*

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
            assertTrue((result as Result.Error).exception is SoshoPayException.ValidationException)
        }

    @Test
    fun `sendOtp with valid phone number succeeds`() =
        runBlocking {
            // Given
            val validPhone = "0771234567"
            val deviceId = "test-device-id"
            val normalizedPhone = "263771234567"
            val mockResponse = mockk<com.soshopay.domain.repository.Result<Any>>()
            val mockOtpSession = OtpSession("session-id", normalizedPhone, "3942", 300000L)

            // Setup mocks
            coEvery { userPreferences.getDeviceId() } returns deviceId
            coEvery { authApiService.sendOtp(normalizedPhone, deviceId) } returns Result.Success(mockResponse)
            every { AuthMapper.mapToOtpSession(mockResponse, normalizedPhone) } returns mockOtpSession

            // When
            val result = repository.sendOtp(validPhone)

            // Then
            assertTrue(result is Result.Success)
            assertEquals(mockOtpSession, (result as Result.Success).data)
        }

    @Test
    fun `login with valid credentials succeeds`() =
        runBlocking {
            // Given
            val phone = "0771234567"
            val pin = "123456"
            val deviceId = "test-device-id"
            val normalizedPhone = "263771234567"
            val mockResponse = mockk<AuthToken>()
            val mockAuthToken = AuthToken("access-token", "refresh-token", "user-id", 2134567890, "user-id", "user-id")
            val mockClientData = mockk<Result<ClientDto>>()
            val mockUser = mockk<User>()

            // Setup mocks
            coEvery { userPreferences.getDeviceId() } returns deviceId
            coEvery { authApiService.login(normalizedPhone, pin, deviceId) } returns Result.Success(mockResponse)
            coEvery { AuthMapper.mapToAuthToken(mockResponse) } returns mockAuthToken
            coEvery { tokenStorage.saveAuthToken(mockAuthToken) } returns true
            every { mockResponse.client } returns mockClientData
            every {
                com.soshopay.data.mapper.ProfileMapper
                    .mapToUser(mockClientData)
            } returns mockUser
            every { profileCache.saveUser(mockUser) } returns true
            every { profileCache.setLastProfileSync(any()) } just Runs

            // When
            val result = repository.login(phone, pin)

            // Then
            assertTrue(result is Result.Success)
            assertEquals(mockAuthToken, (result as Result.Success).data)
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
            val currentToken = AuthToken("old-access", "refresh-token", "user-id")
            val newToken = AuthToken("new-access", "refresh-token", "user-id")
            val mockResponse = mockk<ApiResponse.TokenResponse>()

            coEvery { tokenStorage.getAuthToken() } returns currentToken
            coEvery { tokenStorage.getRefreshToken() } returns "refresh-token"
            coEvery { authApiService.refreshToken("refresh-token") } returns Result.Success(mockResponse)
            every { AuthMapper.mapToAuthToken(mockResponse) } returns newToken.copy(userId = "")
            coEvery { tokenStorage.saveAuthToken(any()) } returns true

            // When
            val result = repository.refreshToken()

            // Then
            assertTrue(result is Result.Success)
            assertEquals(newToken, (result as Result.Success).data)
        }
}
