package com.soshopay.domain.repository

import com.soshopay.domain.model.AuthToken
import com.soshopay.domain.model.OtpSession
import com.soshopay.domain.model.User
import com.soshopay.domain.util.Result

/**
 * Repository abstraction for authentication-related operations in the SoshoPay domain.
 *
 * Implementations of this interface handle user authentication workflows, including OTP verification,
 * PIN setup and updates, login/logout, token refresh, user creation, and mobile number changes.
 * All operations return a [Result] type for robust error handling and success/failure reporting.
 *
 * Typical usage scenarios:
 * - Send and verify OTP codes for user authentication.
 * - Set or update user PINs for secure access.
 * - Log in and log out users, and refresh authentication tokens.
 * - Create new user accounts and manage user profiles.
 * - Initiate and confirm mobile number changes with OTP verification.
 *
 * All methods are suspend functions to support asynchronous and coroutine-based execution.
 */
interface AuthRepository {
    /**
     * Sends an OTP code to the specified [phoneNumber] for authentication.
     * @param phoneNumber The user's phone number.
     * @return [Result] containing the [OtpSession] if successful, or an error.
     */
    suspend fun sendOtp(phoneNumber: String): Result<OtpSession>

    /**
     * Verifies the entered OTP code for the given [otpSession].
     * @param otpSession The OTP session information.
     * @param enteredCode The OTP code entered by the user.
     * @return [Result] containing a temporary token if successful, or an error.
     */
    suspend fun verifyOtp(
        otpSession: OtpSession,
        enteredCode: String,
    ): Result<String>

    /**
     * Sets a new PIN for the user using a temporary token and phone number.
     * @param tempToken The temporary token from OTP verification.
     * @param pin The new PIN to set.
     * @param phoneNumber The user's phone number.
     * @return [Result] containing the [AuthToken] if successful, or an error.
     */
    suspend fun setPin(
        tempToken: String,
        pin: String,
        phoneNumber: String,
    ): Result<AuthToken>

    /**
     * Authenticates the user with the provided [phoneNumber] and [pin].
     * @param phoneNumber The user's phone number.
     * @param pin The user's PIN.
     * @return [Result] containing the [AuthToken] if successful, or an error.
     */
    suspend fun login(
        phoneNumber: String,
        pin: String,
    ): Result<AuthToken>

    /**
     * Refreshes the current authentication token.
     * @return [Result] containing the new [AuthToken] if successful, or an error.
     */
    suspend fun refreshToken(): Result<AuthToken>

    /**
     * Logs out the current user and clears authentication data.
     * @return [Result] containing [Unit] if successful, or an error.
     */
    suspend fun logout(): Result<Unit>

    /**
     * Checks if a user is currently logged in.
     * @return True if logged in, false otherwise.
     */
    suspend fun isLoggedIn(): Boolean

    /**
     * Retrieves the currently authenticated user's profile.
     * @return The [User] if logged in, or null otherwise.
     */
    suspend fun getCurrentUser(): User?

    /**
     * Updates the user's PIN from [currentPin] to [newPin].
     * @param currentPin The user's current PIN.
     * @param newPin The new PIN to set.
     * @return [Result] containing [Unit] if successful, or an error.
     */
    suspend fun updatePin(
        currentPin: String,
        newPin: String,
    ): Result<Unit>

    /**
     * Creates a new client/user with the provided details.
     * @param firstName The user's first name.
     * @param lastName The user's last name.
     * @param phoneNumber The user's phone number.
     * @param pin The user's PIN.
     * @return [Result] containing the created [User] if successful, or an error.
     */
    suspend fun createClient(
        firstName: String,
        lastName: String,
        phoneNumber: String,
        pin: String,
    ): Result<User>

    /**
     * Initiates a mobile number change for the user.
     * @param newMobile The new mobile number to set.
     * @return [Result] containing a change token if successful, or an error.
     */
    suspend fun startMobileChange(newMobile: String): Result<String>

    /**
     * Verifies the OTP for a pending mobile number change.
     * @param changeToken The token for the mobile change session.
     * @param otp The OTP code entered by the user.
     * @return [Result] containing a confirmation token if successful, or an error.
     */
    suspend fun verifyMobileChange(
        changeToken: String,
        otp: String,
    ): Result<String>

    /**
     * Confirms the mobile number change using the provided [changeToken].
     * @param changeToken The token for the mobile change session.
     * @return [Result] containing a success message if successful, or an error.
     */
    suspend fun confirmMobileChange(changeToken: String): Result<String>
}
