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

class AuthRepositoryImpl(
    private val authApiService: AuthApiService,
    private val tokenStorage: TokenStorage,
    private val profileCache: ProfileCache,
    private val userPreferences: UserPreferences,
) : AuthRepository {
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

    override suspend fun isLoggedIn(): Boolean = tokenStorage.isTokenValid()

    override suspend fun getCurrentUser(): User? = profileCache.getCurrentUser()

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

    override suspend fun confirmMobileChange(changeToken: String): Result<String> =
        when (val result = authApiService.confirmMobileChange(changeToken)) {
            is Result.Success -> {
                // Update cached user with new mobile
                val currentUser = getCurrentUser()
                currentUser?.let { user ->
                    val updatedUser = user.copy(phoneNumber = result.data.mobile)
                    profileCache.saveUser(updatedUser)
                }
                Result.Success(result.data.mobile)
            }
            is Result.Error -> result
            is Result.Loading -> result
        }
}
