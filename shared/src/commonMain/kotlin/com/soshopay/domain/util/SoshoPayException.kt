package com.soshopay.domain.util

/**
 * Represents domain-specific exceptions for the SoshoPay application.
 *
 * This sealed class and its subclasses categorize and encapsulate various error scenarios
 * encountered in the domain layer, such as network, authentication, validation, file upload,
 * profile, and server errors. Each exception provides context-specific information to enable
 * precise error handling and user feedback.
 *
 * @param message The error message describing the exception.
 * @param cause The underlying cause of the exception, if any.
 */
sealed class SoshoPayException(
    message: String,
    cause: Throwable? = null,
) : Exception(message, cause) {
    /**
     * Thrown when a network error occurs, such as a failed request or unexpected response.
     * @param message Description of the network error.
     * @param code Optional HTTP or network error code.
     */
    data class NetworkException(
        override val message: String,
        val code: Int? = null,
    ) : SoshoPayException(message)

    /**
     * Thrown when there is no internet connectivity.
     * @param message Description of the connectivity issue (default: "No internet connection").
     */
    data class ConnectivityException(
        override val message: String = "No internet connection",
    ) : SoshoPayException(message)

    /**
     * Thrown when user credentials are invalid during authentication.
     * @param message Description of the error (default: "Invalid phone number or PIN").
     */
    data class InvalidCredentialsException(
        override val message: String = "Invalid phone number or PIN",
    ) : SoshoPayException(message)

    /**
     * Thrown when an OTP has expired and is no longer valid.
     * @param message Description of the error (default: "OTP has expired").
     */
    data class OtpExpiredException(
        override val message: String = "OTP has expired",
    ) : SoshoPayException(message)

    /**
     * Thrown when an invalid OTP code is provided.
     * @param message Description of the error (default: "Invalid OTP code").
     */
    data class OtpInvalidException(
        override val message: String = "Invalid OTP code",
    ) : SoshoPayException(message)

    /**
     * Thrown when the maximum number of OTP attempts is exceeded.
     * @param message Description of the error (default: "Maximum OTP attempts exceeded").
     */
    data class MaxAttemptsExceededException(
        override val message: String = "Maximum OTP attempts exceeded",
    ) : SoshoPayException(message)

    /**
     * Thrown when a session token has expired.
     * @param message Description of the error (default: "Session has expired").
     */
    data class TokenExpiredException(
        override val message: String = "Session has expired",
    ) : SoshoPayException(message)

    /**
     * Thrown when a validation error occurs for a specific field.
     * @param message Description of the validation error.
     * @param field Optional name of the field that failed validation.
     */
    data class ValidationException(
        override val message: String,
        val field: String? = null,
    ) : SoshoPayException(message)

    /**
     * Thrown when a phone number does not match the required Zimbabwe format.
     * @param message Description of the error (default: "Invalid Zimbabwe phone number format").
     */
    data class InvalidPhoneNumberException(
        override val message: String = "Invalid Zimbabwe phone number format",
    ) : SoshoPayException(message)

    /**
     * Thrown when a national ID does not match the required Zimbabwe format.
     * @param message Description of the error (default: "Invalid Zimbabwe national ID format").
     */
    data class InvalidNationalIdException(
        override val message: String = "Invalid Zimbabwe national ID format",
    ) : SoshoPayException(message)

    /**
     * Thrown when a file upload fails.
     * @param message Description of the upload error.
     * @param fileName Optional name of the file involved.
     */
    data class FileUploadException(
        override val message: String,
        val fileName: String? = null,
    ) : SoshoPayException(message)

    /**
     * Thrown when a file exceeds the allowed size limit.
     * @param message Description of the error (default: "File size exceeds 5MB limit").
     * @param maxSize Maximum allowed file size in bytes (default: 5MB).
     */
    data class FileSizeExceededException(
        override val message: String = "File size exceeds 5MB limit",
        val maxSize: Long = 5 * 1024 * 1024,
    ) : SoshoPayException(message)

    /**
     * Thrown when a file type is not supported for upload.
     * @param message Description of the error (default: "Unsupported file type").
     * @param supportedTypes List of supported file types.
     */
    data class UnsupportedFileTypeException(
        override val message: String = "Unsupported file type",
        val supportedTypes: List<String> = listOf("pdf", "jpg", "jpeg", "png"),
    ) : SoshoPayException(message)

    /**
     * Thrown when a user profile is incomplete and requires additional information.
     * @param message Description of the error (default: "Profile is incomplete").
     */
    data class ProfileIncompleteException(
        override val message: String = "Profile is incomplete",
    ) : SoshoPayException(message)

    /**
     * Thrown when document verification is required for the user profile.
     * @param message Description of the error (default: "Document verification required").
     */
    data class DocumentVerificationRequiredException(
        override val message: String = "Document verification required",
    ) : SoshoPayException(message)

    /**
     * Thrown when a server-side error occurs.
     * @param message Description of the server error.
     * @param code Server error code.
     */
    data class ServerException(
        override val message: String,
        val code: Int,
    ) : SoshoPayException(message)

    /**
     * Thrown when an unknown error occurs.
     * @param message Description of the error (default: "An unknown error occurred").
     */
    data class UnknownException(
        override val message: String = "An unknown error occurred",
    ) : SoshoPayException(message)

    /**
     * User registration exceptions
     */
    class UserAlreadyExistsException(
        message: String = "User with this phone number already exists",
    ) : SoshoPayException(message)

    class UserNotFoundException(
        message: String = "User not found",
    ) : SoshoPayException(message)

    /**
     * Authentication exceptions for login/authorization failures
     */
    data class UnauthorizedException(
        override val message: String,
    ) : SoshoPayException(message)
}

/**
 * Extension function to convert generic exceptions to SoshoPayException
 */
fun Throwable.toSoshoPayException(): SoshoPayException =
    when (this) {
        is SoshoPayException -> this
        /*is java.net.UnknownHostException -> SoshoPayException.NetworkException("No internet connection")
        is java.net.SocketTimeoutException -> SoshoPayException.NetworkException("Request timeout")
        is java.net.ConnectException -> SoshoPayException.NetworkException("Connection failed")*/
        else -> SoshoPayException.UnknownException(this.message ?: "An unexpected error occurred")
    }
