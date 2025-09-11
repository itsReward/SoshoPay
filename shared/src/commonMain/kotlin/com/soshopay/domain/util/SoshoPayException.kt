package com.soshopay.domain.util

sealed class SoshoPayException(
    message: String,
    cause: Throwable? = null,
) : Exception(message, cause) {
    // Network Exceptions
    data class NetworkException(
        override val message: String,
        val code: Int? = null,
    ) : SoshoPayException(message)

    data class ConnectivityException(
        override val message: String = "No internet connection",
    ) : SoshoPayException(message)

    // Authentication Exceptions
    data class InvalidCredentialsException(
        override val message: String = "Invalid phone number or PIN",
    ) : SoshoPayException(message)

    data class OtpExpiredException(
        override val message: String = "OTP has expired",
    ) : SoshoPayException(message)

    data class OtpInvalidException(
        override val message: String = "Invalid OTP code",
    ) : SoshoPayException(message)

    data class MaxAttemptsExceededException(
        override val message: String = "Maximum OTP attempts exceeded",
    ) : SoshoPayException(message)

    data class TokenExpiredException(
        override val message: String = "Session has expired",
    ) : SoshoPayException(message)

    // Validation Exceptions
    data class ValidationException(
        override val message: String,
        val field: String? = null,
    ) : SoshoPayException(message)

    data class InvalidPhoneNumberException(
        override val message: String = "Invalid Zimbabwe phone number format",
    ) : SoshoPayException(message)

    data class InvalidNationalIdException(
        override val message: String = "Invalid Zimbabwe national ID format",
    ) : SoshoPayException(message)

    // File Upload Exceptions
    data class FileUploadException(
        override val message: String,
        val fileName: String? = null,
    ) : SoshoPayException(message)

    data class FileSizeExceededException(
        override val message: String = "File size exceeds 5MB limit",
        val maxSize: Long = 5 * 1024 * 1024,
    ) : SoshoPayException(message)

    data class UnsupportedFileTypeException(
        override val message: String = "Unsupported file type",
        val supportedTypes: List<String> = listOf("pdf", "jpg", "jpeg", "png"),
    ) : SoshoPayException(message)

    // Profile Exceptions
    data class ProfileIncompleteException(
        override val message: String = "Profile is incomplete",
    ) : SoshoPayException(message)

    data class DocumentVerificationRequiredException(
        override val message: String = "Document verification required",
    ) : SoshoPayException(message)

    // Server Exceptions
    data class ServerException(
        override val message: String,
        val code: Int,
    ) : SoshoPayException(message)

    data class UnknownException(
        override val message: String = "An unknown error occurred",
    ) : SoshoPayException(message)
}
