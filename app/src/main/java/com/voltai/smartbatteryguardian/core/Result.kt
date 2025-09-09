package com.voltai.smartbatteryguardian.core

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

/**
 * A generic wrapper for handling success/error states in a type-safe way.
 * Provides comprehensive error handling with contextual information.
 */
sealed class Result<T> {
    /**
     * Represents a successful result with data
     */
    data class Success<T>(val data: T) : Result<T>()

    /**
     * Represents an error state with comprehensive error information
     */
    data class Error(
        val exception: Throwable,
        val errorCode: String? = null,
        val errorMessage: String = exception.message ?: "Unknown error occurred",
        val timestamp: Long = System.currentTimeMillis(),
        val recoverable: Boolean = true,
        val userFriendlyMessage: String? = null
    ) : Result<Nothing>()

    /**
     * Represents a loading state
     */
    data object Loading : Result<Nothing>()

    /**
     * Represents an empty state (no data available)
     */
    data object Empty : Result<Nothing>()

    // Extension properties for easy state checking
    val isSuccess: Boolean get() = this is Success
    val isError: Boolean get() = this is Error
    val isLoading: Boolean get() = this is Loading
    val isEmpty: Boolean get() = this is Empty

    /**
     * Returns data if successful, null otherwise
     */
    fun getOrNull(): T? = when (this) {
        is Success -> data
        else -> null
    }

    /**
     * Returns data if successful, default value otherwise
     */
    fun getOrDefault(default: T): T = when (this) {
        is Success -> data
        else -> default
    }

    /**
     * Returns data if successful, throws exception if error
     */
    fun getOrThrow(): T = when (this) {
        is Success -> data
        is Error -> throw exception
        is Loading -> throw IllegalStateException("Result is still loading")
        is Empty -> throw NoSuchElementException("Result is empty")
    }

    /**
     * Transforms successful data while preserving error states
     */
    inline fun <R> map(transform: (T) -> R): Result<R> = when (this) {
        is Success -> try {
            Success(transform(data))
        } catch (e: Exception) {
            Error(e, recoverable = false) as Result<R>
        }
        is Error -> this as Result<R>
        is Loading -> Loading as Result<R>
        is Empty -> Empty as Result<R>
    }

    /**
     * Flat maps the result, useful for chaining operations
     */
    inline fun <R> flatMap(transform: (T) -> Result<R>): Result<R> = when (this) {
        is Success -> try {
            transform(data)
        } catch (e: Exception) {
            Error(e, recoverable = false) as Result<R>
        }
        is Error -> this as Result<R>
        is Loading -> Loading as Result<R>
        is Empty -> Empty as Result<R>
    }

    /**
     * Executes action on successful data
     */
    inline fun onSuccess(action: (T) -> Unit): Result<T> {
        if (this is Success) {
            action(data)
        }
        return this
    }

    /**
     * Executes action on error
     */
    inline fun onError(action: (Error) -> Unit): Result<T> {
        if (this is Error) {
            action(this)
        }
        return this
    }

    /**
     * Executes action on loading
     */
    inline fun onLoading(action: () -> Unit): Result<T> {
        if (this is Loading) {
            action()
        }
        return this
    }
}

/**
 * Executes the given block and wraps the result in a Result wrapper
 */
inline fun <T> safeCall(
    errorCode: String? = null,
    userFriendlyMessage: String? = null,
    action: () -> T
): Result<T> = try {
    Result.Success(action())
} catch (e: Exception) {
    Result.Error(
        exception = e,
        errorCode = errorCode,
        userFriendlyMessage = userFriendlyMessage ?: getDefaultUserMessage(e)
    ) as Result<T>
}

/**
 * Executes the given suspend block and wraps the result in a Result wrapper
 */
suspend inline fun <T> safeSuspendCall(
    errorCode: String? = null,
    userFriendlyMessage: String? = null,
    action: suspend () -> T
): Result<T> = try {
    Result.Success(action())
} catch (e: Exception) {
    Result.Error(
        exception = e,
        errorCode = errorCode,
        userFriendlyMessage = userFriendlyMessage ?: getDefaultUserMessage(e)
    ) as Result<T>
}

/**
 * Converts a Flow to emit Result wrapper
 */
fun <T> Flow<T>.asResult(): Flow<Result<T>> = this
    .map<T, Result<T>> { Result.Success(it) }
    .onStart { emit(Result.Loading as Result<T>) }
    .catch { exception ->
        emit(
            Result.Error(
                exception = exception,
                userFriendlyMessage = getDefaultUserMessage(exception)
            ) as Result<T>
        )
    }

/**
 * Filters successful results from a Flow of Results
 */
