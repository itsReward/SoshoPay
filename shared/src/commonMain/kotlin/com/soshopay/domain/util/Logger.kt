package com.soshopay.domain.util

import co.touchlab.kermit.Logger as KermitLogger

/**
 * SoshoPay Logger utility for structured and component-specific logging.
 *
 * This singleton object wraps the Kermit logging library and provides methods for logging
 * messages at different levels (debug, info, warning, error) with optional tags and exceptions.
 * It also offers specialized logging functions for API requests/responses/errors, authentication events,
 * file uploads, and profile events, ensuring consistent and readable log output across the application.
 *
 * Usage examples:
 * - Logger.d("Debug message")
 * - Logger.logApiRequest(endpoint = "/login", method = "POST", params = mapOf("phone" to "123"))
 *
 * @see co.touchlab.kermit.Logger
 */
object Logger {
    private val logger = KermitLogger.withTag("SoshoPay")

    /**
     * Logs a debug message with an optional tag.
     * @param message The message to log.
     * @param tag The tag to categorize the log (default: "DEBUG").
     */
    fun d(
        message: String,
        tag: String = "DEBUG",
    ) {
        logger.d { "[$tag] $message" }
    }

    /**
     * Logs an info message with an optional tag.
     * @param message The message to log.
     * @param tag The tag to categorize the log (default: "INFO").
     */
    fun i(
        message: String,
        tag: String = "INFO",
    ) {
        logger.i { "[$tag] $message" }
    }

    /**
     * Logs a warning message with an optional tag and throwable.
     * @param message The message to log.
     * @param tag The tag to categorize the log (default: "WARNING").
     * @param throwable Optional exception to include in the log.
     */
    fun w(
        message: String,
        tag: String = "WARNING",
        throwable: Throwable? = null,
    ) {
        logger.w(throwable) { "[$tag] $message" }
    }

    /**
     * Logs an error message with an optional tag and throwable.
     * @param message The message to log.
     * @param tag The tag to categorize the log (default: "ERROR").
     * @param throwable Optional exception to include in the log.
     */
    fun e(
        message: String,
        tag: String = "ERROR",
        throwable: Throwable? = null,
    ) {
        logger.e(throwable) { "[$tag] $message" }
    }

    /**
     * Logs an API request with endpoint, method, and optional parameters.
     * @param endpoint The API endpoint being called.
     * @param method The HTTP method used.
     * @param params Optional parameters sent with the request.
     */
    fun logApiRequest(
        endpoint: String,
        method: String,
        params: Map<String, Any>? = null,
    ) {
        val paramsStr = params?.let { " with params: $it" } ?: ""
        d("API Request: $method $endpoint$paramsStr", "API")
    }

    /**
     * Logs an API response with endpoint, status code, and optional response time.
     * @param endpoint The API endpoint that responded.
     * @param statusCode The HTTP status code returned.
     * @param responseTime Optional response time in milliseconds.
     */
    fun logApiResponse(
        endpoint: String,
        statusCode: Int,
        responseTime: Long? = null,
    ) {
        val timeStr = responseTime?.let { " (${it}ms)" } ?: ""
        d("API Response: $endpoint - $statusCode$timeStr", "API")
    }

    /**
     * Logs an API error with endpoint and exception details.
     * @param endpoint The API endpoint where the error occurred.
     * @param error The exception thrown.
     */
    fun logApiError(
        endpoint: String,
        error: Throwable,
    ) {
        e("API Error: $endpoint - ${error.message}", "API", error)
    }

    /**
     * Logs an authentication event with optional phone number.
     * @param event The authentication event description.
     * @param phoneNumber Optional phone number involved in the event.
     */
    fun logAuthEvent(
        event: String,
        phoneNumber: String? = null,
    ) {
        val phoneStr = phoneNumber?.let { " for ${it.take(3)}****${it.takeLast(3)}" } ?: ""
        i("Auth Event: $event$phoneStr", "AUTH")
    }

    /**
     * Logs a file upload event with file name, size, and success status.
     * @param fileName The name of the file uploaded.
     * @param fileSize The size of the file in bytes.
     * @param success True if upload succeeded, false otherwise.
     */
    fun logFileUpload(
        fileName: String,
        fileSize: Long,
        success: Boolean,
    ) {
        val status = if (success) "SUCCESS" else "FAILED"
        i("File Upload: $fileName (${fileSize}bytes) - $status", "FILE")
    }

    /**
     * Logs a profile-related event for a user.
     * @param event The profile event description.
     * @param userId The user ID involved in the event.
     */
    fun logProfileEvent(
        event: String,
        userId: String,
    ) {
        i("Profile Event: $event for user ${userId.take(8)}...", "PROFILE")
    }
}
