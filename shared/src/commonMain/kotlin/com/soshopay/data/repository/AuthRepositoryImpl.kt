package com.soshopay.data.repository

import com.soshopay.data.mapper.AuthMapper
import com.soshopay.data.remote.api.AuthApiService
import com.soshopay.domain.model.AuthToken
import com.soshopay.domain.model.OtpSession
import com.soshopay.domain.model.User
import com.soshopay.domain.repository.AuthRepository
import com.soshopay.domain.storage.ProfileCache
import com.soshopay.domain.storage.TokenStorage
import com.soshopay.domain.storage.UserPreferences
import com.soshopay.domain.util.Logger
import com.soshopay.domain.util.Result
import com.soshopay.domain.util.SoshoPayException
import com.soshopay.domain.util.ValidationUtils

/**
 * Implementation of [AuthRepository] for managing authentication workflows in the SoshoPay domain.
 *
 * This class coordinates between the remote API ([AuthApiService]), token storage ([TokenStorage]),
 * profile cache ([ProfileCache]), and user preferences ([UserPreferences]) to provide robust, validated,
 * and secure authentication management. It supports OTP verification, PIN setup and updates, login/logout,
 * token refresh, user creation, and mobile number changes.
 *
 * Key features:
 * - Validates all user input before sending to the API, returning detailed errors if validation fails.
 * - Handles normalization of Zimbabwe phone numbers and PIN validation.
 * - Manages authentication and refresh tokens securely, including saving, clearing, and refreshing.
 * - Caches user profile data after successful login and updates cache on mobile change.
 * - Provides fallback and error handling for all authentication operations.
 * - Logs all major authentication events for observability and debugging.
 * - Supports mobile number change workflows with OTP verification and confirmation.
 *
 * @property authApiService Remote API service for authentication operations.
 * @property tokenStorage Secure storage for authentication and refresh tokens.
 * @property profileCache Local cache for user profile data.
 * @property userPreferences Storage for user-specific preferences and device ID.
 */
