package com.voltai.privacy

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.security.crypto.EncryptedFile
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.security.KeyStore
import java.security.SecureRandom
import java.time.LocalDateTime
import java.util.*
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SecurityManager @Inject constructor(
    private val context: Context,
    private val privacyPreferences: PrivacyPreferences
) {
    
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    private val _securityState = MutableStateFlow(SecurityState())
    val securityState: StateFlow<SecurityState> = _securityState.asStateFlow()
    
    private val _privacySettings = MutableStateFlow(PrivacySettings())
    val privacySettings: StateFlow<PrivacySettings> = _privacySettings.asStateFlow()
    
    private var masterKey: MasterKey? = null
    private val keyAlias = "VoltAI_Master_Key"
    
    companion object {
        private const val ENCRYPTED_PREFS_NAME = "voltai_secure_prefs"
        private const val KEY_SIZE = 256
        private const val GCM_IV_LENGTH = 12
        private const val GCM_TAG_LENGTH = 16
    }
    
    init {
        initializeSecurity()
    }
    
    private fun initializeSecurity() {
        scope.launch {
            try {
                setupMasterKey()
                loadPrivacySettings()
                updateSecurityState()
            } catch (e: Exception) {
                _securityState.value = _securityState.value.copy(
                    isError = true,
                    errorMessage = "Failed to initialize security: ${e.message}"
                )
            }
        }
    }
    
    private fun setupMasterKey() {
        try {
            masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .setRequestStrongBoxBacked(true)
                .setUserAuthenticationRequired(false) // Set to true for biometric authentication
                .build()
                
            _securityState.value = _securityState.value.copy(
                isEncryptionEnabled = true,
                encryptionType = EncryptionType.AES_256_GCM
            )
        } catch (e: Exception) {
            // Fallback to standard encryption
            _securityState.value = _securityState.value.copy(
                isEncryptionEnabled = false,
                errorMessage = "Failed to setup encryption: ${e.message}"
            )
        }
    }
    
    private suspend fun loadPrivacySettings() {
        val settings = privacyPreferences.getPrivacySettings()
        _privacySettings.value = settings
    }
    
    fun getEncryptedSharedPreferences(): android.content.SharedPreferences? {
        return try {
            masterKey?.let { key ->
                EncryptedSharedPreferences.create(
                    context,
                    ENCRYPTED_PREFS_NAME,
                    key,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                )
            }
        } catch (e: Exception) {
            null
        }
    }
    
    fun createEncryptedFile(fileName: String): EncryptedFile? {
        return try {
            masterKey?.let { key ->
                val file = File(context.filesDir, fileName)
                EncryptedFile.Builder(
                    context,
                    file,
                    key,
                    EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
                ).build()
            }
        } catch (e: Exception) {
            null
        }
    }
    
    fun encryptData(data: String): EncryptedData? {
        return try {
            val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES)
            val keyGenParameterSpec = KeyGenParameterSpec.Builder(
                keyAlias,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(KEY_SIZE)
                .build()
                
            keyGenerator.init(keyGenParameterSpec)
            val secretKey = keyGenerator.generateKey()
            
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.ENCRYPT_MODE, secretKey)
            
            val iv = cipher.iv
            val encryptedBytes = cipher.doFinal(data.toByteArray())
            
            EncryptedData(
                encryptedBytes = encryptedBytes,
                iv = iv,
                timestamp = System.currentTimeMillis()
            )
        } catch (e: Exception) {
            null
        }
    }
    
    fun decryptData(encryptedData: EncryptedData): String? {
        return try {
            val keyStore = KeyStore.getInstance("AndroidKeyStore")
            keyStore.load(null)
            
            val secretKey = keyStore.getKey(keyAlias, null) as SecretKey
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            val spec = GCMParameterSpec(GCM_TAG_LENGTH * 8, encryptedData.iv)
            
            cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)
            val decryptedBytes = cipher.doFinal(encryptedData.encryptedBytes)
            
            String(decryptedBytes)
        } catch (e: Exception) {
            null
        }
    }
    
    fun generateSecureToken(): String {
        val random = SecureRandom()
        val bytes = ByteArray(32)
        random.nextBytes(bytes)
        return Base64.getEncoder().encodeToString(bytes)
    }
    
    suspend fun updatePrivacySettings(settings: PrivacySettings) {
        _privacySettings.value = settings
        privacyPreferences.savePrivacySettings(settings)
        updateSecurityState()
    }
    
    suspend fun enableDataEncryption(enable: Boolean) {
        val currentSettings = _privacySettings.value
        val updatedSettings = currentSettings.copy(dataEncryptionEnabled = enable)
        updatePrivacySettings(updatedSettings)
    }
    
    suspend fun setDataRetentionPeriod(days: Int) {
        val currentSettings = _privacySettings.value
        val updatedSettings = currentSettings.copy(dataRetentionDays = days)
        updatePrivacySettings(updatedSettings)
    }
    
    suspend fun enableAnalytics(enable: Boolean) {
        val currentSettings = _privacySettings.value
        val updatedSettings = currentSettings.copy(analyticsEnabled = enable)
        updatePrivacySettings(updatedSettings)
    }
    
    suspend fun enableCrashReporting(enable: Boolean) {
        val currentSettings = _privacySettings.value
        val updatedSettings = currentSettings.copy(crashReportingEnabled = enable)
        updatePrivacySettings(updatedSettings)
    }
    
    suspend fun setDataSharingLevel(level: DataSharingLevel) {
        val currentSettings = _privacySettings.value
        val updatedSettings = currentSettings.copy(dataSharingLevel = level)
        updatePrivacySettings(updatedSettings)
    }
    
    suspend fun addDataExportRequest() {
        val currentSettings = _privacySettings.value
        val updatedSettings = currentSettings.copy(
            lastDataExportRequest = System.currentTimeMillis()
        )
        updatePrivacySettings(updatedSettings)
    }
    
    suspend fun addDataDeletionRequest() {
        val currentSettings = _privacySettings.value
        val updatedSettings = currentSettings.copy(
            lastDataDeletionRequest = System.currentTimeMillis()
        )
        updatePrivacySettings(updatedSettings)
    }
    
    suspend fun clearAllUserData() {
        try {
            // Clear encrypted preferences
            getEncryptedSharedPreferences()?.edit()?.clear()?.apply()
            
            // Clear regular preferences
            privacyPreferences.clearAllData()
            
            // Delete encrypted files
            context.filesDir.listFiles()?.forEach { file ->
                if (file.name.startsWith("voltai_encrypted_")) {
                    file.delete()
                }
            }
            
            _securityState.value = _securityState.value.copy(
                lastDataClearTime = System.currentTimeMillis()
            )
        } catch (e: Exception) {
            _securityState.value = _securityState.value.copy(
                isError = true,
                errorMessage = "Failed to clear user data: ${e.message}"
            )
        }
    }
    
    fun validateDataIntegrity(data: ByteArray): Boolean {
        return try {
            // Simple checksum validation
            val checksum = data.sum()
            checksum > 0 // Basic validation
        } catch (e: Exception) {
            false
        }
    }
    
    fun sanitizeData(data: String): String {
        // Remove potentially sensitive information
        return data
            .replace(Regex("[0-9]{10,}"), "[PHONE_NUMBER]") // Phone numbers
            .replace(Regex("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}"), "[EMAIL]") // Email addresses
            .replace(Regex("\\b(?:\\d{4}[-\\s]?){3}\\d{4}\\b"), "[CREDIT_CARD]") // Credit card numbers
    }
    
    fun getDataAccessLog(): List<DataAccessEntry> {
        // Return logged data access entries
        return getEncryptedSharedPreferences()?.let { prefs ->
            val logJson = prefs.getString("data_access_log", "[]") ?: "[]"
            // Parse JSON and return entries (simplified)
            emptyList<DataAccessEntry>()
        } ?: emptyList()
    }
    
    fun logDataAccess(type: DataAccessType, description: String) {
        scope.launch {
            try {
                val entry = DataAccessEntry(
                    timestamp = System.currentTimeMillis(),
                    type = type,
                    description = description,
                    userAgent = "VoltAI Android App"
                )
                
                // Store in encrypted preferences (simplified)
                getEncryptedSharedPreferences()?.edit()
                    ?.putString("last_data_access", "${entry.timestamp}:${entry.type.name}")
                    ?.apply()
            } catch (e: Exception) {
                // Handle logging errors
            }
        }
    }
    
    private fun updateSecurityState() {
        val currentSettings = _privacySettings.value
        
        _securityState.value = _securityState.value.copy(
            isPrivacyModeEnabled = currentSettings.privacyModeEnabled,
            dataEncryptionEnabled = currentSettings.dataEncryptionEnabled,
            isComplianceMode = isGDPRCompliant(),
            lastPrivacyReview = currentSettings.lastPrivacyReview,
            dataRetentionDays = currentSettings.dataRetentionDays
        )
    }
    
    private fun isGDPRCompliant(): Boolean {
        val settings = _privacySettings.value
        return settings.privacyModeEnabled &&
                settings.dataEncryptionEnabled &&
                settings.dataSharingLevel == DataSharingLevel.NONE &&
                settings.dataRetentionDays <= 365
    }
    
    fun getPrivacyReport(): PrivacyReport {
        val settings = _privacySettings.value
        val state = _securityState.value
        
        return PrivacyReport(
            isGDPRCompliant = isGDPRCompliant(),
            encryptionStatus = if (state.dataEncryptionEnabled) "Enabled" else "Disabled",
            dataRetentionDays = settings.dataRetentionDays,
            dataSharingLevel = settings.dataSharingLevel.name,
            lastPrivacyReview = settings.lastPrivacyReview,
            dataAccessCount = getDataAccessLog().size,
            securityRecommendations = generateSecurityRecommendations()
        )
    }
    
    private fun generateSecurityRecommendations(): List<String> {
        val recommendations = mutableListOf<String>()
        val settings = _privacySettings.value
        val state = _securityState.value
        
        if (!state.dataEncryptionEnabled) {
            recommendations.add("Enable data encryption for enhanced security")
        }
        
        if (!settings.privacyModeEnabled) {
            recommendations.add("Enable privacy mode to minimize data collection")
        }
        
        if (settings.dataSharingLevel != DataSharingLevel.NONE) {
            recommendations.add("Consider disabling data sharing for maximum privacy")
        }
        
        if (settings.dataRetentionDays > 365) {
            recommendations.add("Consider reducing data retention period for better privacy")
        }
        
        if (settings.lastPrivacyReview < System.currentTimeMillis() - (90 * 24 * 60 * 60 * 1000)) {
            recommendations.add("Review privacy settings - it's been over 90 days")
        }
        
        return recommendations
    }
}

