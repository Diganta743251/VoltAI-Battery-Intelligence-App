package com.voltai.backup

import android.content.Context
import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton
import android.util.Log
import com.google.api.client.http.FileContent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec
import javax.crypto.spec.IvParameterSpec
import java.security.SecureRandom
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

@Singleton
class GoogleDriveBackupManager @Inject constructor(private val context: Context) {

    companion object {
        private const val TAG = "GoogleDriveBackup"
        private const val BACKUP_FOLDER_NAME = "VoltAI_Backups"
        private const val BACKUP_FILE_PREFIX = "voltai_backup_"
        private const val MIME_TYPE_JSON = "application/json"
        private const val MIME_TYPE_FOLDER = "application/vnd.google-apps.folder"
        private const val ENCRYPTION_KEY_ALIAS = "voltai_backup_key"
        private const val ENCRYPTION_TRANSFORMATION = "AES/CBC/PKCS7Padding"
        private const val KEY_LENGTH = 256
    }

    private val googleSignInClient: GoogleSignInClient
    private var driveService: Drive? = null
    private var backupFolderId: String? = null
    private val encryptedPrefs: SharedPreferences
    
    private val _backupState = MutableStateFlow(BackupState.IDLE)
    val backupState: StateFlow<BackupState> = _backupState.asStateFlow()