class AuthRepositoryImpl(
    private val authApiService: AuthApiService,
    private val tokenStorage: TokenStorage,
    private val profileCache: ProfileCache,
    private val userPreferences: UserPreferences,
) : AuthRepository {
    /**
     * Sends an OTP to the provided phone number after validating and normalizing it.
     *
     * @param phoneNumber The user's phone number to receive the OTP.
     * @return [Result] containing [OtpSession] on success, or an error if validation fails or the API call is unsuccessful.
     *
     * Steps:
     * - Validates the phone number format.
     * - Normalizes the phone number for Zimbabwe.
     * - Retrieves the device ID from user preferences.
     * - Logs the OTP request event.
     * - Calls the remote API to send the OTP.
     * - Maps the API response to an [OtpSession] and logs the result.
     */
    override suspend fun sendOtp(phoneNumber: String): Result<OtpSession> {
        // Validate phone number
        val validationError = ValidationUtils.Phone.getValidationError(phoneNumber)
        if (validationError != null) {
            return Result.Error(SoshoPayException.ValidationException(validationError))
        }
        val normalizedPhone = ValidationUtils.Phone.normalizeZimbabwePhone(phoneNumber)
        val deviceId = userPreferences.getDeviceId()
        Logger.logAuthEvent("OTP_REQUEST_STARTED", normalizedPhone)
        return when (val result = authApiService.sendOtp(normalizedPhone, deviceId)) {
            is Result.Success -> {
                val otpSession = AuthMapper.mapToOtpSession(result.data, normalizedPhone)
                Logger.logAuthEvent("OTP_SENT_SUCCESS", normalizedPhone)
                Result.Success(otpSession)
            }
            is Result.Error -> {
                Logger.logAuthEvent("OTP_SEND_FAILED", normalizedPhone)
                result
            }
            is Result.Loading -> result
        }
    }

    /**
     * Verifies the OTP code for a given session.
     *
     * Validates the OTP session for expiration and retry attempts, checks the entered code format,
     * logs the verification event, and calls the remote API to verify the OTP.
     *
     * @param otpSession The current OTP session containing session details.
     * @param enteredCode The OTP code entered by the user.
     * @return [Result] containing a temporary token on success, or an error if validation fails or the API call is unsuccessful.
     */
    override suspend fun verifyOtp(
        otpSession: OtpSession,
        enteredCode: String,
    ): Result<String> {
        // Validate OTP session
        if (otpSession.isExpired()) {
            return Result.Error(SoshoPayException.OtpExpiredException())
        }

        if (!otpSession.canRetry()) {
            return Result.Error(SoshoPayException.MaxAttemptsExceededException())
        }

        if (enteredCode.length != 6 || !enteredCode.all { it.isDigit() }) {
            return Result.Error(SoshoPayException.ValidationException("OTP must be 6 digits"))
        }

        Logger.logAuthEvent("OTP_VERIFICATION_STARTED", otpSession.phoneNumber)

        return when (val result = authApiService.verifyOtp(otpSession.id, enteredCode)) {
            is Result.Success -> {
                val tempToken = AuthMapper.mapToTempToken(result.data)
                Logger.logAuthEvent("OTP_VERIFICATION_SUCCESS", otpSession.phoneNumber)
                Result.Success(tempToken)
            }
            is Result.Error -> {
                Logger.logAuthEvent("OTP_VERIFICATION_FAILED", otpSession.phoneNumber)
                result
            }
            is Result.Loading -> result
        }
    }

    /**
     * Verifies the OTP code for a given session.
     *
     * This function checks if the provided `OtpSession` is expired or has exceeded retry attempts,
     * validates the format of the entered OTP code, logs the verification event, and calls the remote API
     * to verify the OTP. On success, it returns a temporary token; on failure, it returns a detailed error.
     *
     * @param otpSession The current OTP session containing session details.
     * @param enteredCode The OTP code entered by the user.
     * @return [Result] containing a temporary token as [String] on success, or an error if validation fails or the API call is unsuccessful.
     */
    override suspend fun setPin(
        tempToken: String,
        pin: String,
        phoneNumber: String,
    ): Result<AuthToken> {
        // Validate PIN
        val pinError = ValidationUtils.Pin.getValidationError(pin)
        if (pinError != null) {
            return Result.Error(SoshoPayException.ValidationException(pinError))
        }

        val normalizedPhone = ValidationUtils.Phone.normalizeZimbabwePhone(phoneNumber)

        Logger.logAuthEvent("PIN_SETUP_STARTED", normalizedPhone)

        return when (val result = authApiService.setPin(normalizedPhone, pin, pin)) {
            is Result.Success -> {
                val authToken = AuthMapper.mapToAuthToken(result.data)

                // Save token
                val tokenSaved = tokenStorage.saveAuthToken(authToken)
                if (!tokenSaved) {
                    Logger.e("Failed to save auth token after PIN setup", "AUTH")
                    return Result.Error(SoshoPayException.UnknownException("Failed to save authentication"))
                }

                Logger.logAuthEvent("PIN_SETUP_SUCCESS", normalizedPhone)
                Result.Success(authToken)
            }
            is Result.Error -> {
                Logger.logAuthEvent("PIN_SETUP_FAILED", normalizedPhone)
                result
            }
            is Result.Loading -> result
        }
    }

    /**
     * Authenticates a user with the provided phone number and PIN.
     *
     * Validates the phone number and PIN, normalizes the phone number, retrieves the device ID,
     * logs the login event, and calls the remote API to perform login. On success, saves the authentication token,
     * caches the user profile, and logs the result.
     *
     * @param phoneNumber The user's phone number.
     * @param pin The user's PIN.
     * @return [Result] containing [AuthToken] on success, or an error if validation fails or the API call is unsuccessful.
     */
    override suspend fun login(
        phoneNumber: String,
        pin: String,
    ): Result<AuthToken> {
        // Validate inputs
        val phoneError = ValidationUtils.Phone.getValidationError(phoneNumber)
        if (phoneError != null) {
            return Result.Error(SoshoPayException.ValidationException(phoneError))
        }

        val pinError = ValidationUtils.Pin.getValidationError(pin)
        if (pinError != null) {
            return Result.Error(SoshoPayException.ValidationException(pinError))
        }

        val normalizedPhone = ValidationUtils.Phone.normalizeZimbabwePhone(phoneNumber)
        val deviceId = userPreferences.getDeviceId()

        Logger.logAuthEvent("LOGIN_STARTED", normalizedPhone)

        return when (val result = authApiService.login(normalizedPhone, pin, deviceId)) {
            is Result.Success -> {
                val authToken = AuthMapper.mapToAuthToken(result.data)

                // Save token
                val tokenSaved = tokenStorage.saveAuthToken(authToken)
                if (!tokenSaved) {
                    Logger.e("Failed to save auth token after login", "AUTH")
                    return Result.Error(SoshoPayException.UnknownException("Failed to save authentication"))
                }

                // Cache user profile
                val user =
                    com.soshopay.data.mapper.ProfileMapper
                        .mapToUser(result.data.client)
                profileCache.saveUser(user)
                profileCache.setLastProfileSync(
                    kotlinx.datetime.Clock.System
                        .now()
                        .toEpochMilliseconds(),
                )

                Logger.logAuthEvent("LOGIN_SUCCESS", normalizedPhone)
                Result.Success(authToken)
            }
            is Result.Error -> {
                Logger.logAuthEvent("LOGIN_FAILED", normalizedPhone)
                result
            }
            is Result.Loading -> result
        }
    }

    /**
     * Refreshes the authentication token using the stored refresh token.
     *
     * Retrieves the current and refresh tokens, validates their presence, logs the refresh event,
     * and calls the remote API to refresh the token. On success, saves the new token and logs the result.
     * Clears tokens on failure.
     *
     * @return [Result] containing [AuthToken] on success, or an error if tokens are missing or the API call fails.
     */
    override suspend fun refreshToken(): Result<AuthToken> {
        val currentToken = tokenStorage.getAuthToken()
        val refreshToken = tokenStorage.getRefreshToken()

        if (currentToken == null || refreshToken == null) {
            return Result.Error(SoshoPayException.TokenExpiredException("No tokens available"))
        }

        Logger.logAuthEvent("TOKEN_REFRESH_STARTED")

        return when (val result = authApiService.refreshToken(refreshToken)) {
            is Result.Success -> {
                val newToken = AuthMapper.mapToAuthToken(result.data).copy(userId = currentToken.userId)

                // Save new tokens
                val tokenSaved = tokenStorage.saveAuthToken(newToken)
                if (!tokenSaved) {
                    Logger.e("Failed to save refreshed auth token", "AUTH")
                    return Result.Error(SoshoPayException.UnknownException("Failed to save refreshed token"))
                }

                Logger.logAuthEvent("TOKEN_REFRESH_SUCCESS")
                Result.Success(newToken)
            }
            is Result.Error -> {
                Logger.logAuthEvent("TOKEN_REFRESH_FAILED")
                // Clear invalid tokens
                tokenStorage.clearAllTokens()
                result
            }
            is Result.Loading -> result
        }
    }

    /**
     * Logs out the current user by clearing tokens and cached profile data.
     *
     * Retrieves the device ID, logs the logout event, calls the remote API to log out,
     * and clears local tokens and user profile. Returns success if all data is cleared, otherwise returns an error.
     *
     * @return [Result] containing [Unit] on success, or an error if local data could not be cleared.
     */
    override suspend fun logout(): Result<Unit> {
        val deviceId = userPreferences.getDeviceId()

        Logger.logAuthEvent("LOGOUT_STARTED")

        // Call API logout (don't fail if this fails)
        authApiService.logout(deviceId)

        // Clear local data
        val tokensCleared = tokenStorage.clearAllTokens()
        val profileCleared = profileCache.clearUser()

        return if (tokensCleared && profileCleared) {
            Logger.logAuthEvent("LOGOUT_SUCCESS")
            Result.Success(Unit)
        } else {
            Logger.logAuthEvent("LOGOUT_PARTIAL_FAILURE")
            Result.Error(SoshoPayException.UnknownException("Failed to clear all local data"))
        }
    }

    /**
     * Checks if the user is currently logged in by validating the stored token.
     *
     * @return `true` if the token is valid, `false` otherwise.
     */
    override suspend fun isLoggedIn(): Boolean = tokenStorage.isTokenValid()

    /**
     * Retrieves the currently cached user profile.
     *
     * @return [User] if available, or `null` if not cached.
     */
    override suspend fun getCurrentUser(): User? = profileCache.getCurrentUser()

    /**
     * Updates the user's PIN after validating the current and new PINs.
     *
     * Validates both PINs, ensures they are different, logs the update event,
     * and calls the remote API to update the PIN. Returns success or error based on the API response.
     *
     * @param currentPin The user's current PIN.
     * @param newPin The new PIN to set.
     * @return [Result] containing [Unit] on success, or an error if validation fails or the API call fails.
     */
    override suspend fun updatePin(
        currentPin: String,
        newPin: String,
    ): Result<Unit> {
        // Validate PINs
        val currentPinError = ValidationUtils.Pin.getValidationError(currentPin)
        if (currentPinError != null) {
            return Result.Error(SoshoPayException.ValidationException("Current $currentPinError"))
        }

        val newPinError = ValidationUtils.Pin.getValidationError(newPin)
        if (newPinError != null) {
            return Result.Error(SoshoPayException.ValidationException("New $newPinError"))
        }

        if (currentPin == newPin) {
            return Result.Error(SoshoPayException.ValidationException("New PIN must be different from current PIN"))
        }

        Logger.logAuthEvent("PIN_UPDATE_STARTED")

        return when (val result = authApiService.updatePin(currentPin, newPin, newPin)) {
            is Result.Success -> {
                Logger.logAuthEvent("PIN_UPDATE_SUCCESS")
                result
            }
            is Result.Error -> {
                Logger.logAuthEvent("PIN_UPDATE_FAILED")
                result
            }
            is Result.Loading -> result
        }
    }

    /**
     * Creates a new client (user) with the provided personal details and PIN.
     *
     * Validates the first and last names, phone number, and PIN, normalizes the phone number,
     * logs the client creation event, and calls the remote API to create the client. On success,
     * maps and returns the created user.
     *
     * @param firstName The user's first name.
     * @param lastName The user's last name.
     * @param phoneNumber The user's phone number.
     * @param pin The user's PIN.
     * @return [Result] containing [User] on success, or an error if validation fails or the API call fails.
     */
    override suspend fun createClient(
        firstName: String,
        lastName: String,
        phoneNumber: String,
        pin: String,
    ): Result<User> {
        // Validate inputs
        val validationResult =
            ValidationUtils.PersonalDetails.validatePersonalDetails(
                firstName = firstName,
                lastName = lastName,
                dateOfBirth = 0L, // Will be set later
                gender = "",
                nationality = "",
                occupation = "",
                monthlyIncome = 0.0,
            )

        if (!ValidationUtils.PersonalDetails.isValidName(firstName)) {
            return Result.Error(SoshoPayException.ValidationException("Invalid first name"))
        }

        if (!ValidationUtils.PersonalDetails.isValidName(lastName)) {
            return Result.Error(SoshoPayException.ValidationException("Invalid last name"))
        }

        val phoneError = ValidationUtils.Phone.getValidationError(phoneNumber)
        if (phoneError != null) {
            return Result.Error(SoshoPayException.ValidationException(phoneError))
        }

        val pinError = ValidationUtils.Pin.getValidationError(pin)
        if (pinError != null) {
            return Result.Error(SoshoPayException.ValidationException(pinError))
        }

        val normalizedPhone = ValidationUtils.Phone.normalizeZimbabwePhone(phoneNumber)

        Logger.logAuthEvent("CLIENT_CREATION_STARTED", normalizedPhone)

        return when (val result = authApiService.createClient(firstName, lastName, normalizedPhone, pin, pin)) {
            is Result.Success -> {
                val user =
                    com.soshopay.data.mapper.ProfileMapper
                        .mapToUser(result.data.client)
                Logger.logAuthEvent("CLIENT_CREATION_SUCCESS", normalizedPhone)
                Result.Success(user)
            }
            is Result.Error -> {
                Logger.logAuthEvent("CLIENT_CREATION_FAILED", normalizedPhone)
                result
            }
            is Result.Loading -> result
        }
    }

    /**
     * Initiates the mobile number change workflow.
     *
     * Validates the new mobile number, normalizes it, and calls the remote API to start the change process.
     * Returns a change token on success, or an error if validation fails or the API call fails.
     *
     * @param newMobile The new mobile number to set.
     * @return [Result] containing the change token as [String] on success, or an error.
     */
    override suspend fun startMobileChange(newMobile: String): Result<String> {
        val phoneError = ValidationUtils.Phone.getValidationError(newMobile)
        if (phoneError != null) {
            return Result.Error(SoshoPayException.ValidationException(phoneError))
        }

        val normalizedPhone = ValidationUtils.Phone.normalizeZimbabwePhone(newMobile)

        return when (val result = authApiService.startMobileChange(normalizedPhone)) {
            is Result.Success -> Result.Success(result.data.changeToken)
            is Result.Error -> result
            is Result.Loading -> result
        }
    }

    /**
     * Verifies the OTP for mobile number change.
     *
     * Checks OTP format, then calls the remote API to verify the change token and OTP.
     * Returns a new change token on success, or an error if validation fails or the API call fails.
     *
     * @param changeToken The token received from starting the mobile change.
     * @param otp The OTP code entered by the user.
     * @return [Result] containing the new change token as [String] on success, or an error.
     */
    override suspend fun verifyMobileChange(
        changeToken: String,
        otp: String,
    ): Result<String> {
        if (otp.length != 6 || !otp.all { it.isDigit() }) {
            return Result.Error(SoshoPayException.ValidationException("OTP must be 6 digits"))
        }

        return when (val result = authApiService.verifyMobileChange(changeToken, otp)) {
            is Result.Success -> Result.Success(result.data.changeToken)
            is Result.Error -> result
            is Result.Loading -> result
        }
    }

    override suspend fun confirmMobileChange(changeToken: String): Result<String> {
        TODO("Not yet implemented")
    }
}