// Data classes for security and privacy
data class SecurityState(
    val isEncryptionEnabled: Boolean = false,
    val encryptionType: EncryptionType = EncryptionType.NONE,
    val isPrivacyModeEnabled: Boolean = false,
    val dataEncryptionEnabled: Boolean = false,
    val isComplianceMode: Boolean = false,
    val lastPrivacyReview: Long = 0L,
    val dataRetentionDays: Int = 30,
    val lastDataClearTime: Long = 0L,
    val isError: Boolean = false,
    val errorMessage: String? = null
)

data class PrivacySettings(
    val privacyModeEnabled: Boolean = false,
    val dataEncryptionEnabled: Boolean = true,
    val analyticsEnabled: Boolean = false,
    val crashReportingEnabled: Boolean = false,
    val dataSharingLevel: DataSharingLevel = DataSharingLevel.MINIMAL,
    val dataRetentionDays: Int = 30,
    val autoDeleteEnabled: Boolean = true,
    val lastPrivacyReview: Long = 0L,
    val lastDataExportRequest: Long = 0L,
    val lastDataDeletionRequest: Long = 0L,
    val consentGiven: Boolean = false,
    val consentTimestamp: Long = 0L
)

data class EncryptedData(
    val encryptedBytes: ByteArray,
    val iv: ByteArray,
    val timestamp: Long
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        
        other as EncryptedData
        
        if (!encryptedBytes.contentEquals(other.encryptedBytes)) return false
        if (!iv.contentEquals(other.iv)) return false
        if (timestamp != other.timestamp) return false
        
        return true
    }
    
    override fun hashCode(): Int {
        var result = encryptedBytes.contentHashCode()
        result = 31 * result + iv.contentHashCode()
        result = 31 * result + timestamp.hashCode()
        return result
    }
}

data class DataAccessEntry(
    val timestamp: Long,
    val type: DataAccessType,
    val description: String,
    val userAgent: String
)

data class PrivacyReport(
    val isGDPRCompliant: Boolean,
    val encryptionStatus: String,
    val dataRetentionDays: Int,
    val dataSharingLevel: String,
    val lastPrivacyReview: Long,
    val dataAccessCount: Int,
    val securityRecommendations: List<String>
)

enum class EncryptionType {
    NONE,
    AES_128,
    AES_256_GCM,
    CHACHA20_POLY1305
}

enum class DataSharingLevel {
    NONE,           // No data sharing
    MINIMAL,        // Only essential anonymized data
    STANDARD,       // Standard analytics and crash reports
    FULL            // Full analytics and diagnostic data
}

enum class DataAccessType {
    READ_BATTERY_DATA,
    WRITE_BATTERY_DATA,
    EXPORT_DATA,
    DELETE_DATA,
    ANALYTICS_COLLECTION,
    CRASH_REPORT,
    SYNC_CLOUD,
    BACKUP_DATA
}
