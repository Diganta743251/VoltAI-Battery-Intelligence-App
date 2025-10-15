package com.voltai.smartbatteryguardian.ui.state

import androidx.compose.runtime.Stable

/**
 * Enhanced UI state management for professional error handling and loading states
 * Follows Material 3 design guidelines for user feedback
 */
@Stable
sealed class UiState<out T> {
    object Loading : UiState<Nothing>()

    data class Success<T>(val data: T) : UiState<T>()

    data class Error(
        val exception: Throwable,
        val message: String = exception.localizedMessage ?: "Unknown error occurred",
        val canRetry: Boolean = true,
    ) : UiState<Nothing>()

    /**
     * Loading with partial data (e.g., refreshing existing data)
     */
    data class LoadingWithData<T>(val data: T) : UiState<T>()

    /**
     * Empty state (no data available)
     */
    object Empty : UiState<Nothing>()
}

/**
 * Material 3 compliant loading states for different scenarios
 */
sealed class LoadingState {
    object Idle : LoadingState()

    object Loading : LoadingState()

    object Refreshing : LoadingState()

    data class Error(
        val message: String,
        val canRetry: Boolean = true,
        val retryAction: (() -> Unit)? = null,
    ) : LoadingState()
}

/**
 * Battery Guru style error types for better user experience
 */
enum class ErrorType {
    NETWORK_ERROR,
    BATTERY_PERMISSION_DENIED,
    DATA_CORRUPTION,
    INSUFFICIENT_STORAGE,
    UNKNOWN_ERROR,
}

/**
 * Professional error handling with user-friendly messages
 */
data class BatteryGuruError(
    val type: ErrorType,
    val title: String,
    val description: String,
    val actionText: String? = null,
    val action: (() -> Unit)? = null,
    val canDismiss: Boolean = true,
) {
    companion object {
        fun networkError(retry: (() -> Unit)? = null) =
            BatteryGuruError(
                type = ErrorType.NETWORK_ERROR,
                title = "Connection Issue",
                description = "Unable to fetch battery data. Check your connection and try again.",
                actionText = "Retry",
                action = retry,
            )

        fun permissionError(openSettings: (() -> Unit)? = null) =
            BatteryGuruError(
                type = ErrorType.BATTERY_PERMISSION_DENIED,
                title = "Battery Access Required",
                description = "VoltAI needs battery access to provide accurate monitoring.",
                actionText = "Open Settings",
                action = openSettings,
            )

        fun dataError(clearCache: (() -> Unit)? = null) =
            BatteryGuruError(
                type = ErrorType.DATA_CORRUPTION,
                title = "Data Issue Detected",
                description = "Battery data appears corrupted. Clear cache to resolve.",
                actionText = "Clear Cache",
                action = clearCache,
            )

        fun storageError() =
            BatteryGuruError(
                type = ErrorType.INSUFFICIENT_STORAGE,
                title = "Storage Full",
                description = "Insufficient storage space for battery monitoring data.",
                actionText = null,
                action = null,
            )
    }
}

/**
 * Extension functions for handling UI states elegantly
 */
fun <T> UiState<T>.onSuccess(action: (T) -> Unit): UiState<T> {
    if (this is UiState.Success) action(data)
    return this
}

fun <T> UiState<T>.onError(action: (Throwable) -> Unit): UiState<T> {
    if (this is UiState.Error) action(exception)
    return this
}

fun <T> UiState<T>.onLoading(action: () -> Unit): UiState<T> {
    if (this is UiState.Loading) action()
    return this
}

/**
 * Get data from UiState safely
 */
fun <T> UiState<T>.getDataOrNull(): T? =
    when (this) {
        is UiState.Success -> data
        is UiState.LoadingWithData -> data
        else -> null
    }

/**
 * Check if state has any data (success or loading with data)
 */
fun <T> UiState<T>.hasData(): Boolean =
    when (this) {
        is UiState.Success -> true
        is UiState.LoadingWithData -> true
        else -> false
    }
