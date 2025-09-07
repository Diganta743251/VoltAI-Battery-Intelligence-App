package com.voltai.core.error

import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ErrorHandler @Inject constructor(
    private val context: Context
) : Thread.UncaughtExceptionHandler {
    
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
    
    private val _errorEvents = MutableSharedFlow<ErrorEvent>()
    val errorEvents: SharedFlow<ErrorEvent> = _errorEvents.asSharedFlow()
    
    init {
        Thread.setDefaultUncaughtExceptionHandler(this)
    }
    
    override fun uncaughtException(t: Thread, e: Throwable) {
        handleCrash(e, "UNCAUGHT_EXCEPTION", t.name)
        defaultHandler?.uncaughtException(t, e)
    }
    
    fun handleError(
        throwable: Throwable,
        context: String = "",
        severity: ErrorSeverity = ErrorSeverity.ERROR,
        userMessage: String? = null
    ) {
        scope.launch {
            val errorEvent = ErrorEvent(
                id = generateErrorId(),
                throwable = throwable,
                context = context,
                severity = severity,
                userMessage = userMessage ?: getDefaultUserMessage(throwable),
                timestamp = LocalDateTime.now(),
                stackTrace = getStackTrace(throwable),
                deviceInfo = getDeviceInfo()
            )
            
            logError(errorEvent)
            saveErrorToFile(errorEvent)
            _errorEvents.emit(errorEvent)
            
            if (severity == ErrorSeverity.CRITICAL) {
                reportCriticalError(errorEvent)
            }
        }
    }
    
    private fun handleCrash(throwable: Throwable, type: String, threadName: String) {
        val crashEvent = ErrorEvent(
            id = generateErrorId(),
            throwable = throwable,
            context = "CRASH_$type",
            severity = ErrorSeverity.CRITICAL,
            userMessage = "The app has encountered a critical error and needs to restart",
            timestamp = LocalDateTime.now(),
            stackTrace = getStackTrace(throwable),
            deviceInfo = getDeviceInfo(),
            additionalData = mapOf("thread" to threadName, "type" to type)
        )
        
        logError(crashEvent)
        saveErrorToFile(crashEvent)
    }
    
    private fun logError(errorEvent: ErrorEvent) {
        val tag = "VoltAI_${errorEvent.severity.name}"
        val message = "${errorEvent.context}: ${errorEvent.throwable.message}"
        
        when (errorEvent.severity) {
            ErrorSeverity.DEBUG -> Log.d(tag, message, errorEvent.throwable)
            ErrorSeverity.INFO -> Log.i(tag, message, errorEvent.throwable)
            ErrorSeverity.WARNING -> Log.w(tag, message, errorEvent.throwable)
            ErrorSeverity.ERROR -> Log.e(tag, message, errorEvent.throwable)
            ErrorSeverity.CRITICAL -> Log.wtf(tag, message, errorEvent.throwable)
        }
    }
    
    private fun saveErrorToFile(errorEvent: ErrorEvent) {
        try {
            val logsDir = File(context.filesDir, "error_logs")
            if (!logsDir.exists()) {
                logsDir.mkdirs()
            }
            
            val timestamp = errorEvent.timestamp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"))
            val logFile = File(logsDir, "error_${timestamp}_${errorEvent.id}.txt")
            
            logFile.writeText(formatErrorForFile(errorEvent))
        } catch (e: Exception) {
            Log.e("ErrorHandler", "Failed to save error to file", e)
        }
    }
    
    private fun formatErrorForFile(errorEvent: ErrorEvent): String {
        return buildString {
            appendLine("=== VoltAI Error Report ===")
            appendLine("ID: ${errorEvent.id}")
            appendLine("Timestamp: ${errorEvent.timestamp}")
            appendLine("Severity: ${errorEvent.severity}")
            appendLine("Context: ${errorEvent.context}")
            appendLine("Exception: ${errorEvent.throwable.javaClass.simpleName}")
            appendLine("Message: ${errorEvent.throwable.message}")
            appendLine("User Message: ${errorEvent.userMessage}")
            appendLine()
            appendLine("Stack Trace:")
            appendLine(errorEvent.stackTrace)
            appendLine()
            appendLine("Device Info:")
            errorEvent.deviceInfo.forEach { (key, value) ->
                appendLine("$key: $value")
            }
            appendLine()
            appendLine("Additional Data:")
            errorEvent.additionalData.forEach { (key, value) ->
                appendLine("$key: $value")
            }
        }
    }
    
    private fun reportCriticalError(errorEvent: ErrorEvent) {
        // In a real app, this would send to crash reporting service
        Log.e("CriticalError", "Critical error reported: ${errorEvent.id}")
    }
    
    private fun getStackTrace(throwable: Throwable): String {
        val stringWriter = StringWriter()
        val printWriter = PrintWriter(stringWriter)
        throwable.printStackTrace(printWriter)
        return stringWriter.toString()
    }
    
    private fun getDeviceInfo(): Map<String, String> {
        return mapOf(
            "model" to android.os.Build.MODEL,
            "manufacturer" to android.os.Build.MANUFACTURER,
            "version" to android.os.Build.VERSION.RELEASE,
            "sdk" to android.os.Build.VERSION.SDK_INT.toString(),
            "app_version" to "1.0.0" // Should come from BuildConfig
        )
    }
    
    private fun getDefaultUserMessage(throwable: Throwable): String {
        return when (throwable) {
            is OutOfMemoryError -> "The app is running low on memory. Please close other apps and try again."
            is SecurityException -> "A security error occurred. Please check your permissions."
            is java.net.UnknownHostException -> "Unable to connect to the internet. Please check your connection."
            is java.io.IOException -> "A data error occurred. Please try again."
            else -> "An unexpected error occurred. Please try again or contact support if the problem persists."
        }
    }
    
    private fun generateErrorId(): String {
        return "ERR_${System.currentTimeMillis()}_${(1000..9999).random()}"
    }
    
    fun getErrorLogs(): List<File> {
        val logsDir = File(context.filesDir, "error_logs")
        return if (logsDir.exists()) {
            logsDir.listFiles()?.toList() ?: emptyList()
        } else {
            emptyList()
        }
    }
    
    fun clearOldLogs(daysOld: Int = 7) {
        val cutoffTime = System.currentTimeMillis() - (daysOld * 24 * 60 * 60 * 1000)
        getErrorLogs().forEach { file ->
            if (file.lastModified() < cutoffTime) {
                file.delete()
            }
        }
    }
}

data class ErrorEvent(
    val id: String,
    val throwable: Throwable,
    val context: String,
    val severity: ErrorSeverity,
    val userMessage: String,
    val timestamp: LocalDateTime,
    val stackTrace: String,
    val deviceInfo: Map<String, String>,
    val additionalData: Map<String, String> = emptyMap()
)

enum class ErrorSeverity {
    DEBUG,
    INFO,
    WARNING,
    ERROR,
    CRITICAL
}

// Extension functions for easy error handling
inline fun <T> Result.Companion.tryWithErrorHandler(
    errorHandler: ErrorHandler,
    context: String = "",
    block: () -> T
): Result<T> {
    return try {
        Result.success(block())
    } catch (e: Exception) {
        errorHandler.handleError(e, context)
        Result.failure(e)
    }
}

suspend inline fun <T> safeCall(
    errorHandler: ErrorHandler,
    context: String = "",
    defaultValue: T,
    block: suspend () -> T
): T {
    return try {
        block()
    } catch (e: Exception) {
        errorHandler.handleError(e, context)
        defaultValue
    }
}
