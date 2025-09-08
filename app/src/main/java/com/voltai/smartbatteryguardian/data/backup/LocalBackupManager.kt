package com.voltai.smartbatteryguardian.data.backup

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Local backup manager for data export/import functionality
 * This is a simplified version without Google Drive dependencies
 */
@Singleton
class LocalBackupManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    enum class BackupState {
        IDLE,
        CREATING,
        COMPLETED,
        ERROR
    }

    private val _backupState = MutableStateFlow(BackupState.IDLE)
    val backupState: StateFlow<BackupState> = _backupState.asStateFlow()

    private val _backupProgress = MutableStateFlow(0f)
    val backupProgress: StateFlow<Float> = _backupProgress.asStateFlow()

    data class BackupResult(
        val success: Boolean,
        val filePath: String?,
        val fileSize: Long,
        val errorMessage: String?
    )

    data class LocalBackup(
        val id: String,
        val name: String,
        val filePath: String,
        val createdTime: Date,
        val fileSize: Long
    )

    /**
     * Create a local backup file
     */
    suspend fun createLocalBackup(data: Map<String, Any>): BackupResult = withContext(Dispatchers.IO) {
        try {
            _backupState.value = BackupState.CREATING
            _backupProgress.value = 0f

            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val filename = "voltai_backup_$timestamp.json"
            val backupFile = File(context.getExternalFilesDir("backups"), filename)

            // Ensure backup directory exists
            backupFile.parentFile?.mkdirs()

            _backupProgress.value = 0.3f

            // Add metadata
            val backupData = mutableMapOf<String, Any>()
            backupData["metadata"] = mapOf(
                "backupDate" to Date(),
                "appVersion" to "1.0",
                "deviceModel" to android.os.Build.MODEL,
                "androidVersion" to android.os.Build.VERSION.RELEASE
            )
            backupData.putAll(data)

            _backupProgress.value = 0.6f

            // Write backup to file
            val jsonString = json.encodeToString(backupData)
            backupFile.writeText(jsonString)

            _backupProgress.value = 1f
            _backupState.value = BackupState.COMPLETED

            BackupResult(
                success = true,
                filePath = backupFile.absolutePath,
                fileSize = backupFile.length(),
                errorMessage = null
            )

        } catch (e: Exception) {
            _backupState.value = BackupState.ERROR
            BackupResult(
                success = false,
                filePath = null,
                fileSize = 0L,
                errorMessage = e.message
            )
        }
    }

    /**
     * List all local backups
     */
    suspend fun listLocalBackups(): List<LocalBackup> = withContext(Dispatchers.IO) {
        try {
            val backupDir = File(context.getExternalFilesDir("backups"), "")
            if (!backupDir.exists()) return@withContext emptyList()

            backupDir.listFiles { file ->
                file.name.startsWith("voltai_backup_") && file.name.endsWith(".json")
            }?.mapNotNull { file ->
                try {
                    LocalBackup(
                        id = file.nameWithoutExtension,
                        name = "VoltAI Backup ${SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(Date(file.lastModified()))}",
                        filePath = file.absolutePath,
                        createdTime = Date(file.lastModified()),
                        fileSize = file.length()
                    )
                } catch (e: Exception) {
                    null
                }
            }?.sortedByDescending { it.createdTime } ?: emptyList()

        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Delete a local backup
     */
    suspend fun deleteLocalBackup(backupId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val backupFile = File(context.getExternalFilesDir("backups"), "$backupId.json")
            if (backupFile.exists()) {
                backupFile.delete()
            } else false
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Read backup data from file
     */
    suspend fun readBackupData(filePath: String): Map<String, Any>? = withContext(Dispatchers.IO) {
        try {
            val file = File(filePath)
            if (!file.exists()) return@withContext null

            val jsonString = file.readText()
            json.decodeFromString<Map<String, Any>>(jsonString)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Get backup directory path
     */
    fun getBackupDirectory(): String {
        val backupDir = File(context.getExternalFilesDir("backups"), "")
        backupDir.mkdirs()
        return backupDir.absolutePath
    }

    /**
     * Get total size of all backups
     */
    suspend fun getTotalBackupSize(): Long = withContext(Dispatchers.IO) {
        try {
            val backupDir = File(context.getExternalFilesDir("backups"), "")
            if (!backupDir.exists()) return@withContext 0L

            backupDir.listFiles()?.sumOf { it.length() } ?: 0L
        } catch (e: Exception) {
            0L
        }
    }

    /**
     * Clean up old backups (keep only the most recent 10)
     */
    suspend fun cleanupOldBackups(maxBackups: Int = 10): Int = withContext(Dispatchers.IO) {
        try {
            val backups = listLocalBackups()
            val toDelete = backups.drop(maxBackups)

            var deletedCount = 0
            toDelete.forEach { backup ->
                if (deleteLocalBackup(backup.id)) {
                    deletedCount++
                }
            }

            deletedCount
        } catch (e: Exception) {
            0
        }
    }
}
