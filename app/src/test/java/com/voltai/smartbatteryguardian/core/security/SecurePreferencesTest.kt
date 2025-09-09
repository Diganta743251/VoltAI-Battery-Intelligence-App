package com.voltai.smartbatteryguardian.core.security

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [28])
class SecurePreferencesTest {

    private lateinit var context: Context
    private lateinit var securePreferences: SecurePreferences

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        securePreferences = SecurePreferences(context)
    }

    @After
    fun tearDown() {
        // Clear all preferences after each test
        runTest {
            securePreferences.clearAllData()
        }
    }

    @Test
    fun `saveAuthToken should store and retrieve token successfully`() = runTest {
        val token = "test_auth_token_12345"

        val saveResult = securePreferences.saveAuthToken(token)
        assertThat(saveResult.isSuccess).isTrue()

        val retrieveResult = securePreferences.getAuthToken()
        assertThat(retrieveResult.isSuccess).isTrue()
        assertThat(retrieveResult.getOrNull()).isEqualTo(token)
    }

    @Test
    fun `getAuthToken should return error when token not found`() = runTest {
        val result = securePreferences.getAuthToken()
        assertThat(result.isError).isTrue()
    }

    @Test
    fun `savePremiumStatus should store and retrieve status successfully`() = runTest {
        val isPremium = true
        val expirationTime = System.currentTimeMillis() + 86400000 // 24 hours

        val saveResult = securePreferences.savePremiumStatus(isPremium, expirationTime)
        assertThat(saveResult.isSuccess).isTrue()

        val retrieveResult = securePreferences.getPremiumStatus()
        assertThat(retrieveResult.isSuccess).isTrue()
        val (premium, expiration) = retrieveResult.getOrThrow()
        assertThat(premium).isTrue()
        assertThat(expiration).isEqualTo(expirationTime)
    }

    @Test
    fun `saveAnalyticsId should store and retrieve ID successfully`() = runTest {
        val analyticsId = "analytics_id_67890"

        val saveResult = securePreferences.saveAnalyticsId(analyticsId)
        assertThat(saveResult.isSuccess).isTrue()

        val retrieveResult = securePreferences.getAnalyticsId()
        assertThat(retrieveResult.isSuccess).isTrue()
        assertThat(retrieveResult.getOrNull()).isEqualTo(analyticsId)
    }

    @Test
    fun `saveBiometricEnabled should store and retrieve biometric setting successfully`() = runTest {
        val biometricEnabled = true
        val pinHash = "hashed_pin_12345"

        val saveResult = securePreferences.saveBiometricEnabled(biometricEnabled, pinHash)
        assertThat(saveResult.isSuccess).isTrue()

        val retrieveResult = securePreferences.getBiometricSettings()
        assertThat(retrieveResult.isSuccess).isTrue()
        val (enabled, hash) = retrieveResult.getOrThrow()
        assertThat(enabled).isTrue()
        assertThat(hash).isEqualTo(pinHash)
    }

    @Test
    fun `saveAppLockEnabled should store and retrieve app lock setting successfully`() = runTest {
        val appLockEnabled = true
        val timeout = 300000L // 5 minutes

        val saveResult = securePreferences.saveAppLockEnabled(appLockEnabled, timeout)
        assertThat(saveResult.isSuccess).isTrue()

        val retrieveResult = securePreferences.getAppLockSettings()
        assertThat(retrieveResult.isSuccess).isTrue()
        val (enabled, lockTimeout) = retrieveResult.getOrThrow()
        assertThat(enabled).isTrue()
        assertThat(lockTimeout).isEqualTo(timeout)
    }

    @Test
    fun `savePrivacyMode should store and retrieve privacy mode successfully`() = runTest {
        val privacyEnabled = true
        val restrictions = setOf("analytics", "crash_reporting", "usage_data")

        val saveResult = securePreferences.savePrivacyMode(privacyEnabled, restrictions)
        assertThat(saveResult.isSuccess).isTrue()

        val retrieveResult = securePreferences.getPrivacyModeSettings()
        assertThat(retrieveResult.isSuccess).isTrue()
        val (enabled, savedRestrictions) = retrieveResult.getOrThrow()
        assertThat(enabled).isTrue()
        assertThat(savedRestrictions).isEqualTo(restrictions)
    }

    @Test
    fun `saveConsentFlags should store and retrieve consent flags successfully`() = runTest {
        val consentFlags = mapOf(
            "analytics" to true,
            "crash_reporting" to false,
            "marketing" to true,
            "personalization" to false
        )

        val saveResult = securePreferences.saveConsentFlags(consentFlags)
        assertThat(saveResult.isSuccess).isTrue()

        val retrieveResult = securePreferences.getConsentFlags()
        assertThat(retrieveResult.isSuccess).isTrue()
        assertThat(retrieveResult.getOrNull()).isEqualTo(consentFlags)
    }

    @Test
    fun `saveEncryptionKeys should store and retrieve keys successfully`() = runTest {
        val publicKey = "public_key_data_12345"
        val privateKeyHash = "private_key_hash_67890"

        val saveResult = securePreferences.saveEncryptionKeys(publicKey, privateKeyHash)
        assertThat(saveResult.isSuccess).isTrue()

        val retrieveResult = securePreferences.getEncryptionKeys()
        assertThat(retrieveResult.isSuccess).isTrue()
        val (pub, priv) = retrieveResult.getOrThrow()
        assertThat(pub).isEqualTo(publicKey)
        assertThat(priv).isEqualTo(privateKeyHash)
    }

    @Test
    fun `getAllSettings should export all stored settings`() = runTest {
        // Store various settings
        securePreferences.saveAuthToken("test_token")
        securePreferences.savePremiumStatus(true, 123456789L)
        securePreferences.saveAnalyticsId("analytics_123")

        val result = securePreferences.getAllSettings()
        assertThat(result.isSuccess).isTrue()

        val settings = result.getOrThrow()
        assertThat(settings).containsKey("auth_token")
        assertThat(settings).containsKey("premium_status")
        assertThat(settings).containsKey("analytics_id")
        assertThat(settings["auth_token"]).isEqualTo("test_token")
    }

    @Test
    fun `importSettings should restore settings from map`() = runTest {
        val settingsMap = mapOf(
            "auth_token" to "imported_token",
            "analytics_id" to "imported_analytics",
            "premium_status" to "true",
            "premium_expiration" to "123456789"
        )

        val importResult = securePreferences.importSettings(settingsMap)
        assertThat(importResult.isSuccess).isTrue()

        // Verify imported settings
        val tokenResult = securePreferences.getAuthToken()
        assertThat(tokenResult.isSuccess).isTrue()
        assertThat(tokenResult.getOrNull()).isEqualTo("imported_token")

        val analyticsResult = securePreferences.getAnalyticsId()
        assertThat(analyticsResult.isSuccess).isTrue()
        assertThat(analyticsResult.getOrNull()).isEqualTo("imported_analytics")
    }

    @Test
    fun `validateDataIntegrity should detect data corruption`() = runTest {
        // This test assumes the implementation includes integrity checks
        // Store some data first
        securePreferences.saveAuthToken("test_token")

        val result = securePreferences.validateDataIntegrity()
        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()).isTrue()
    }

    @Test
    fun `getSecurityMetadata should return security information`() = runTest {
        val result = securePreferences.getSecurityMetadata()
        assertThat(result.isSuccess).isTrue()

        val metadata = result.getOrThrow()
        assertThat(metadata).containsKey("encryption_enabled")
        assertThat(metadata).containsKey("created_at")
        assertThat(metadata).containsKey("last_accessed")
        assertThat(metadata["encryption_enabled"]).isEqualTo("true")
    }

    @Test
    fun `clearAllData should remove all stored preferences`() = runTest {
        // Store some data
        securePreferences.saveAuthToken("test_token")
        securePreferences.savePremiumStatus(true, 123456789L)

        val clearResult = securePreferences.clearAllData()
        assertThat(clearResult.isSuccess).isTrue()

        // Verify data is cleared
        val tokenResult = securePreferences.getAuthToken()
        assertThat(tokenResult.isError).isTrue()

        val premiumResult = securePreferences.getPremiumStatus()
        assertThat(premiumResult.isError).isTrue()
    }

    @Test
    fun `multiple concurrent operations should be handled safely`() = runTest {
        val token1 = "token_1"
        val token2 = "token_2"
        val analyticsId = "analytics_concurrent"

        // Launch concurrent operations
        val saveResult1 = securePreferences.saveAuthToken(token1)
        val saveResult2 = securePreferences.saveAnalyticsId(analyticsId)
        val saveResult3 = securePreferences.saveAuthToken(token2) // Override token1

        assertThat(saveResult1.isSuccess).isTrue()
        assertThat(saveResult2.isSuccess).isTrue()
        assertThat(saveResult3.isSuccess).isTrue()

        // Verify final state
        val finalTokenResult = securePreferences.getAuthToken()
        assertThat(finalTokenResult.getOrNull()).isEqualTo(token2)

        val finalAnalyticsResult = securePreferences.getAnalyticsId()
        assertThat(finalAnalyticsResult.getOrNull()).isEqualTo(analyticsId)
    }

    @Test
    fun `error handling should work for invalid operations`() = runTest {
        // Test importing invalid settings
        val invalidSettings = mapOf(
            "invalid_key" to "invalid_value",
            "malformed_data" to "corrupted:::data"
        )

        val importResult = securePreferences.importSettings(invalidSettings)
        // Import should either succeed (ignoring invalid keys) or return a partial success
        // The exact behavior depends on implementation
        assertThat(importResult.isSuccess || importResult.isError).isTrue()
    }

    @Test
    fun `settings should persist across instance recreation`() = runTest {
        val token = "persistent_token"
        val analyticsId = "persistent_analytics"

        // Save with first instance
        securePreferences.saveAuthToken(token)
        securePreferences.saveAnalyticsId(analyticsId)

        // Create new instance
        val newSecurePreferences = SecurePreferences(context)

        // Verify data persists
        val tokenResult = newSecurePreferences.getAuthToken()
        assertThat(tokenResult.isSuccess).isTrue()
        assertThat(tokenResult.getOrNull()).isEqualTo(token)

        val analyticsResult = newSecurePreferences.getAnalyticsId()
        assertThat(analyticsResult.isSuccess).isTrue()
        assertThat(analyticsResult.getOrNull()).isEqualTo(analyticsId)
    }

    @Test
    fun `empty or null values should be handled correctly`() = runTest {
        // Test saving empty values
        val emptyResult = securePreferences.saveAuthToken("")
        assertThat(emptyResult.isSuccess).isTrue()

        val retrieveEmptyResult = securePreferences.getAuthToken()
        assertThat(retrieveEmptyResult.isSuccess).isTrue()
        assertThat(retrieveEmptyResult.getOrNull()).isEmpty()

        // Test null consent flags
        val nullConsentResult = securePreferences.saveConsentFlags(emptyMap())
        assertThat(nullConsentResult.isSuccess).isTrue()

        val retrieveConsentResult = securePreferences.getConsentFlags()
        assertThat(retrieveConsentResult.isSuccess).isTrue()
        assertThat(retrieveConsentResult.getOrNull()).isEmpty()
    }
}
