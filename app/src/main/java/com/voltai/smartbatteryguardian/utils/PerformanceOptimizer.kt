package com.voltai.smartbatteryguardian.utils

import androidx.compose.runtime.*
import androidx.compose.ui.graphics.ImageBitmap
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.CoroutineContext
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

/**
 * Performance optimization utilities for premium app experience
 * Includes lazy loading, memory management, and efficient state handling
 */

object PerformanceOptimizer {

    // Memory cache with automatic cleanup
    private val imageCache = ConcurrentHashMap<String, CachedImage>()
    private val dataCache = ConcurrentHashMap<String, CachedData<*>>()

    // Configuration
    private const val MAX_CACHE_SIZE = 50
    private val DEFAULT_CACHE_DURATION = 5.minutes
    private val CLEANUP_INTERVAL = 1.minutes

    private var cleanupJob: Job? = null

    init {
        startPeriodicCleanup()
    }

    /**
     * Cached data wrapper with expiration
     */
    data class CachedData<T>(
        val data: T,
        val timestamp: Long,
        val duration: Duration = DEFAULT_CACHE_DURATION
    ) {
        val isExpired: Boolean
            get() = System.currentTimeMillis() - timestamp > duration.inWholeMilliseconds
    }

    data class CachedImage(
        val bitmap: ImageBitmap,
        val timestamp: Long,
        val size: Long = 0L
    )

    /**
     * Lazy data loader with caching
     */
    @Composable
    fun <T> rememberCachedData(
        key: String,
        cacheDuration: Duration = DEFAULT_CACHE_DURATION,
        loader: suspend () -> T
    ): State<AsyncData<T>> {
        return produceState<AsyncData<T>>(initialValue = AsyncData.Loading) {
            // Check cache first
            val cached = dataCache[key]
            if (cached != null && !cached.isExpired) {
                @Suppress("UNCHECKED_CAST")
                value = AsyncData.Success(cached.data as T)
                return@produceState
            }

            try {
                val result = loader()
                dataCache[key] = CachedData(result, System.currentTimeMillis(), cacheDuration)
                value = AsyncData.Success(result)
            } catch (e: Exception) {
                value = AsyncData.Error(e)
            }
        }
    }

    /**
     * Async data state for loading operations
     */
    sealed class AsyncData<out T> {
        object Loading : AsyncData<Nothing>()
        data class Success<T>(val data: T) : AsyncData<T>()
        data class Error(val exception: Throwable) : AsyncData<Nothing>()

        val isLoading: Boolean get() = this is Loading
        val isSuccess: Boolean get() = this is Success
        val isError: Boolean get() = this is Error

        fun getOrNull(): T? = if (this is Success) data else null
    }

    /**
     * Debounced state for search and input fields
     */
    @Composable
    fun rememberDebouncedState(
        initialValue: String,
        delayMillis: Long = 300L
    ): DebouncedState {
        var value by remember { mutableStateOf(initialValue) }
        var debouncedValue by remember { mutableStateOf(initialValue) }

        LaunchedEffect(value) {
            delay(delayMillis)
            debouncedValue = value
        }

        return remember {
            DebouncedState(
                value = value,
                debouncedValue = debouncedValue,
                onValueChange = { value = it }
            )
        }
    }

    data class DebouncedState(
        val value: String,
        val debouncedValue: String,
        val onValueChange: (String) -> Unit
    )

    /**
     * Lazy column optimization for large lists
     */
    @Composable
    fun <T> rememberLazyListState(
        items: List<T>,
        pageSize: Int = 20
    ): LazyListState<T> {
        return remember(items, pageSize) {
            LazyListState(
                items = items,
                pageSize = pageSize
            )
        }
    }

    data class LazyListState<T>(
        val items: List<T>,
        val pageSize: Int
    ) {
        private var loadedPages = mutableSetOf<Int>()

        fun getVisibleItems(firstVisibleIndex: Int, lastVisibleIndex: Int): List<T> {
            val startPage = firstVisibleIndex / pageSize
            val endPage = lastVisibleIndex / pageSize

            // Load required pages
            for (page in startPage..endPage) {
                loadedPages.add(page)
            }

            // Return items for loaded pages
            val startIndex = startPage * pageSize
            val endIndex = minOf((endPage + 1) * pageSize, items.size)

            return items.subList(startIndex, endIndex)
        }
    }

