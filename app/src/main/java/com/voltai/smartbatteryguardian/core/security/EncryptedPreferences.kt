package com.voltai.smartbatteryguardian.core.security

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.voltai.smartbatteryguardian.core.Result
import com.voltai.smartbatteryguardian.core.safeSuspendCall
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Secure encrypted preferences for sensitive data storage
 * Uses AES256-GCM encryption for maximum security
 */
@Singleton
class EncryptedPreferences
@Inject
constructor(
    @ApplicationContext private val context: Context,
) {
    companion object {
        private const val SECURE_PREFS_FILE = "voltai_secure_prefs"
        private const val PREMIUM_PREFS_FILE = "voltai_premium_prefs"
        private const val ANALYTICS_PREFS_FILE = "voltai_analytics_prefs"

        // Keys for secure storage
        private const val KEY_USER_TOKEN = "user_token"
        private const val KEY_PREMIUM_STATUS = "premium_status"
        private const val KEY_PREMIUM_EXPIRY = "premium_expiry"
        private const val KEY_ANALYTICS_ID = "analytics_id"
        private const val KEY_DEVICE_ID = "device_id"
        private const val KEY_ENCRYPTION_KEY = "encryption_key"
        private const val KEY_BIOMETRIC_ENABLED = "biometric_enabled"
        private const val KEY_APP_LOCK_ENABLED = "app_lock_enabled"
        private const val KEY_PRIVACY_MODE = "privacy_mode"
        private const val KEY_DATA_SHARING_CONSENT = "data_sharing_consent"
        private const val KEY_CRASH_REPORTING_CONSENT = "crash_reporting_consent"
    }

    private val masterKey: MasterKey by lazy {
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .setUserAuthenticationRequired(false) // Set to true for biometric auth
            .setRequestStrongBoxBacked(true) // Use hardware security module if available
            .build()
    }

    private val securePrefs: SharedPreferences by lazy {
        EncryptedSharedPreferences.create(
            context,
            SECURE_PREFS_FILE,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )
    }

    private val premiumPrefs: SharedPreferences by lazy {
        EncryptedSharedPreferences.create(
            context,
            PREMIUM_PREFS_FILE,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )
    }

    private val analyticsPrefs: SharedPreferences by lazy {
        EncryptedSharedPreferences.create(
            context,
            ANALYTICS_PREFS_FILE,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )
    }

    // User Authentication & Security

    suspend fun setUserToken(token: String?): Result<Unit> =
        safeSuspendCall(
            errorCode = "SECURE_STORAGE_ERROR",
            userFriendlyMessage = "Failed to save user authentication",
        ) {
            withContext(Dispatchers.IO) {
                with(securePrefs.edit()) {
                    if (token != null) {
                        putString(KEY_USER_TOKEN, token)
                    } else {
                        remove(KEY_USER_TOKEN)
                    }
                    apply()
                }
            }
        }

    suspend fun getUserToken(): Result<String?> =
        safeSuspendCall(
            errorCode = "SECURE_STORAGE_ERROR",
            userFriendlyMessage = "Failed to retrieve user authentication",
        ) {
            withContext(Dispatchers.IO) {
                securePrefs.getString(KEY_USER_TOKEN, null)
            }
        }

    suspend fun setBiometricEnabled(enabled: Boolean): Result<Unit> =
        safeSuspendCall {
            withContext(Dispatchers.IO) {
                with(securePrefs.edit()) {
                    putBoolean(KEY_BIOMETRIC_ENABLED, enabled)
                    apply()
                }
            }
        }

    suspend fun isBiometricEnabled(): Result<Boolean> =
        safeSuspendCall {
            withContext(Dispatchers.IO) {
                securePrefs.getBoolean(KEY_BIOMETRIC_ENABLED, false)
            }
        }

    suspend fun setAppLockEnabled(enabled: Boolean): Result<Unit> =
        safeSuspendCall {
            withContext(Dispatchers.IO) {
                with(securePrefs.edit()) {
                    putBoolean(KEY_APP_LOCK_ENABLED, enabled)
                    apply()
                }
            }
        }

    suspend fun isAppLockEnabled(): Result<Boolean> =
        safeSuspendCall {
            withContext(Dispatchers.IO) {
                securePrefs.getBoolean(KEY_APP_LOCK_ENABLED, false)
            }
        }

    // Premium Status Management

    suspend fun setPremiumStatus(
        isPremium: Boolean,
        expiryTimestamp: Long = 0L,
    ): Result<Unit> =
        safeSuspendCall(
            errorCode = "PREMIUM_STORAGE_ERROR",
            userFriendlyMessage = "Failed to update premium status",
        ) {
            withContext(Dispatchers.IO) {
                with(premiumPrefs.edit()) {
                    putBoolean(KEY_PREMIUM_STATUS, isPremium)
                    putLong(KEY_PREMIUM_EXPIRY, expiryTimestamp)
                    apply()
                }
            }
        }

    suspend fun isPremiumUser(): Result<Boolean> =
        safeSuspendCall {
            withContext(Dispatchers.IO) {
                val isPremium = premiumPrefs.getBoolean(KEY_PREMIUM_STATUS, false)
                val expiryTime = premiumPrefs.getLong(KEY_PREMIUM_EXPIRY, 0L)

                // Check if premium status is still valid
                if (isPremium && expiryTime > 0L && System.currentTimeMillis() > expiryTime) {
                    // Premium expired, update status
                    with(premiumPrefs.edit()) {
                        putBoolean(KEY_PREMIUM_STATUS, false)
                        apply()
                    }
                    false
                } else {
                    isPremium
                }
            }
        }

    suspend fun getPremiumExpiryTime(): Result<Long> =
        safeSuspendCall {
            withContext(Dispatchers.IO) {
                premiumPrefs.getLong(KEY_PREMIUM_EXPIRY, 0L)
            }
        }

    // Privacy & Analytics

    suspend fun setPrivacyMode(enabled: Boolean): Result<Unit> =
        safeSuspendCall {
            withContext(Dispatchers.IO) {
                with(analyticsPrefs.edit()) {
                    putBoolean(KEY_PRIVACY_MODE, enabled)
                    apply()
                }
            }
        }

    suspend fun isPrivacyModeEnabled(): Result<Boolean> =
        safeSuspendCall {
            withContext(Dispatchers.IO) {
                analyticsPrefs.getBoolean(KEY_PRIVACY_MODE, false)
            }
        }

    suspend fun setDataSharingConsent(consent: Boolean): Result<Unit> =
        safeSuspendCall {
            withContext(Dispatchers.IO) {
                with(analyticsPrefs.edit()) {
                    putBoolean(KEY_DATA_SHARING_CONSENT, consent)
                    apply()
                }
            }
        }

    suspend fun hasDataSharingConsent(): Result<Boolean> =
        safeSuspendCall {
            withContext(Dispatchers.IO) {
                analyticsPrefs.getBoolean(KEY_DATA_SHARING_CONSENT, false)
            }
        }

    suspend fun setCrashReportingConsent(consent: Boolean): Result<Unit> =
        safeSuspendCall {
            withContext(Dispatchers.IO) {
                with(analyticsPrefs.edit()) {
                    putBoolean(KEY_CRASH_REPORTING_CONSENT, consent)
                    apply()
                }
            }
        }

    suspend fun hasCrashReportingConsent(): Result<Boolean> =
        safeSuspendCall {
            withContext(Dispatchers.IO) {
                analyticsPrefs.getBoolean(KEY_CRASH_REPORTING_CONSENT, true) // Default to true
            }
        }

    suspend fun setAnalyticsId(analyticsId: String): Result<Unit> =
        safeSuspendCall {
            withContext(Dispatchers.IO) {
                with(analyticsPrefs.edit()) {
                    putString(KEY_ANALYTICS_ID, analyticsId)
                    apply()
                }
            }
        }

    suspend fun getAnalyticsId(): Result<String?> =
        safeSuspendCall {
            withContext(Dispatchers.IO) {
                analyticsPrefs.getString(KEY_ANALYTICS_ID, null)
            }
        }

    suspend fun setDeviceId(deviceId: String): Result<Unit> =
        safeSuspendCall {
            withContext(Dispatchers.IO) {
                with(securePrefs.edit()) {
                    putString(KEY_DEVICE_ID, deviceId)
                    apply()
                }
            }
        }

    suspend fun getDeviceId(): Result<String?> =
        safeSuspendCall {
            withContext(Dispatchers.IO) {
                securePrefs.getString(KEY_DEVICE_ID, null)
            }
        }

    // Encryption Key Management

    suspend fun generateAndStoreEncryptionKey(): Result<String> =
        safeSuspendCall(
            errorCode = "ENCRYPTION_KEY_ERROR",
            userFriendlyMessage = "Failed to generate encryption key",
        ) {
            withContext(Dispatchers.IO) {
                val key = java.util.UUID.randomUUID().toString()
                with(securePrefs.edit()) {
                    putString(KEY_ENCRYPTION_KEY, key)
                    apply()
                }
                key
            }
        }

    suspend fun getEncryptionKey(): Result<String?> =
        safeSuspendCall {
            withContext(Dispatchers.IO) {
                securePrefs.getString(KEY_ENCRYPTION_KEY, null)
            }
        }

    // Utility Methods

    suspend fun clearAllSecureData(): Result<Unit> =
        safeSuspendCall(
            errorCode = "CLEAR_DATA_ERROR",
            userFriendlyMessage = "Failed to clear secure data",
        ) {
            withContext(Dispatchers.IO) {
                securePrefs.edit().clear().apply()
                premiumPrefs.edit().clear().apply()
                analyticsPrefs.edit().clear().apply()
            }
        }

    suspend fun clearUserData(): Result<Unit> =
        safeSuspendCall(
            errorCode = "CLEAR_USER_DATA_ERROR",
            userFriendlyMessage = "Failed to clear user data",
        ) {
            withContext(Dispatchers.IO) {
                with(securePrefs.edit()) {
                    remove(KEY_USER_TOKEN)
                    remove(KEY_ANALYTICS_ID)
                    apply()
                }
            }
        }

    suspend fun exportSecureSettings(): Result<Map<String, Any>> =
        safeSuspendCall(
            errorCode = "EXPORT_SETTINGS_ERROR",
            userFriendlyMessage = "Failed to export settings",
        ) {
            withContext(Dispatchers.IO) {
                mapOf(
                    "biometric_enabled" to securePrefs.getBoolean(KEY_BIOMETRIC_ENABLED, false),
                    "app_lock_enabled" to securePrefs.getBoolean(KEY_APP_LOCK_ENABLED, false),
                    "privacy_mode" to analyticsPrefs.getBoolean(KEY_PRIVACY_MODE, false),
                    "data_sharing_consent" to analyticsPrefs.getBoolean(KEY_DATA_SHARING_CONSENT, false),
                    "crash_reporting_consent" to analyticsPrefs.getBoolean(KEY_CRASH_REPORTING_CONSENT, true),
                    "premium_status" to premiumPrefs.getBoolean(KEY_PREMIUM_STATUS, false),
                    "premium_expiry" to premiumPrefs.getLong(KEY_PREMIUM_EXPIRY, 0L),
                )
            }
        }

    suspend fun importSecureSettings(settings: Map<String, Any>): Result<Unit> =
        safeSuspendCall(
            errorCode = "IMPORT_SETTINGS_ERROR",
            userFriendlyMessage = "Failed to import settings",
        ) {
            withContext(Dispatchers.IO) {
                // Import non-sensitive settings only
                with(securePrefs.edit()) {
                    settings["biometric_enabled"]?.let {
                        putBoolean(KEY_BIOMETRIC_ENABLED, it as Boolean)
                    }
                    settings["app_lock_enabled"]?.let {
                        putBoolean(KEY_APP_LOCK_ENABLED, it as Boolean)
                    }
                    apply()
                }

                with(analyticsPrefs.edit()) {
                    settings["privacy_mode"]?.let {
                        putBoolean(KEY_PRIVACY_MODE, it as Boolean)
                    }
                    settings["data_sharing_consent"]?.let {
                        putBoolean(KEY_DATA_SHARING_CONSENT, it as Boolean)
                    }
                    settings["crash_reporting_consent"]?.let {
                        putBoolean(KEY_CRASH_REPORTING_CONSENT, it as Boolean)
                    }
                    apply()
                }

                // Premium settings require additional validation
                settings["premium_status"]?.let { isPremium ->
                    settings["premium_expiry"]?.let { expiry ->
                        setPremiumStatus(isPremium as Boolean, expiry as Long)
                    }
                }
            }
        }

    /**
     * Validates the integrity of encrypted preferences
     */
    suspend fun validateIntegrity(): Result<Boolean> =
        safeSuspendCall(
            errorCode = "INTEGRITY_CHECK_ERROR",
            userFriendlyMessage = "Security validation failed",
        ) {
            withContext(Dispatchers.IO) {
                try {
                    // Attempt to read a value to ensure encryption is working
                    securePrefs.getString(KEY_DEVICE_ID, null)
                    premiumPrefs.getBoolean(KEY_PREMIUM_STATUS, false)
                    analyticsPrefs.getBoolean(KEY_PRIVACY_MODE, false)
                    true
                } catch (e: Exception) {
                    false
                }
            }
        }

    /**
     * Gets secure preferences statistics for debugging (non-sensitive info only)
     */
    suspend fun getPreferencesStats(): Result<Map<String, Int>> =
        safeSuspendCall {
            withContext(Dispatchers.IO) {
                mapOf(
                    "secure_keys_count" to securePrefs.all.size,
                    "premium_keys_count" to premiumPrefs.all.size,
                    "analytics_keys_count" to analyticsPrefs.all.size,
                )
            }
        }
}
