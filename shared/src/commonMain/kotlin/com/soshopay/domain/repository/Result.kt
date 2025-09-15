package com.soshopay.domain.repository

/**
 * Represents the result of an operation, encapsulating success, error, or loading states.
 *
 * This sealed class is used to model the outcome of asynchronous or synchronous operations,
 * such as network requests or computations, in a type-safe manner. It provides utility methods
 * for handling and extracting results, enabling robust error handling and clear success/failure reporting.
 *
 * @param T The type of data returned on success.
 */
sealed class Result<out T> {
    /**
     * Indicates a successful result containing the returned data.
     *
     * @param data The data returned by the operation.
     */
    data class Success<T>(
        val data: T,
    ) : Result<T>()

    /**
     * Indicates an error result containing the exception that occurred.
     *
     * @param exception The exception describing the error.
     */
    data class Error(
        val exception: Throwable,
    ) : Result<Nothing>()

    /**
     * Indicates that the operation is currently in progress (e.g., loading state).
     */
    data object Loading : Result<Nothing>()

    /**
     * Returns true if this result is a [Success].
     */
    fun isSuccess(): Boolean = this is Success

    /**
     * Returns true if this result is an [Error].
     */
    fun isError(): Boolean = this is Error

    /**
     * Returns true if this result is [Loading].
     */
    fun isLoading(): Boolean = this is Loading

    /**
     * Returns the data if this result is [Success], or null otherwise.
     */
    fun getOrNull(): T? = if (this is Success) data else null

    /**
     * Returns the exception if this result is [Error], or null otherwise.
     */
    fun getErrorOrNull(): Throwable? = if (this is Error) exception else null
}
