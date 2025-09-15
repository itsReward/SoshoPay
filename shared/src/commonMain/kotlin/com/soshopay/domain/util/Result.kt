package com.soshopay.domain.util

/**
 * Represents the result of an operation, encapsulating success, error, or loading states.
 *
 * This sealed class is used to model the outcome of asynchronous or synchronous operations,
 * such as network requests or computations, in a type-safe manner. It provides utility methods
 * for handling and transforming results, and for extracting data or errors.
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
    val isSuccess: Boolean get() = this is Success

    /**
     * Returns true if this result is an [Error].
     */
    val isError: Boolean get() = this is Error

    /**
     * Returns true if this result is [Loading].
     */
    val isLoading: Boolean get() = this is Loading

    /**
     * Returns the data if this result is [Success], or null otherwise.
     */
    fun getOrNull(): T? =
        when (this) {
            is Success -> data
            else -> null
        }

    /**
     * Returns the exception if this result is [Error], or null otherwise.
     */
    fun getErrorOrNull(): Throwable? =
        when (this) {
            is Error -> exception
            else -> null
        }

    /**
     * Executes [action] if this result is [Success]. Returns this result for chaining.
     *
     * @param action The function to execute with the success value.
     */
    inline fun onSuccess(action: (value: T) -> Unit): Result<T> {
        if (this is Success) action(data)
        return this
    }

    /**
     * Executes [action] if this result is [Error]. Returns this result for chaining.
     *
     * @param action The function to execute with the error value.
     */
    inline fun onError(action: (error: Throwable) -> Unit): Result<T> {
        if (this is Error) action(exception)
        return this
    }

    /**
     * Transforms the success value using [transform] if this result is [Success].
     * Returns [Error] or [Loading] unchanged.
     *
     * @param transform The function to transform the success value.
     */
    inline fun <R> map(transform: (T) -> R): Result<R> =
        when (this) {
            is Success -> Success(transform(data))
            is Error -> this
            is Loading -> this
        }
}

/**
 * Executes [call] and wraps its result in [Result.Success] if successful, or [Result.Error] if an exception is thrown.
 *
 * @param call The suspend function to execute.
 * @return [Result.Success] if the call succeeds, [Result.Error] if an exception is thrown.
 */
suspend inline fun <T> safeCall(crossinline call: suspend () -> T): Result<T> =
    try {
        Result.Success(call())
    } catch (e: Exception) {
        Result.Error(e)
    }
