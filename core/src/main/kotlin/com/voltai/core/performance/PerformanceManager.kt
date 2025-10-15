package com.voltai.core.performance

import android.app.ActivityManager
import android.content.Context
import android.os.Debug
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.ThreadPoolExecutor
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.max

@Singleton
class PerformanceManager @Inject constructor(
    private val context: Context
) : DefaultLifecycleObserver {
    
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val mainHandler = Handler(Looper.getMainLooper())
    
    private val _performanceState = MutableStateFlow(PerformanceState())
    val performanceState: StateFlow<PerformanceState> = _performanceState.asStateFlow()
    
    private val _memoryMetrics = MutableStateFlow(MemoryMetrics())
    val memoryMetrics: StateFlow<MemoryMetrics> = _memoryMetrics.asStateFlow()
    
    // Thread pools for different types of work
    private val ioExecutor = Executors.newCachedThreadPool() as ThreadPoolExecutor
    private val computationExecutor = Executors.newFixedThreadPool(
        max(1, Runtime.getRuntime().availableProcessors() - 1)
    ) as ThreadPoolExecutor
    
    // Memory and performance caches
    private val dataCache = ConcurrentHashMap<String, CacheEntry<Any>>()
    private val imageCache = ConcurrentHashMap<String, CacheEntry<Any>>()
    
    // Performance monitoring
    private var isMonitoring = false
    private var performanceMonitoringJob: Job? = null
    
    companion object {
        private const val MONITORING_INTERVAL = 5000L // 5 seconds
        private const val MEMORY_WARNING_THRESHOLD = 0.8f // 80% memory usage
        private const val MEMORY_CRITICAL_THRESHOLD = 0.9f // 90% memory usage
        private const val CACHE_MAX_SIZE = 100
        private const val CACHE_TTL_MS = 300000L // 5 minutes
        private const val GC_TRIGGER_THRESHOLD = 0.85f
    }
    
    init {
        startPerformanceMonitoring()
    }
    
    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        resumePerformanceMonitoring()
    }
    
    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        pausePerformanceMonitoring()
    }
    
    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        shutdown()
    }
    
    fun startPerformanceMonitoring() {
        if (isMonitoring) return
        
        isMonitoring = true
        performanceMonitoringJob = scope.launch {
            monitorPerformance()
        }
    }
    
    fun stopPerformanceMonitoring() {
        isMonitoring = false
        performanceMonitoringJob?.cancel()
    }
    
    private fun resumePerformanceMonitoring() {
        if (!isMonitoring) {
            startPerformanceMonitoring()
        }
    }
    
    private fun pausePerformanceMonitoring() {
        // Don't stop completely, just reduce frequency
        // This allows background monitoring while app is not visible
    }
    
    private suspend fun monitorPerformance() {
        while (isMonitoring) {
            try {
                val memoryInfo = getMemoryInfo()
                val cpuInfo = getCpuInfo()
                val threadInfo = getThreadInfo()
                val cacheInfo = getCacheInfo()
                
                val currentMemoryMetrics = MemoryMetrics(
                    totalMemoryMB = memoryInfo.totalMemory / (1024 * 1024),
                    usedMemoryMB = memoryInfo.usedMemory / (1024 * 1024),
                    availableMemoryMB = memoryInfo.availableMemory / (1024 * 1024),
                    memoryUsagePercentage = memoryInfo.usagePercentage,
                    heapSizeMB = memoryInfo.heapSize / (1024 * 1024),
                    heapUsedMB = memoryInfo.heapUsed / (1024 * 1024),
                    nativeHeapSizeMB = memoryInfo.nativeHeapSize / (1024 * 1024),
                    gcCount = memoryInfo.gcCount,
                    lastGcTime = memoryInfo.lastGcTime
                )
                
                _memoryMetrics.value = currentMemoryMetrics
                
                val currentPerformanceState = PerformanceState(
                    memoryUsagePercentage = memoryInfo.usagePercentage,
                    cpuUsagePercentage = cpuInfo.usage,
                    activeThreadCount = threadInfo.activeThreads,
                    ioThreadPoolSize = ioExecutor.poolSize,
                    computationThreadPoolSize = computationExecutor.poolSize,
                    cacheHitRate = cacheInfo.hitRate,
                    cacheSize = cacheInfo.size,
                    performanceLevel = determinePerformanceLevel(memoryInfo.usagePercentage, cpuInfo.usage),
                    shouldOptimize = shouldTriggerOptimization(memoryInfo.usagePercentage),
                    lastUpdate = System.currentTimeMillis()
                )
                
                _performanceState.value = currentPerformanceState
                
                // Auto-optimize if needed
                if (currentPerformanceState.shouldOptimize) {
                    performAutoOptimization()
                }
                
            } catch (e: Exception) {
                _performanceState.value = _performanceState.value.copy(
                    isError = true,
                    errorMessage = "Performance monitoring error: ${e.message}"
                )
            }
            
            delay(MONITORING_INTERVAL)
        }
    }
    
    private fun getMemoryInfo(): SystemMemoryInfo {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)
        
        val runtime = Runtime.getRuntime()
        val totalMemory = memoryInfo.totalMem
        val availableMemory = memoryInfo.availMem
        val usedMemory = totalMemory - availableMemory
        val usagePercentage = (usedMemory.toFloat() / totalMemory.toFloat())
        
        val heapSize = runtime.totalMemory()
        val heapUsed = heapSize - runtime.freeMemory()
        
        return SystemMemoryInfo(
            totalMemory = totalMemory,
            usedMemory = usedMemory,
            availableMemory = availableMemory,
            usagePercentage = usagePercentage,
            heapSize = heapSize,
            heapUsed = heapUsed,
            nativeHeapSize = Debug.getNativeHeapSize(),
            gcCount = getGcCount(),
            lastGcTime = System.currentTimeMillis()
        )
    }
    
    private fun getCpuInfo(): SystemCpuInfo {
        return try {
            val cpuUsage = getCurrentCpuUsage()
            SystemCpuInfo(
                usage = cpuUsage,
                coreCount = Runtime.getRuntime().availableProcessors(),
                frequency = 0 // Would require root access to read actual frequency
            )
        } catch (e: Exception) {
            SystemCpuInfo(0f, Runtime.getRuntime().availableProcessors(), 0)
        }
    }
    
    private fun getCurrentCpuUsage(): Float {
        return try {
            val statFile = File("/proc/stat")
            if (statFile.exists()) {
                // Simplified CPU usage calculation
                // In a real implementation, you'd need to read /proc/stat multiple times
                val lines = statFile.readLines()
                val cpuLine = lines.firstOrNull { it.startsWith("cpu ") }
                if (cpuLine != null) {
                    val values = cpuLine.split(" ").drop(1).mapNotNull { it.toLongOrNull() }
                    if (values.size >= 4) {
                        val idle = values[3]
                        val total = values.sum()
                        return ((total - idle).toFloat() / total.toFloat()) * 100f
                    }
                }
            }
            0f
        } catch (e: Exception) {
            0f
        }
    }
    
    private fun getThreadInfo(): SystemThreadInfo {
        val threadGroup = Thread.currentThread().threadGroup
        var rootGroup = threadGroup
        while (rootGroup?.parent != null) {
            rootGroup = rootGroup.parent
        }
        
        val activeThreads = rootGroup?.activeCount() ?: 0
        
        return SystemThreadInfo(
            activeThreads = activeThreads,
            maxThreads = activeThreads * 2, // Estimated
            ioPoolActive = ioExecutor.activeCount,
            computationPoolActive = computationExecutor.activeCount
        )
    }
    
    private fun getCacheInfo(): SystemCacheInfo {
        val totalSize = dataCache.size + imageCache.size
        val hitRate = calculateCacheHitRate()
        
        return SystemCacheInfo(
            size = totalSize,
            maxSize = CACHE_MAX_SIZE * 2, // Data + Image cache
            hitRate = hitRate
        )
    }
    
    private fun getGcCount(): Int {
        return try {
            // This is a simplified approach
            System.gc()
            0 // Would need more sophisticated GC monitoring
        } catch (e: Exception) {
            0
        }
    }
    
    private fun calculateCacheHitRate(): Float {
        // Simplified cache hit rate calculation
        // In a real implementation, you'd track hits and misses
        return 0.75f // Placeholder
    }
    
    private fun determinePerformanceLevel(memoryUsage: Float, cpuUsage: Float): PerformanceLevel {
        return when {
            memoryUsage > MEMORY_CRITICAL_THRESHOLD || cpuUsage > 90f -> PerformanceLevel.CRITICAL
            memoryUsage > MEMORY_WARNING_THRESHOLD || cpuUsage > 70f -> PerformanceLevel.WARNING
            memoryUsage > 0.5f || cpuUsage > 50f -> PerformanceLevel.MODERATE
            else -> PerformanceLevel.OPTIMAL
        }
    }
    
    private fun shouldTriggerOptimization(memoryUsage: Float): Boolean {
        return memoryUsage > GC_TRIGGER_THRESHOLD
    }
    
    private fun performAutoOptimization() {
        scope.launch {
            try {
                // Clear expired cache entries
                clearExpiredCache()
                
                // Suggest garbage collection
                suggestGarbageCollection()
                
                // Optimize thread pools
                optimizeThreadPools()
                
                // Trim memory if possible
                trimMemory()
                
            } catch (e: Exception) {
                // Log optimization errors
            }
        }
    }
    
    fun clearExpiredCache() {
        val currentTime = System.currentTimeMillis()
        
        dataCache.entries.removeAll { (_, entry) ->
            currentTime - entry.timestamp > CACHE_TTL_MS
        }
        
        imageCache.entries.removeAll { (_, entry) ->
            currentTime - entry.timestamp > CACHE_TTL_MS
        }
    }
    
    fun clearAllCaches() {
        dataCache.clear()
        imageCache.clear()
    }
    
    fun suggestGarbageCollection() {
        val memoryUsage = _memoryMetrics.value.memoryUsagePercentage
        if (memoryUsage > GC_TRIGGER_THRESHOLD) {
            System.gc()
        }
    }
    
    private fun optimizeThreadPools() {
        // Adjust thread pool sizes based on current load
        val currentLoad = _performanceState.value.cpuUsagePercentage
        
        if (currentLoad > 80f) {
            // Reduce thread pool sizes under high load
            ioExecutor.corePoolSize = max(1, ioExecutor.corePoolSize - 1)
        } else if (currentLoad < 30f) {
            // Increase thread pool sizes under low load
            ioExecutor.corePoolSize = ioExecutor.corePoolSize + 1
        }
    }
    
    fun trimMemory() {
        clearExpiredCache()
        suggestGarbageCollection()
    }
    
    // Cache management functions
    fun <T> cacheData(key: String, data: T) {
        if (dataCache.size >= CACHE_MAX_SIZE) {
            // Remove oldest entry
            val oldestKey = dataCache.entries.minByOrNull { it.value.timestamp }?.key
            oldestKey?.let { dataCache.remove(it) }
        }
        
        dataCache[key] = CacheEntry<Any>(data as Any, System.currentTimeMillis())
    }
    
    @Suppress("UNCHECKED_CAST")
    fun <T> getCachedData(key: String): T? {
        val entry = dataCache[key] ?: return null
        
        if (System.currentTimeMillis() - entry.timestamp > CACHE_TTL_MS) {
            dataCache.remove(key)
            return null
        }
        
        return entry.data as? T
    }
    
    // Async processing utilities
    fun <T> executeOnIO(block: suspend () -> T): Deferred<T> {
        return scope.async(Dispatchers.IO) {
            block()
        }
    }
    
    fun <T> executeOnComputation(block: suspend () -> T): Deferred<T> {
        return scope.async(Dispatchers.Default) {
            block()
        }
    }
    
    fun executeOnMain(block: () -> Unit) {
        mainHandler.post(block)
    }
    
    fun <T> executeOnMainWithResult(block: suspend () -> T): Deferred<T> {
        return scope.async(Dispatchers.Main) {
            block()
        }
    }
    
    // Batch processing for efficient data handling
    suspend fun <T, R> processBatch(items: List<T>, batchSize: Int = 50, processor: suspend (List<T>) -> List<R>): List<R> {
        return items.chunked(batchSize).map { batch ->
            processor(batch)
        }.flatten()
    }
    
    // Performance profiling
    fun <T> measurePerformance(operation: String, block: () -> T): T {
        val startTime = System.nanoTime()
        val result = block()
        val endTime = System.nanoTime()
        val durationMs = (endTime - startTime) / 1_000_000
        
        // Log performance metric (simplified)
        scope.launch {
            logPerformanceMetric(operation, durationMs)
        }
        
        return result
    }
    
    suspend fun <T> measurePerformanceSuspend(operation: String, block: suspend () -> T): T {
        val startTime = System.nanoTime()
        val result = block()
        val endTime = System.nanoTime()
        val durationMs = (endTime - startTime) / 1_000_000
        
        logPerformanceMetric(operation, durationMs)
        
        return result
    }
    
    private fun logPerformanceMetric(operation: String, durationMs: Long) {
        // In a real implementation, you'd send this to analytics or logging system
        if (durationMs > 1000) { // Log operations taking more than 1 second
            // Log slow operation
        }
    }
    
    // Memory management utilities
    fun getOptimalBatchSize(itemSize: Int, availableMemoryMB: Long): Int {
        val availableBytes = availableMemoryMB * 1024 * 1024
        val maxItems = (availableBytes / (itemSize * 2)).toInt() // Use half available memory
        return max(1, maxItems)
    }
    
    fun shouldReduceQuality(): Boolean {
        return _performanceState.value.memoryUsagePercentage > MEMORY_WARNING_THRESHOLD
    }
    
    fun getRecommendedImageQuality(): Float {
        return when (_performanceState.value.performanceLevel) {
            PerformanceLevel.CRITICAL -> 0.3f
            PerformanceLevel.WARNING -> 0.5f
            PerformanceLevel.MODERATE -> 0.7f
            PerformanceLevel.OPTIMAL -> 1.0f
        }
    }
    
    // Shutdown and cleanup
    private fun shutdown() {
        stopPerformanceMonitoring()
        clearAllCaches()
        ioExecutor.shutdown()
        computationExecutor.shutdown()
        scope.cancel()
    }
}

