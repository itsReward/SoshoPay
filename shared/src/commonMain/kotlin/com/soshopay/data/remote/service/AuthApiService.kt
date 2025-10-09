package com.soshopay.data.remote.api

import com.soshopay.data.remote.dto.AuthResponse
import com.soshopay.data.remote.dto.ConfirmMobileChangeRequest
import com.soshopay.data.remote.dto.CreateClientRequest
import com.soshopay.data.remote.dto.CreateClientResponse
import com.soshopay.data.remote.dto.ErrorResponse
import com.soshopay.data.remote.dto.LoginRequest
import com.soshopay.data.remote.dto.MobileChangeConfirmResponse
import com.soshopay.data.remote.dto.MobileChangeStartResponse
import com.soshopay.data.remote.dto.MobileChangeVerifyResponse
import com.soshopay.data.remote.dto.OtpResponse
import com.soshopay.data.remote.dto.RefreshTokenRequest
import com.soshopay.data.remote.dto.RefreshTokenResponse
import com.soshopay.data.remote.dto.SendOtpRequest
import com.soshopay.data.remote.dto.SetPinRequest
import com.soshopay.data.remote.dto.SetPinResponse
import com.soshopay.data.remote.dto.StartMobileChangeRequest
import com.soshopay.data.remote.dto.TempTokenResponse
import com.soshopay.data.remote.dto.UpdatePinRequest
import com.soshopay.data.remote.dto.VerifyMobileChangeRequest
import com.soshopay.data.remote.dto.VerifyOtpRequest
import com.soshopay.domain.util.Logger
import com.soshopay.domain.util.Result
import com.soshopay.domain.util.SoshoPayException
import com.soshopay.domain.util.safeCall
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType

interface AuthApiService {
    suspend fun sendOtp(
        phoneNumber: String,
        deviceId: String? = null,
    ): Result<OtpResponse>

    suspend fun verifyOtp(
        otpId: String,
        otpCode: String,
    ): Result<TempTokenResponse>

    suspend fun setPin(
        mobile: String,
        newPin: String,
        confirmPin: String,
    ): Result<SetPinResponse>

    suspend fun login(
        mobile: String,
        pin: String,
        deviceId: String? = null,
    ): Result<AuthResponse>

    suspend fun refreshToken(refreshToken: String): Result<RefreshTokenResponse>

    suspend fun logout(deviceId: String? = null): Result<Unit>

    suspend fun createClient(
        firstName: String,
        lastName: String,
        mobile: String,
        pin: String,
        confirmPin: String,
    ): Result<CreateClientResponse>

    suspend fun updatePin(
        currentPin: String,
        newPin: String,
        confirmPin: String,
    ): Result<Unit>

    suspend fun startMobileChange(newMobile: String): Result<MobileChangeStartResponse>

    suspend fun verifyMobileChange(
        changeToken: String,
        otp: String,
    ): Result<MobileChangeVerifyResponse>

    suspend fun confirmMobileChange(changeToken: String): Result<MobileChangeConfirmResponse>
}

/**
 * Implementation of [AuthApiService] that communicates with the SoshoPay backend API for authentication-related operations.
 *
 * This class uses Ktor's [HttpClient] to perform HTTP requests to the backend endpoints for actions such as:
 * - Sending and verifying OTPs
 * - Setting and updating PINs
 * - Logging in and out
 * - Creating new clients
 * - Refreshing tokens
 * - Managing mobile number changes
 *
 * Each method wraps the HTTP call in a [safeCall] to handle exceptions and map API errors to [SoshoPayException] types.
 *
 * @property httpClient The Ktor HTTP client used to make network requests.
 */