fun <T> Flow<Result<T>>.filterSuccess(): Flow<T> = this
    .map { result ->
        when (result) {
            is Result.Success -> result.data
            else -> null
        }
    }
    .map { it ?: throw IllegalStateException("Non-success result encountered") }

/**
 * Gets user-friendly error messages based on exception type
 */
fun getDefaultUserMessage(exception: Throwable): String = when (exception) {
    is java.net.UnknownHostException -> "No internet connection available"
    is java.net.SocketTimeoutException -> "Connection timeout. Please try again"
    is java.io.IOException -> "Network error occurred. Please check your connection"
    is SecurityException -> "Permission denied. Please check app permissions"
    is IllegalArgumentException -> "Invalid input provided"
    is IllegalStateException -> "App is in an invalid state. Please restart"
    is java.util.concurrent.CancellationException -> "Operation was cancelled"
    else -> "An unexpected error occurred. Please try again"
}

/**
 * Specific error types for better error handling
 */
sealed class AppError : Exception() {
    abstract val errorCode: String
    abstract val userFriendlyMessage: String
    abstract val recoverable: Boolean

    data class NetworkError(
        override val message: String,
        val statusCode: Int? = null
    ) : AppError() {
        override val errorCode = "NETWORK_ERROR"
        override val userFriendlyMessage = "Network connection problem"
        override val recoverable = true
    }

    data class DatabaseError(
        override val message: String,
        val query: String? = null
    ) : AppError() {
        override val errorCode = "DATABASE_ERROR"
        override val userFriendlyMessage = "Data storage problem"
        override val recoverable = true
    }

    data class ValidationError(
        override val message: String,
        val field: String? = null
    ) : AppError() {
        override val errorCode = "VALIDATION_ERROR"
        override val userFriendlyMessage = "Invalid input provided"
        override val recoverable = true
    }

    data class PermissionError(
        override val message: String,
        val permission: String
    ) : AppError() {
        override val errorCode = "PERMISSION_ERROR"
        override val userFriendlyMessage = "Permission required to continue"
        override val recoverable = true
    }

    data class SecurityError(
        override val message: String,
        val securityIssue: String
    ) : AppError() {
        override val errorCode = "SECURITY_ERROR"
        override val userFriendlyMessage = "Security verification failed"
        override val recoverable = false
    }

    data class BusinessLogicError(
        override val message: String,
        val operation: String
    ) : AppError() {
        override val errorCode = "BUSINESS_LOGIC_ERROR"
        override val userFriendlyMessage = "Operation cannot be completed"
        override val recoverable = true
    }

    data class UnknownError(
        override val message: String = "Unknown error occurred"
    ) : AppError() {
        override val errorCode = "UNKNOWN_ERROR"
        override val userFriendlyMessage = "Something went wrong"
        override val recoverable = true
    }
}

/**
 * Extension functions for common Result operations
 */

/**
 * Combines two Results into one
 */
fun <T, R, S> Result<T>.combine(
    other: Result<R>,
    transform: (T, R) -> S
): Result<S> = when {
    this is Result.Success && other is Result.Success -> {
        safeCall { transform(this.data, other.data) }
    }
    this is Result.Error -> this as Result<S>
    other is Result.Error -> other as Result<S>
    this is Result.Loading || other is Result.Loading -> Result.Loading as Result<S>
    else -> Result.Empty as Result<S>
}

/**
 * Retries an operation with exponential backoff
 */
suspend fun <T> Result<T>.retryWithBackoff(
    maxRetries: Int = 3,
    initialDelayMs: Long = 1000,
    maxDelayMs: Long = 10000,
    backoffMultiplier: Double = 2.0,
    retryCondition: (Result.Error) -> Boolean = { it.recoverable },
    operation: suspend () -> Result<T>
): Result<T> {
    var currentResult = this
    var delayMs = initialDelayMs

    repeat(maxRetries) { attemptNumber ->
        when (val result = currentResult) {
            is Result.Error -> {
                if (retryCondition(result)) {
                    kotlinx.coroutines.delay(delayMs)
                    currentResult = operation()
                    delayMs = minOf(maxDelayMs, (delayMs * backoffMultiplier).toLong())
                } else {
                    return result
                }
            }
            else -> return currentResult
        }
    }

    return currentResult
}

/**
 * Creates a Result from a nullable value
 */
fun <T> T?.toResult(errorMessage: String = "Value is null"): Result<T> =
    this?.let { Result.Success(it) } ?: Result.Error(
        IllegalArgumentException(errorMessage),
        userFriendlyMessage = "Required data is missing"
    ) as Result<T>
