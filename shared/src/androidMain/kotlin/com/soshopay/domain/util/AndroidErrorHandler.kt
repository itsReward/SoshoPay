package com.soshopay.platform.util

import android.os.Build
import android.security.keystore.StrongBoxUnavailableException
import androidx.annotation.RequiresApi
import com.soshopay.domain.util.ErrorHandler
import com.soshopay.domain.util.Logger
import com.soshopay.domain.util.Result

/**
 * Android-specific error handling extensions.
 *
 * Handles Android-specific error types and provides platform-specific
 * error detection and recovery mechanisms.
 */
object AndroidErrorHandler {
    /**
     * Android-specific keystore error detection.
     */
    @RequiresApi(Build.VERSION_CODES.P)
    fun isAndroidKeystoreError(error: Throwable): Boolean =
        when (error) {
            is StrongBoxUnavailableException -> true
            is SecurityException -> {
                error.message?.contains("keystore", ignoreCase = true) == true ||
                    error.message?.contains("StrongBox", ignoreCase = true) == true
            }
            is RuntimeException -> {
                error.cause is StrongBoxUnavailableException
            }
            else -> false
        }

    /**
     * Handle Android-specific keystore errors with detailed detection.
     */
    @RequiresApi(Build.VERSION_CODES.P)
    fun handleAndroidKeystoreError(
        error: Throwable,
        context: String,
    ): ErrorHandler.RecoveryStrategy {
        when (error) {
            is StrongBoxUnavailableException -> {
                Logger.w("StrongBox unavailable on Android - will fallback to software keystore", "ANDROID_ERROR_HANDLER")
                return ErrorHandler.RecoveryStrategy.FALLBACK
            }
            is SecurityException -> {
                when {
                    error.message?.contains("StrongBox", ignoreCase = true) == true -> {
                        Logger.w("Android StrongBox unavailable - using software fallback", "ANDROID_ERROR_HANDLER")
                        return ErrorHandler.RecoveryStrategy.FALLBACK
                    }
                    error.message?.contains("keystore", ignoreCase = true) == true -> {
                        Logger.w("Android keystore error - attempting fallback", "ANDROID_ERROR_HANDLER")
                        return ErrorHandler.RecoveryStrategy.FALLBACK
                    }
                }
            }
            is OutOfMemoryError -> {
                Logger.e("Android OOM error detected", "ANDROID_ERROR_HANDLER")
                return ErrorHandler.RecoveryStrategy.FAIL_FAST
            }
        }

        // Fallback to shared error handling
        return ErrorHandler.handleSecurityError(error, context)
    }

    /**
     * Android-specific synchronous error execution wrapper.
     */
    fun <T> executeWithAndroidRecovery(
        context: String,
        maxRetries: Int = 3,
        operation: () -> T,
        fallbackOperation: () -> T,
    ): Result<T> =
        ErrorHandler.executeWithRecoverySync(
            context = context,
            maxRetries = maxRetries,
            operation = operation,
            fallbackOperation = fallbackOperation,
        )

    /**
     * Android-specific suspend error execution wrapper.
     */
    suspend fun <T> executeWithAndroidRecoveryAsync(
        context: String,
        maxRetries: Int = 3,
        operation: suspend () -> T,
        fallbackOperation: suspend () -> T,
    ): Result<T> =
        ErrorHandler.executeWithRecovery(
            context = context,
            maxRetries = maxRetries,
            operation = operation,
            fallbackOperation = fallbackOperation,
        )
}