// Data classes for performance management
data class PerformanceState(
    val memoryUsagePercentage: Float = 0f,
    val cpuUsagePercentage: Float = 0f,
    val activeThreadCount: Int = 0,
    val ioThreadPoolSize: Int = 0,
    val computationThreadPoolSize: Int = 0,
    val cacheHitRate: Float = 0f,
    val cacheSize: Int = 0,
    val performanceLevel: PerformanceLevel = PerformanceLevel.OPTIMAL,
    val shouldOptimize: Boolean = false,
    val lastUpdate: Long = 0L,
    val isError: Boolean = false,
    val errorMessage: String? = null
)

data class MemoryMetrics(
    val totalMemoryMB: Long = 0L,
    val usedMemoryMB: Long = 0L,
    val availableMemoryMB: Long = 0L,
    val memoryUsagePercentage: Float = 0f,
    val heapSizeMB: Long = 0L,
    val heapUsedMB: Long = 0L,
    val nativeHeapSizeMB: Long = 0L,
    val gcCount: Int = 0,
    val lastGcTime: Long = 0L
)

private data class SystemMemoryInfo(
    val totalMemory: Long,
    val usedMemory: Long,
    val availableMemory: Long,
    val usagePercentage: Float,
    val heapSize: Long,
    val heapUsed: Long,
    val nativeHeapSize: Long,
    val gcCount: Int,
    val lastGcTime: Long
)

private data class SystemCpuInfo(
    val usage: Float,
    val coreCount: Int,
    val frequency: Int
)

private data class SystemThreadInfo(
    val activeThreads: Int,
    val maxThreads: Int,
    val ioPoolActive: Int,
    val computationPoolActive: Int
)

private data class SystemCacheInfo(
    val size: Int,
    val maxSize: Int,
    val hitRate: Float
)

private data class CacheEntry<T>(
    val data: T,
    val timestamp: Long
)

enum class PerformanceLevel {
    OPTIMAL,
    MODERATE,
    WARNING,
    CRITICAL
}