    init {
        val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(Scope(DriveScopes.DRIVE_FILE), Scope(DriveScopes.DRIVE_APPDATA))
            .build()
        googleSignInClient = GoogleSignIn.getClient(context, signInOptions)
        
        // Initialize encrypted shared preferences
        encryptedPrefs = EncryptedSharedPreferences.create(
            "secure_backup_prefs",
            MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build(),
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
        
        // Check if already signed in
        val account = GoogleSignIn.getLastSignedInAccount(context)
        if (account != null) {
            initializeDriveService(account.account?.name ?: "")
        }
    }
    
    private fun initializeDriveService(accountName: String) {
        try {
            val credential = GoogleAccountCredential.usingOAuth2(
                context,
                setOf(DriveScopes.DRIVE_FILE, DriveScopes.DRIVE_APPDATA)
            )
            credential.selectedAccountName = accountName
            
            driveService = Drive.Builder(
                NetHttpTransport(),
                GsonFactory.getDefaultInstance(),
                credential
            )
                .setApplicationName("VoltAI Smart Battery Guardian")
                .build()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize Drive service", e)
        }
    }

    fun getSignInIntent(): Intent {
        return googleSignInClient.signInIntent
    }
    
    fun isSignedIn(): Boolean {
        return GoogleSignIn.getLastSignedInAccount(context) != null
    }
    
    suspend fun signOut() {
        withContext(Dispatchers.IO) {
            try {
                googleSignInClient.signOut()
                driveService = null
                backupFolderId = null
                _backupState.value = BackupState.IDLE
            } catch (e: Exception) {
                Log.e(TAG, "Sign out failed", e)
            }
        }
    }

    fun handleSignInResult(resultCode: Int, data: Intent?, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        GoogleSignIn.getSignedInAccountFromIntent(data)
            .addOnSuccessListener { account ->
                initializeDriveService(account.account?.name ?: "")
                onSuccess()
            }
            .addOnFailureListener(onFailure)
    }
    
    private suspend fun getOrCreateBackupFolder(): String? = withContext(Dispatchers.IO) {
        if (backupFolderId != null) return@withContext backupFolderId
        
        try {
            // Search for existing folder
            val query = "mimeType='$MIME_TYPE_FOLDER' and name='$BACKUP_FOLDER_NAME' and trashed=false"
            val result = driveService?.files()?.list()
                ?.setQ(query)
                ?.setSpaces("drive")
                ?.execute()
            
            if (result?.files?.isNotEmpty() == true) {
                backupFolderId = result.files[0].id
                return@withContext backupFolderId
            }
            
            // Create new folder
            val folderMetadata = com.google.api.services.drive.model.File()
                .setName(BACKUP_FOLDER_NAME)
                .setMimeType(MIME_TYPE_FOLDER)
            
            val folder = driveService?.files()?.create(folderMetadata)
                ?.setFields("id")
                ?.execute()
            
            backupFolderId = folder?.id
            return@withContext backupFolderId
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get or create backup folder", e)
            return@withContext null
        }
    }

    suspend fun uploadBackup(backupData: String, encrypted: Boolean = true): BackupResult = withContext(Dispatchers.IO) {
        try {
            _backupState.value = BackupState.UPLOADING
            
            val folderId = getOrCreateBackupFolder()
            if (folderId == null) {
                _backupState.value = BackupState.ERROR("Failed to create backup folder")
                return@withContext BackupResult.Failure("Failed to create backup folder")
            }
            
            // Encrypt data if requested
            val dataToUpload = if (encrypted) {
                encryptData(backupData)
            } else {
                backupData
            }
            
            // Create backup file name with timestamp
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName = "${BACKUP_FILE_PREFIX}$timestamp.json"
            
            // Create temp file
            val tempFile = File(context.cacheDir, fileName)
            tempFile.writeText(dataToUpload)
            
            // Upload to Drive
            val fileMetadata = com.google.api.services.drive.model.File()
                .setName(fileName)
                .setParents(listOf(folderId))
                .setDescription("VoltAI Battery Guardian Backup - ${Date()}")
            
            val mediaContent = FileContent(MIME_TYPE_JSON, tempFile)
            
            val uploadedFile = driveService?.files()?.create(fileMetadata, mediaContent)
                ?.setFields("id, name, modifiedTime, size")
                ?.execute()
            
            // Clean up temp file
            tempFile.delete()
            
            if (uploadedFile != null) {
                _backupState.value = BackupState.SUCCESS
                BackupResult.Success(uploadedFile.id, uploadedFile.name)
            } else {
                _backupState.value = BackupState.ERROR("Upload failed")
                BackupResult.Failure("Upload failed")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Backup upload failed", e)
            _backupState.value = BackupState.ERROR(e.message ?: "Unknown error")
            BackupResult.Failure(e.message ?: "Unknown error")
        }
    }

    suspend fun downloadLatestBackup(encrypted: Boolean = true): String? = withContext(Dispatchers.IO) {
        try {
            _backupState.value = BackupState.DOWNLOADING
            
            // List all backup files
            val backups = listBackups()
            if (backups.isEmpty()) {
                _backupState.value = BackupState.ERROR("No backups found")
                return@withContext null
            }
            
            // Get the latest backup
            val latestBackup = backups.first()
            
            // Download file content
            val outputStream = ByteArrayOutputStream()
            driveService?.files()?.get(latestBackup.id)
                ?.executeMediaAndDownloadTo(outputStream)
            
            val downloadedData = outputStream.toString("UTF-8")
            outputStream.close()
            
            // Decrypt if needed
            val result = if (encrypted) {
                decryptData(downloadedData)
            } else {
                downloadedData
            }
            
            _backupState.value = BackupState.SUCCESS
            result
        } catch (e: Exception) {
            Log.e(TAG, "Backup download failed", e)
            _backupState.value = BackupState.ERROR(e.message ?: "Unknown error")
            null
        }
    }

    suspend fun listBackups(): List<com.google.api.services.drive.model.File> = withContext(Dispatchers.IO) {
        try {
            val folderId = getOrCreateBackupFolder() ?: return@withContext emptyList()
            
            val query = "'$folderId' in parents and name contains '$BACKUP_FILE_PREFIX' and trashed=false"
            val result = driveService?.files()?.list()
                ?.setQ(query)
                ?.setOrderBy("modifiedTime desc")
                ?.setFields("files(id, name, modifiedTime, size)")
                ?.execute()
            
            result?.files ?: emptyList()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to list backups", e)
            emptyList()
        }
    }
    
    suspend fun deleteBackup(fileId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            driveService?.files()?.delete(fileId)?.execute()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete backup", e)
            false
        }
    }
    
    suspend fun deleteOldBackups(keepCount: Int = 5) = withContext(Dispatchers.IO) {
        try {
            val backups = listBackups()
            if (backups.size > keepCount) {
                backups.drop(keepCount).forEach { file ->
                    deleteBackup(file.id)
                }
            }
            // Return Unit explicitly
            Unit
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete old backups", e)
        }
    }
    
    /**
     * Encrypts data using AES-CBC encryption with a securely stored key
     */
    private fun encryptData(data: String): String {
        try {
            val secretKey = getOrGenerateSecretKey()
            val cipher = Cipher.getInstance(ENCRYPTION_TRANSFORMATION)
            
            // Generate random IV
            val iv = ByteArray(16)
            SecureRandom().nextBytes(iv)
            val ivSpec = IvParameterSpec(iv)
            
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec)
            val encryptedBytes = cipher.doFinal(data.toByteArray(Charsets.UTF_8))
            
            // Combine IV and encrypted data
            val combined = iv + encryptedBytes
            
            return android.util.Base64.encodeToString(combined, android.util.Base64.NO_WRAP)
        } catch (e: Exception) {
            Log.e(TAG, "Encryption failed", e)
            throw SecurityException("Failed to encrypt data", e)
        }
    }
    
