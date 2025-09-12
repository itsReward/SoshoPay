package com.soshopay.domain.repository

sealed class Result<out T> {
    data class Success<T>(
        val data: T,
    ) : Result<T>()

    data class Error(
        val exception: Throwable,
    ) : Result<Nothing>()

    data object Loading : Result<Nothing>()

    fun isSuccess(): Boolean = this is Success

    fun isError(): Boolean = this is Error

    fun isLoading(): Boolean = this is Loading

    fun getOrNull(): T? = if (this is Success) data else null

    fun getErrorOrNull(): Throwable? = if (this is Error) exception else null
}