class AuthApiServiceImpl(
    private val httpClient: HttpClient,
) : AuthApiService {
    companion object {
        /**
         * Base path for authentication endpoints (relative to base URL)
         *
         * OLD: "https://beta.soshopay.com/api/mobile/client"
         * NEW: "api/mobile/client" (relative path)
         *
         * The full URL will be: {baseUrl}/api/mobile/client/{endpoint}
         * Examples:
         * - Local: http://192.168.100.100:8080/api/mobile/client/login
         * - Beta: https://beta.soshopay.com/api/mobile/client/login
         * - Production: https://api.soshopay.com/api/mobile/client/login
         */
        private const val BASE_PATH = "api/mobile/client"
    }

    override suspend fun sendOtp(
        phoneNumber: String,
        deviceId: String?,
    ): Result<OtpResponse> =
        safeCall {
            Logger.logApiRequest("$BASE_PATH/send-otp", "POST", mapOf("mobile" to phoneNumber))

            val response =
                httpClient.post("$BASE_PATH/send-otp") {
                    contentType(ContentType.Application.Json)
                    deviceId?.let { header("X-Device-ID", it) }
                    setBody(SendOtpRequest(mobile = phoneNumber))
                }

            Logger.logApiResponse("$BASE_PATH/send-otp", response.status.value)

            when (response.status) {
                HttpStatusCode.OK -> response.body<OtpResponse>()
                HttpStatusCode.BadRequest -> {
                    val error = response.body<ErrorResponse>()
                    throw SoshoPayException.ValidationException(error.message)
                }
                HttpStatusCode.TooManyRequests -> {
                    throw SoshoPayException.MaxAttemptsExceededException("Too many OTP requests")
                }
                else -> {
                    val error = response.body<ErrorResponse>()
                    throw SoshoPayException.ServerException(error.message, response.status.value)
                }
            }
        }

    override suspend fun verifyOtp(
        otpId: String,
        otpCode: String,
    ): Result<TempTokenResponse> =
        safeCall {
            Logger.logApiRequest("$BASE_PATH/verify-otp", "POST", mapOf("otp_id" to otpId))

            val response =
                httpClient.post("$BASE_PATH/verify-otp") {
                    contentType(ContentType.Application.Json)
                    setBody(VerifyOtpRequest(otpId = otpId, otpCode = otpCode))
                }

            Logger.logApiResponse("$BASE_PATH/verify-otp", response.status.value)

            when (response.status) {
                HttpStatusCode.OK -> response.body<TempTokenResponse>()
                HttpStatusCode.BadRequest -> {
                    val error = response.body<ErrorResponse>()
                    when {
                        error.message.contains("expired", ignoreCase = true) ->
                            throw SoshoPayException.OtpExpiredException(error.message)
                        error.message.contains("invalid", ignoreCase = true) ->
                            throw SoshoPayException.OtpInvalidException(error.message)
                        error.message.contains("attempts", ignoreCase = true) ->
                            throw SoshoPayException.MaxAttemptsExceededException(error.message)
                        else -> throw SoshoPayException.ValidationException(error.message)
                    }
                }
                else -> {
                    val error = response.body<ErrorResponse>()
                    throw SoshoPayException.ServerException(error.message, response.status.value)
                }
            }
        }

    override suspend fun setPin(
        mobile: String,
        newPin: String,
        confirmPin: String,
    ): Result<SetPinResponse> =
        safeCall {
            Logger.logApiRequest("$BASE_PATH/set-pin", "POST", mapOf("mobile" to mobile))

            val response =
                httpClient.post("$BASE_PATH/set-pin") {
                    contentType(ContentType.Application.Json)
                    setBody(SetPinRequest(mobile = mobile, newPin = newPin, confirmPin = confirmPin))
                }

            Logger.logApiResponse("$BASE_PATH/set-pin", response.status.value)

            when (response.status) {
                HttpStatusCode.OK -> response.body<SetPinResponse>()
                HttpStatusCode.BadRequest -> {
                    val error = response.body<ErrorResponse>()
                    throw SoshoPayException.ValidationException(error.message)
                }
                HttpStatusCode.Conflict -> {
                    val error = response.body<ErrorResponse>()
                    throw SoshoPayException.ValidationException("User already has a PIN set")
                }
                else -> {
                    val error = response.body<ErrorResponse>()
                    throw SoshoPayException.ServerException(error.message, response.status.value)
                }
            }
        }

    override suspend fun login(
        mobile: String,
        pin: String,
        deviceId: String?,
    ): Result<AuthResponse> =
        safeCall {
            Logger.logApiRequest("$BASE_PATH/login", "POST", mapOf("mobile" to mobile))

            val response =
                httpClient.post("$BASE_PATH/login") {
                    contentType(ContentType.Application.Json)
                    deviceId?.let { header("X-Device-ID", it) }
                    setBody(LoginRequest(mobile = mobile, pin = pin))
                }

            Logger.logApiResponse("$BASE_PATH/login", response.status.value)

            when (response.status) {
                HttpStatusCode.OK -> response.body<AuthResponse>()
                HttpStatusCode.Unauthorized -> {
                    throw SoshoPayException.InvalidCredentialsException("Invalid mobile number or PIN")
                }
                HttpStatusCode.BadRequest -> {
                    val error = response.body<ErrorResponse>()
                    throw SoshoPayException.ValidationException(error.message)
                }
                else -> {
                    val error = response.body<ErrorResponse>()
                    throw SoshoPayException.ServerException(error.message, response.status.value)
                }
            }
        }

    override suspend fun refreshToken(refreshToken: String): Result<RefreshTokenResponse> =
        safeCall {
            Logger.logApiRequest("$BASE_PATH/refresh-token", "POST")

            val response =
                httpClient.post("$BASE_PATH/refresh-token") {
                    contentType(ContentType.Application.Json)
                    setBody(RefreshTokenRequest(refreshToken = refreshToken))
                }

            Logger.logApiResponse("$BASE_PATH/refresh-token", response.status.value)

            when (response.status) {
                HttpStatusCode.OK -> response.body<RefreshTokenResponse>()
                HttpStatusCode.Unauthorized -> {
                    throw SoshoPayException.TokenExpiredException("Refresh token has expired")
                }
                else -> {
                    val error = response.body<ErrorResponse>()
                    throw SoshoPayException.ServerException(error.message, response.status.value)
                }
            }
        }

    override suspend fun logout(deviceId: String?): Result<Unit> =
        safeCall {
            Logger.logApiRequest("$BASE_PATH/logout", "POST")

            val response =
                httpClient.post("$BASE_PATH/logout") {
                    deviceId?.let { header("X-Device-ID", it) }
                }

            Logger.logApiResponse("$BASE_PATH/logout", response.status.value)

            when (response.status) {
                HttpStatusCode.OK -> Unit
                else -> {
                    val error = response.body<ErrorResponse>()
                    throw SoshoPayException.ServerException(error.message, response.status.value)
                }
            }
        }

    override suspend fun createClient(
        firstName: String,
        lastName: String,
        mobile: String,
        pin: String,
        confirmPin: String,
    ): Result<CreateClientResponse> =
        safeCall {
            Logger.logApiRequest("$BASE_PATH/create", "POST", mapOf("mobile" to mobile))

            val response =
                httpClient.post("$BASE_PATH/create") {
                    contentType(ContentType.Application.Json)
                    setBody(
                        CreateClientRequest(
                            firstName = firstName,
                            lastName = lastName,
                            mobile = mobile,
                            pin = pin,
                            confirmPin = confirmPin,
                        ),
                    )
                }

            Logger.logApiResponse("$BASE_PATH/create", response.status.value)

            when (response.status) {
                HttpStatusCode.Created -> response.body<CreateClientResponse>()
                HttpStatusCode.BadRequest -> {
                    val error = response.body<ErrorResponse>()
                    throw SoshoPayException.ValidationException(error.message)
                }
                HttpStatusCode.Conflict -> {
                    val error = response.body<ErrorResponse>()
                    throw SoshoPayException.ValidationException("User with this mobile number already exists")
                }
                else -> {
                    val error = response.body<ErrorResponse>()
                    throw SoshoPayException.ServerException(error.message, response.status.value)
                }
            }
        }

    override suspend fun updatePin(
        currentPin: String,
        newPin: String,
        confirmPin: String,
    ): Result<Unit> =
        safeCall {
            Logger.logApiRequest("$BASE_PATH/pin", "POST")

            val response =
                httpClient.post("$BASE_PATH/pin") {
                    contentType(ContentType.Application.Json)
                    setBody(
                        UpdatePinRequest(
                            currentPin = currentPin,
                            newPin = newPin,
                            confirmPin = confirmPin,
                        ),
                    )
                }

            Logger.logApiResponse("$BASE_PATH/pin", response.status.value)

            when (response.status) {
                HttpStatusCode.OK -> Unit
                HttpStatusCode.BadRequest -> {
                    val error = response.body<ErrorResponse>()
                    throw SoshoPayException.ValidationException(error.message)
                }
                HttpStatusCode.Unauthorized -> {
                    throw SoshoPayException.InvalidCredentialsException("Current PIN is incorrect")
                }
                else -> {
                    val error = response.body<ErrorResponse>()
                    throw SoshoPayException.ServerException(error.message, response.status.value)
                }
            }
        }

    override suspend fun startMobileChange(newMobile: String): Result<MobileChangeStartResponse> =
        safeCall {
            Logger.logApiRequest("$BASE_PATH/mobile/change/start", "POST")

            val response =
                httpClient.post("$BASE_PATH/mobile/change/start") {
                    contentType(ContentType.Application.Json)
                    setBody(StartMobileChangeRequest(newMobile = newMobile))
                }

            Logger.logApiResponse("$BASE_PATH/mobile/change/start", response.status.value)

            when (response.status) {
                HttpStatusCode.OK -> response.body<MobileChangeStartResponse>()
                HttpStatusCode.BadRequest -> {
                    val error = response.body<ErrorResponse>()
                    throw SoshoPayException.ValidationException(error.message)
                }
                HttpStatusCode.Conflict -> {
                    val error = response.body<ErrorResponse>()
                    throw SoshoPayException.ValidationException("Mobile number already in use")
                }
                else -> {
                    val error = response.body<ErrorResponse>()
                    throw SoshoPayException.ServerException(error.message, response.status.value)
                }
            }
        }

    override suspend fun verifyMobileChange(
        changeToken: String,
        otp: String,
    ): Result<MobileChangeVerifyResponse> =
        safeCall {
            Logger.logApiRequest("$BASE_PATH/mobile/change/verify", "POST")

            val response =
                httpClient.post("$BASE_PATH/mobile/change/verify") {
                    contentType(ContentType.Application.Json)
                    setBody(VerifyMobileChangeRequest(changeToken = changeToken, otp = otp))
                }

            Logger.logApiResponse("$BASE_PATH/mobile/change/verify", response.status.value)

            when (response.status) {
                HttpStatusCode.OK -> response.body<MobileChangeVerifyResponse>()
                HttpStatusCode.BadRequest -> {
                    val error = response.body<ErrorResponse>()
                    when {
                        error.message.contains("expired", ignoreCase = true) ->
                            throw SoshoPayException.OtpExpiredException(error.message)
                        error.message.contains("invalid", ignoreCase = true) ->
                            throw SoshoPayException.OtpInvalidException(error.message)
                        else -> throw SoshoPayException.ValidationException(error.message)
                    }
                }
                else -> {
                    val error = response.body<ErrorResponse>()
                    throw SoshoPayException.ServerException(error.message, response.status.value)
                }
            }
        }

    override suspend fun confirmMobileChange(changeToken: String): Result<MobileChangeConfirmResponse> =
        safeCall {
            Logger.logApiRequest("$BASE_PATH/mobile/change/confirm", "POST")

            val response =
                httpClient.post("$BASE_PATH/mobile/change/confirm") {
                    contentType(ContentType.Application.Json)
                    setBody(ConfirmMobileChangeRequest(changeToken = changeToken))
                }

            Logger.logApiResponse("$BASE_PATH/mobile/change/confirm", response.status.value)

            when (response.status) {
                HttpStatusCode.OK -> response.body<MobileChangeConfirmResponse>()
                HttpStatusCode.BadRequest -> {
                    val error = response.body<ErrorResponse>()
                    throw SoshoPayException.ValidationException(error.message)
                }
                else -> {
                    val error = response.body<ErrorResponse>()
                    throw SoshoPayException.ServerException(error.message, response.status.value)
                }
            }
        }
}
