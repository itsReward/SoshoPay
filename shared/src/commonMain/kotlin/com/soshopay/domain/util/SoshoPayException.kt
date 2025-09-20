package com.soshopay.domain.util

import kotlin.jvm.JvmOverloads

/**
 * Enhanced domain-specific exceptions for the SoshoPay application.
 *
 * This sealed class provides comprehensive error categorization with specific
 * handling for security and keystore-related failures that can cause app crashes.
 *
 * Key enhancements:
 * - Added SecurityException for keystore and encryption failures
 * - Enhanced error context with error codes for precise handling
 * - Improved error messages for better user experience
 * - Specific handling for hardware security limitations
 *
 * @param message The error message describing the exception
 * @param cause The underlying cause of the exception, if any
 */
sealed class SoshoPayException(
    message: String,
    cause: Throwable? = null,
) : Exception(message, cause) {
    /**
     * Thrown when a network error occurs, such as a failed request or unexpected response.
     * @param message Description of the network error
     * @param code Optional HTTP or network error code
     */
    data class NetworkException(
        override val message: String,
        val code: Int? = null,
    ) : SoshoPayException(message)

    /**
     * Thrown when there is no internet connectivity.
     * @param message Description of the connectivity issue (default: "No internet connection")
     */
    data class ConnectivityException(
        override val message: String = "No internet connection",
    ) : SoshoPayException(message)

    /**
     * Thrown when user credentials are invalid during authentication.
     * @param message Description of the error (default: "Invalid phone number or PIN")
     */
    data class InvalidCredentialsException(
        override val message: String = "Invalid phone number or PIN",
    ) : SoshoPayException(message)

    /**
     * Thrown when an OTP has expired and is no longer valid.
     * @param message Description of the error (default: "OTP has expired")
     */
    data class OtpExpiredException(
        override val message: String = "OTP has expired",
    ) : SoshoPayException(message)

    /**
     * Thrown when an invalid OTP code is provided.
     * @param message Description of the error (default: "Invalid OTP code")
     */
    data class OtpInvalidException(
        override val message: String = "Invalid OTP code",
    ) : SoshoPayException(message)

    /**
     * Thrown when the maximum number of OTP attempts is exceeded.
     * @param message Description of the error (default: "Maximum OTP attempts exceeded")
     */
    data class MaxAttemptsExceededException(
        override val message: String = "Maximum OTP attempts exceeded",
    ) : SoshoPayException(message)

    data class UnauthorizedException(
        override val message: String = "Unauthorized",
    ) : SoshoPayException(message)

    data class UserAlreadyExistsException(
        override val message: String = "User already exists",
    ) : SoshoPayException(message)

    /**
     * Thrown when a session token has expired.
     * @param message Description of the error (default: "Session has expired")
     */
    data class TokenExpiredException(
        override val message: String = "Session has expired",
    ) : SoshoPayException(message)

    /**
     * Enhanced security exception for keystore, encryption, and hardware security failures.
     *
     * This exception provides specific handling for the StrongBoxUnavailableException
     * and related security issues that were causing the app to crash.
     *
     * @param message Description of the security error
     * @param errorCode Specific error code for precise handling
     * @param recoverySuggestion Optional suggestion for error recovery
     */
    data class SecurityException(
        override val message: String,
        val errorCode: String,
        val recoverySuggestion: String? = null,
    ) : SoshoPayException(message) {
        companion object {
            const val STRONGBOX_UNAVAILABLE = "STRONGBOX_UNAVAILABLE"
            const val KEYSTORE_FAILED = "KEYSTORE_FAILED"
            const val HARDWARE_SECURITY_UNAVAILABLE = "HARDWARE_SECURITY_UNAVAILABLE"
            const val ENCRYPTION_FAILED = "ENCRYPTION_FAILED"
            const val DECRYPTION_FAILED = "DECRYPTION_FAILED"
            const val KEY_GENERATION_FAILED = "KEY_GENERATION_FAILED"
            const val KEYSTORE_ACCESS_DENIED = "KEYSTORE_ACCESS_DENIED"
            const val OUT_OF_MEMORY_ERROR = "OUT_OF_MEMORY_ERROR"
        }

        /**
         * Creates a SecurityException for StrongBox unavailability.
         */
        fun createStrongBoxUnavailable(): SecurityException =
            SecurityException(
                message = "Hardware-backed security is not available on this device",
                errorCode = STRONGBOX_UNAVAILABLE,
                recoverySuggestion = "The app will use software-based security instead",
            )

        /**
         * Creates a SecurityException for keystore access failures.
         */
        fun createKeystoreFailure(details: String? = null): SecurityException =
            SecurityException(
                message = "Failed to access secure storage${if (details != null) ": $details" else ""}",
                errorCode = KEYSTORE_FAILED,
                recoverySuggestion = "Data will be stored with reduced security",
            )
    }

    /**
     * Thrown when a validation error occurs for a specific field.
     * @param message Description of the validation error
     * @param field The field that failed validation (optional)
     */
    data class ValidationException(
        override val message: String,
        val field: String? = null,
    ) : SoshoPayException(message)

    /**
     * Thrown when a server error occurs (5xx status codes).
     * @param message Description of the server error
     * @param code HTTP status code
     */
    data class ServerException(
        override val message: String,
        val code: Int,
    ) : SoshoPayException(message)

    /**
     * Thrown when a client error occurs (4xx status codes).
     * @param message Description of the client error
     * @param code HTTP status code
     */
    data class ClientException(
        override val message: String,
        val code: Int,
    ) : SoshoPayException(message)

    /**
     * Thrown when a file upload operation fails.
     * @param message Description of the upload error
     * @param fileName The name of the file that failed to upload (optional)
     */
    data class FileUploadException(
        override val message: String,
        val fileName: String? = null,
    ) : SoshoPayException(message)

    /**
     * Thrown when a profile operation encounters an error.
     * @param message Description of the profile error
     * @param operation The operation that failed (e.g., "UPDATE_ADDRESS", "UPLOAD_DOCUMENT")
     */
    data class ProfileException(
        override val message: String,
        val operation: String,
    ) : SoshoPayException(message)

    /**
     * Thrown when a loan operation encounters an error.
     * @param message Description of the loan error
     * @param loanId The ID of the loan related to the error (optional)
     * @param operation The operation that failed (optional)
     */
    data class LoanException(
        override val message: String,
        val loanId: String? = null,
        val operation: String? = null,
    ) : SoshoPayException(message)

    /**
     * Thrown when a payment operation encounters an error.
     * @param message Description of the payment error
     * @param paymentId The ID of the payment related to the error (optional)
     * @param operation The operation that failed (optional)
     */
    data class PaymentException(
        override val message: String,
        val paymentId: String? = null,
        val operation: String? = null,
    ) : SoshoPayException(message)

    /**
     * Thrown for unexpected or unknown errors.
     * @param message Description of the unknown error (default: "An unexpected error occurred")
     */
    data class UnknownException(
        override val message: String = "An unexpected error occurred",
    ) : SoshoPayException(message)

    /**
     * Enhanced parsing exception with context information.
     * @param message Description of the parsing error
     * @param dataType Type of data being parsed (e.g., "JSON", "DATE", "PHONE_NUMBER")
     * @param inputValue The value that failed to parse (optional, for debugging)
     */
    data class ParsingException(
        override val message: String,
        val dataType: String,
        val inputValue: String? = null,
    ) : SoshoPayException(message)

    /**
     * Thrown when required permissions are not granted.
     * @param message Description of the permission error
     * @param permission The specific permission that was denied
     */
    data class PermissionException(
        override val message: String,
        val permission: String,
    ) : SoshoPayException(message)

    /**
     * Provides user-friendly error messages based on the exception type.
     *
     * This method implements the Single Responsibility Principle by focusing
     * solely on generating appropriate user-facing error messages.
     */
    fun getUserFriendlyMessage(): String =
        when (this) {
            is SecurityException -> {
                when (errorCode) {
                    SecurityException.STRONGBOX_UNAVAILABLE ->
                        "Your device doesn't support hardware security. The app will use software security instead."
                    SecurityException.KEYSTORE_FAILED ->
                        "Secure storage is temporarily unavailable. Your data will be protected using an alternative method."
                    SecurityException.HARDWARE_SECURITY_UNAVAILABLE ->
                        "Advanced security features are not available on this device, but your data will still be protected."
                    else -> "A security feature is unavailable, but the app will continue to protect your data."
                }
            }
            is NetworkException -> "Please check your internet connection and try again."
            is ConnectivityException -> "No internet connection. Please connect to the internet and try again."
            is InvalidCredentialsException -> "Invalid phone number or PIN. Please check your credentials."
            is OtpExpiredException -> "The verification code has expired. Please request a new one."
            is OtpInvalidException -> "Invalid verification code. Please check and try again."
            is ValidationException -> message
            is ServerException -> "Server is temporarily unavailable. Please try again later."
            is TokenExpiredException -> "Your session has expired. Please log in again."
            else -> "Something went wrong. Please try again."
        }
}