    /**
     * Decrypts data that was encrypted using AES-CBC
     */
    private fun decryptData(data: String): String {
        try {
            val secretKey = getOrGenerateSecretKey()
            val cipher = Cipher.getInstance(ENCRYPTION_TRANSFORMATION)
            
            // Decode from Base64
            val combined = android.util.Base64.decode(data, android.util.Base64.NO_WRAP)
            
            // Extract IV and encrypted data
            val iv = combined.sliceArray(0..15)
            val encryptedBytes = combined.sliceArray(16 until combined.size)
            
            val ivSpec = IvParameterSpec(iv)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec)
            
            val decryptedBytes = cipher.doFinal(encryptedBytes)
            return String(decryptedBytes, Charsets.UTF_8)
        } catch (e: Exception) {
            Log.e(TAG, "Decryption failed", e)
            throw SecurityException("Failed to decrypt data", e)
        }
    }
    
    /**
     * Gets the existing secret key from encrypted preferences or generates a new one
     */
    private fun getOrGenerateSecretKey(): SecretKey {
        val keyString = encryptedPrefs.getString(ENCRYPTION_KEY_ALIAS, null)
        
        return if (keyString != null) {
            // Restore existing key
            val keyBytes = android.util.Base64.decode(keyString, android.util.Base64.NO_WRAP)
            SecretKeySpec(keyBytes, "AES")
        } else {
            // Generate new key
            val keyGenerator = KeyGenerator.getInstance("AES")
            keyGenerator.init(KEY_LENGTH)
            val secretKey = keyGenerator.generateKey()
            
            // Store the key securely
            val keyBytes = secretKey.encoded
            val keyString = android.util.Base64.encodeToString(keyBytes, android.util.Base64.NO_WRAP)
            encryptedPrefs.edit().putString(ENCRYPTION_KEY_ALIAS, keyString).apply()
            
            secretKey
        }
    }
    
    enum class BackupState {
        IDLE,
        UPLOADING,
        DOWNLOADING,
        SUCCESS,
        ERROR;
        
        var message: String? = null
        
        companion object {
            fun ERROR(msg: String): BackupState {
                return ERROR.also { it.message = msg }
            }
        }
    }
    
    sealed class BackupResult {
        data class Success(val fileId: String, val fileName: String) : BackupResult()
        data class Failure(val error: String) : BackupResult()
    }
}
