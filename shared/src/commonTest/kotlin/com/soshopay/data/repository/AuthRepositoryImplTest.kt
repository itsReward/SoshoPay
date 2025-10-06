package com.soshopay.data.repository

import com.soshopay.data.mapper.AuthMapper
import com.soshopay.data.mapper.ProfileMapper
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
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
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

            coEvery { authApiService.sendOtp("123", "test-device-id") } returns
                Result.Error(SoshoPayException.ValidationException("Invalid phone"))

            // Mock deviceId to avoid MockKException (even though it shouldn't be called)
            coEvery { userPreferences.getDeviceId() } returns "test-device-id"

            // When sendOtp is called
            val result = repository.sendOtp(invalidPhone)

            // Then verify it returns validation error
            assertTrue(result is Result.Error)
            assertTrue(result.exception is SoshoPayException.ValidationException)
        }

    @Test
    fun `sendOtp with valid phone number succeeds`() =
        runTest {
            // Given
            val validPhone = "0771234567"
            val deviceId = "test-device-id"
            val normalizedPhone = "263771234567"
            val mockResponse = mockk<OtpResponse>()

            // Setup mocks
            coEvery { userPreferences.getDeviceId() } returns deviceId
            coEvery { authApiService.sendOtp(normalizedPhone, deviceId) } returns Result.Success(mockResponse)
            every { mockResponse.otpId } returns "session-id"
            every { mockResponse.message } returns ""
            every { mockResponse.expiresIn } returns 300 // expiresIn is in seconds, not milliseconds

            // When
            val result = repository.sendOtp(validPhone)

            // Then
            assertTrue(result is Result.Success, "Expected Result.Success but got $result")
            val otpSession = (result as Result.Success).data

            // Verify the mapped values
            assertEquals("session-id", otpSession.id)
            assertEquals(normalizedPhone, otpSession.phoneNumber)
            assertEquals("", otpSession.otpCode)
            assertEquals(false, otpSession.isUsed)
            assertEquals(0, otpSession.attempts)
            assertEquals(3, otpSession.maxAttempts)

            // Verify timestamps are reasonable (within last second)
            val now =
                Clock.System
                    .now()
                    .toEpochMilliseconds()
            assertTrue(otpSession.createdAt <= now && otpSession.createdAt > now - 1000)
            assertTrue(otpSession.expiresAt > now) // Should expire in the future

            // Verify the expiration calculation (createdAt + 300 seconds in milliseconds)
            assertEquals(otpSession.createdAt + 300000L, otpSession.expiresAt)

            // Verify API calls
            coVerify(exactly = 1) {
                userPreferences.getDeviceId()
                authApiService.sendOtp(normalizedPhone, deviceId)
            }
        }

    @Test
    fun `login with valid credentials succeeds`() =
        runTest {
            // Given
            val phone = "0778811197"
            val normalizedPhone = "263778811197"
            val pin = "1234"
            val deviceId = "test-device-id"
            val mockClientDto =
                ClientDto(
                    id = "user-id",
                    firstName = "name",
                    lastName = "lastname",
                    mobile = phone,
                    profilePicture = "",
                )

            // Create the mock first, then configure it
            val mockAuthResponse = mockk<AuthResponse>()

            val mockAuthToken =
                AuthToken(
                    accessToken = "access-token",
                    refreshToken = "refresh-token",
                    userId = "user-id",
                    expiresIn = 2134567890L,
                    tokenType = "Bearer",
                    scope = "full",
                )
            val mockUser = mockk<User>()

            // Mock companion objects
            mockkObject(AuthMapper)
            mockkObject(ProfileMapper)

            // Setup mocks
            coEvery { userPreferences.getDeviceId() } returns deviceId
            coEvery { authApiService.login(normalizedPhone, pin, deviceId) } returns Result.Success(mockAuthResponse)
            every { mockAuthResponse.accessToken } returns "access-token"
            every { mockAuthResponse.refreshToken } returns "refresh-token"
            every { mockAuthResponse.accessExpiresAt } returns "2134567890"
            every { mockAuthResponse.client } returns mockClientDto
            every { AuthMapper.mapToAuthToken(mockAuthResponse) } returns mockAuthToken
            coEvery { tokenStorage.saveAuthToken(mockAuthToken) } returns true
            every { ProfileMapper.mapToUser(mockClientDto) } returns mockUser
            coEvery { profileCache.saveUser(mockUser) } returns true
            coEvery { profileCache.setLastProfileSync(any()) } returns true

            // When
            val result = repository.login(phone, pin)

            // Then
            assertTrue(result is Result.Success, "Expected Result.Success but got $result")
            assertEquals(mockAuthToken, (result as Result.Success).data)

            // Verify in order of execution
            coVerifySequence {
                userPreferences.getDeviceId()
                authApiService.login(normalizedPhone, pin, deviceId)
                tokenStorage.saveAuthToken(mockAuthToken)
                profileCache.saveUser(mockUser)
                profileCache.setLastProfileSync(any())
            }

            // Cleanup
            unmockkObject(AuthMapper)
            unmockkObject(ProfileMapper)
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

            mockkObject(AuthMapper) // Mock the companion object

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

            unmockkObject(AuthMapper) // Cleanup after test
        }
}
