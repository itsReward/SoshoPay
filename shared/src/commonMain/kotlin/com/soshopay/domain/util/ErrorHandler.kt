package com.soshopay.domain.util

import kotlinx.coroutines.delay
import kotlin.math.pow

/**
 * Shared error handling utility for all platforms.
 *
 * Contains platform-agnostic error handling logic that applies
 * to both Android and iOS implementations.
 */
object ErrorHandler {
    enum class ErrorSeverity {
        LOW,
        MEDIUM,
        HIGH,
        CRITICAL,
    }

    enum class RecoveryStrategy {
        RETRY,
        FALLBACK,
        GRACEFUL_DEGRADE,
        FAIL_FAST,
    }

    /**
     * Platform-agnostic error handling logic.
     */
    fun handleSecurityError(
        error: Throwable,
        context: String,
    ): RecoveryStrategy {
        Logger.d("Handling security error in context: $context", "ERROR_HANDLER")

        return when {
            isKeystoreUnavailable(error) -> {
                Logger.w("Keystore unavailable - will fallback", "ERROR_HANDLER")
                RecoveryStrategy.FALLBACK
            }
            isHardwareSecurityUnavailable(error) -> {
                Logger.w("Hardware security unavailable - using software fallback", "ERROR_HANDLER")
                RecoveryStrategy.FALLBACK
            }
            else -> {
                Logger.e("Unknown security error: ${error.message}", "ERROR_HANDLER")
                RecoveryStrategy.GRACEFUL_DEGRADE
            }
        }
    }

    fun determineErrorSeverity(
        error: Throwable,
        context: String,
    ): ErrorSeverity =
        when {
            isKeystoreUnavailable(error) -> ErrorSeverity.MEDIUM
            isMemoryError(error) -> ErrorSeverity.CRITICAL
            else -> ErrorSeverity.HIGH
        }

    /**
     * Platform-agnostic error classification methods.
     * Platform modules will implement the specific detection logic.
     */
    private fun isKeystoreUnavailable(error: Throwable): Boolean =
        error.message?.contains("keystore", ignoreCase = true) == true ||
            error.message?.contains("StrongBox", ignoreCase = true) == true

    private fun isHardwareSecurityUnavailable(error: Throwable): Boolean =
        error.message?.contains("hardware", ignoreCase = true) == true ||
            error.message?.contains("StrongBox", ignoreCase = true) == true

    private fun isMemoryError(error: Throwable): Boolean {
        // Use generic Exception checks since OutOfMemoryError is JVM-specific
        return error.message?.contains("memory", ignoreCase = true) == true ||
            error.message?.contains("out of memory", ignoreCase = true) == true
    }

    /**
     * Generic execution with recovery - works on all platforms.
     * Made fallbackOperation non-nullable and added noinline to fix KMP issues.
     */
    suspend inline fun <T> executeWithRecovery(
        context: String,
        maxRetries: Int = 3,
        operation: suspend () -> T,
        noinline fallbackOperation: (suspend () -> T),
    ): Result<T> {
        var lastError: Throwable? = null
        var shouldUseFallback = false

        for (attempt in 0 until maxRetries) {
            try {
                Logger.d("Executing operation (attempt ${attempt + 1}/$maxRetries)", context)
                val result = operation()
                Logger.d("Operation completed successfully", context)
                return Result.Success(result)
            } catch (e: Exception) {
                lastError = e
                val strategy = handleSecurityError(e, context)

                when (strategy) {
                    RecoveryStrategy.FAIL_FAST -> {
                        Logger.e("Critical error - failing fast", context, e)
                        return Result.Error(e)
                    }
                    RecoveryStrategy.FALLBACK -> {
                        Logger.w("Using fallback operation", context)
                        shouldUseFallback = true
                        break
                    }
                    RecoveryStrategy.RETRY -> {
                        Logger.w("Retrying operation (attempt ${attempt + 1}/$maxRetries)", context, e)
                        if (attempt < maxRetries - 1) {
                            delay(calculateBackoffDelay(attempt))
                        }
                    }
                    RecoveryStrategy.GRACEFUL_DEGRADE -> {
                        Logger.w("Gracefully degrading", context)
                        shouldUseFallback = true
                        break
                    }
                }
            }
        }

        // Try fallback operation if needed
        if (shouldUseFallback) {
            try {
                Logger.w("Executing fallback operation", context)
                val result = fallbackOperation()
                Logger.i("Fallback operation completed successfully", context)
                return Result.Success(result)
            } catch (e: Exception) {
                Logger.e("Fallback operation also failed", context, e)
                lastError = e
            }
        }

        val finalError = lastError ?: Exception("Unknown error in $context")
        Logger.e("All recovery attempts failed", context, finalError)
        return Result.Error(finalError)
    }

    /**
     * Synchronous version for non-suspend operations.
     */
    inline fun <T> executeWithRecoverySync(
        context: String,
        maxRetries: Int = 3,
        operation: () -> T,
        noinline fallbackOperation: () -> T,
    ): Result<T> {
        var lastError: Throwable? = null
        var shouldUseFallback = false

        for (attempt in 0 until maxRetries) {
            try {
                Logger.d("Executing operation (attempt ${attempt + 1}/$maxRetries)", context)
                val result = operation()
                Logger.d("Operation completed successfully", context)
                return Result.Success(result)
            } catch (e: Exception) {
                lastError = e
                val strategy = handleSecurityError(e, context)

                when (strategy) {
                    RecoveryStrategy.FAIL_FAST -> {
                        Logger.e("Critical error - failing fast", context, e)
                        return Result.Error(e)
                    }
                    RecoveryStrategy.FALLBACK -> {
                        Logger.w("Using fallback operation", context)
                        shouldUseFallback = true
                        break
                    }
                    RecoveryStrategy.RETRY -> {
                        Logger.w("Retrying operation (attempt ${attempt + 1}/$maxRetries)", context, e)
                        // No delay in sync version to avoid blocking
                    }
                    RecoveryStrategy.GRACEFUL_DEGRADE -> {
                        Logger.w("Gracefully degrading", context)
                        shouldUseFallback = true
                        break
                    }
                }
            }
        }

        // Try fallback operation if needed
        if (shouldUseFallback) {
            try {
                Logger.w("Executing fallback operation", context)
                val result = fallbackOperation()
                Logger.i("Fallback operation completed successfully", context)
                return Result.Success(result)
            } catch (e: Exception) {
                Logger.e("Fallback operation also failed", context, e)
                lastError = e
            }
        }

        val finalError = lastError ?: Exception("Unknown error in $context")
        Logger.e("All recovery attempts failed", context, finalError)
        return Result.Error(finalError)
    }

    /**
     * Calculates exponential backoff delay for retries.
     */
    fun calculateBackoffDelay(attempt: Int): Long = (1000 * 2.0.pow(attempt.toDouble())).toLong().coerceAtMost(10000)
}
