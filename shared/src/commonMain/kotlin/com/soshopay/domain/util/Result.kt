package com.soshopay.domain.util

sealed class Result<out T> {
    data class Success<T>(
        val data: T,
    ) : Result<T>()

    data class Error(
        val exception: Throwable,
    ) : Result<Nothing>()

    data object Loading : Result<Nothing>()

    val isSuccess: Boolean get() = this is Success
    val isError: Boolean get() = this is Error
    val isLoading: Boolean get() = this is Loading

    fun getOrNull(): T? =
        when (this) {
            is Success -> data
            else -> null
        }

    fun getErrorOrNull(): Throwable? =
        when (this) {
            is Error -> exception
            else -> null
        }

    inline fun onSuccess(action: (value: T) -> Unit): Result<T> {
        if (this is Success) action(data)
        return this
    }

    inline fun onError(action: (error: Throwable) -> Unit): Result<T> {
        if (this is Error) action(exception)
        return this
    }

    inline fun <R> map(transform: (T) -> R): Result<R> =
        when (this) {
            is Success -> Success(transform(data))
            is Error -> this
            is Loading -> this
        }
}

// Utility functions
suspend inline fun <T> safeCall(crossinline call: suspend () -> T): Result<T> =
    try {
        Result.Success(call())
    } catch (e: Exception) {
        Result.Error(e)
    }