    /**
     * Memory-efficient bitmap caching
     */
    fun cacheImage(key: String, bitmap: ImageBitmap, sizeBytes: Long = 0L) {
        if (imageCache.size >= MAX_CACHE_SIZE) {
            // Remove oldest entries
            val sortedEntries = imageCache.entries.sortedBy { it.value.timestamp }
            sortedEntries.take(imageCache.size - MAX_CACHE_SIZE + 1).forEach {
                imageCache.remove(it.key)
            }
        }

        imageCache[key] = CachedImage(bitmap, System.currentTimeMillis(), sizeBytes)
    }

    fun getCachedImage(key: String): ImageBitmap? {
        val cached = imageCache[key]
        return if (cached != null && !isImageExpired(cached)) {
            cached.bitmap
        } else {
            imageCache.remove(key)
            null
        }
    }

    private fun isImageExpired(cached: CachedImage): Boolean {
        return System.currentTimeMillis() - cached.timestamp > DEFAULT_CACHE_DURATION.inWholeMilliseconds
    }

    /**
     * Background cleanup of expired cache entries
     */
    private fun startPeriodicCleanup() {
        cleanupJob?.cancel()
        cleanupJob = CoroutineScope(Dispatchers.Default).launch {
            while (isActive) {
                delay(CLEANUP_INTERVAL)
                cleanupExpiredEntries()
            }
        }
    }

    private fun cleanupExpiredEntries() {
        // Clean data cache
        val expiredDataKeys = dataCache.entries
            .filter { it.value.isExpired }
            .map { it.key }
        expiredDataKeys.forEach { dataCache.remove(it) }

        // Clean image cache
        val expiredImageKeys = imageCache.entries
            .filter { isImageExpired(it.value) }
            .map { it.key }
        expiredImageKeys.forEach { imageCache.remove(it) }
    }

    /**
     * Manual cache cleanup
     */
    fun clearCache() {
        dataCache.clear()
        imageCache.clear()
    }

    fun clearExpiredCache() {
        cleanupExpiredEntries()
    }

    /**
     * Memory usage monitoring
     */
    fun getCacheStats(): CacheStats {
        val dataSize = dataCache.size
        val imageSize = imageCache.size
        val imageSizeBytes = imageCache.values.sumOf { it.size }

        return CacheStats(
            dataEntries = dataSize,
            imageEntries = imageSize,
            imageSizeBytes = imageSizeBytes,
            totalEntries = dataSize + imageSize
        )
    }

    data class CacheStats(
        val dataEntries: Int,
        val imageEntries: Int,
        val imageSizeBytes: Long,
        val totalEntries: Int
    )

    /**
     * Cleanup on app termination
     */
    fun cleanup() {
        cleanupJob?.cancel()
        clearCache()
    }
}

/**
 * Efficient state management for complex UI
 */
@Composable
fun <T> rememberOptimizedState(
    key: Any?,
    calculation: () -> T
): T {
    return remember(key) { calculation() }
}

/**
 * Lazy initialization for expensive operations
 */
class LazyInitializer<T>(
    private val initializer: () -> T
) {
    private var _value: T? = null
    private var initialized = false

    val value: T
        get() {
            if (!initialized) {
                _value = initializer()
                initialized = true
            }
            @Suppress("UNCHECKED_CAST")
            return _value as T
        }

    fun reset() {
        _value = null
        initialized = false
    }
}

/**
 * Flow-based data loader with error handling
 */
fun <T> createDataLoader(
    loader: suspend () -> T
): Flow<PerformanceOptimizer.AsyncData<T>> = flow {
    emit(PerformanceOptimizer.AsyncData.Loading)
    try {
        val result = loader()
        emit(PerformanceOptimizer.AsyncData.Success(result))
    } catch (e: Exception) {
        emit(PerformanceOptimizer.AsyncData.Error(e))
    }
}.flowOn(Dispatchers.IO)

/**
 * Extension functions for performance optimization
 */
@Composable
fun <T> Flow<T>.collectAsStateWithLifecycle(
    initial: T,
    context: CoroutineContext = Dispatchers.Main.immediate
): State<T> {
    return collectAsState(initial = initial, context = context)
}
