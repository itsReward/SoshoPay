package com.soshopay.domain.util

import co.touchlab.kermit.Logger as KermitLogger

object Logger {
    private val logger = KermitLogger.withTag("SoshoPay")

    fun d(
        message: String,
        tag: String = "DEBUG",
    ) {
        logger.d { "[$tag] $message" }
    }

    fun i(
        message: String,
        tag: String = "INFO",
    ) {
        logger.i { "[$tag] $message" }
    }

    fun w(
        message: String,
        tag: String = "WARNING",
        throwable: Throwable? = null,
    ) {
        logger.w(throwable) { "[$tag] $message" }
    }

    fun e(
        message: String,
        tag: String = "ERROR",
        throwable: Throwable? = null,
    ) {
        logger.e(throwable) { "[$tag] $message" }
    }

    // Specific logging methods for different components
    fun logApiRequest(
        endpoint: String,
        method: String,
        params: Map<String, Any>? = null,
    ) {
        val paramsStr = params?.let { " with params: $it" } ?: ""
        d("API Request: $method $endpoint$paramsStr", "API")
    }

    fun logApiResponse(
        endpoint: String,
        statusCode: Int,
        responseTime: Long? = null,
    ) {
        val timeStr = responseTime?.let { " (${it}ms)" } ?: ""
        d("API Response: $endpoint - $statusCode$timeStr", "API")
    }

    fun logApiError(
        endpoint: String,
        error: Throwable,
    ) {
        e("API Error: $endpoint - ${error.message}", "API", error)
    }

    fun logAuthEvent(
        event: String,
        phoneNumber: String? = null,
    ) {
        val phoneStr = phoneNumber?.let { " for ${it.take(3)}****${it.takeLast(3)}" } ?: ""
        i("Auth Event: $event$phoneStr", "AUTH")
    }

    fun logFileUpload(
        fileName: String,
        fileSize: Long,
        success: Boolean,
    ) {
        val status = if (success) "SUCCESS" else "FAILED"
        i("File Upload: $fileName (${fileSize}bytes) - $status", "FILE")
    }

    fun logProfileEvent(
        event: String,
        userId: String,
    ) {
        i("Profile Event: $event for user ${userId.take(8)}...", "PROFILE")
    }
}
