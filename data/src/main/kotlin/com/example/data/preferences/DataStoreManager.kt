package com.voltai.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.voltai.domain.model.PowerProfile
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class DataStoreManager @Inject constructor(@ApplicationContext private val context: Context) {

    private object PreferencesKeys {
        val BRIGHTNESS = intPreferencesKey("brightness")
        val BLUETOOTH = booleanPreferencesKey("bluetooth")
        val SYNC = booleanPreferencesKey("sync")
        val VIBRATION = booleanPreferencesKey("vibration")
        val PROFILE_NAME = stringPreferencesKey("profile_name")
        val OPTIMAL_CHARGING_LIMIT_ENABLED = booleanPreferencesKey("optimal_charging_limit_enabled")
        val OPTIMAL_CHARGING_LIMIT_PERCENTAGE = intPreferencesKey("optimal_charging_limit_percentage")
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        
        // AI Settings
        val AI_ENABLED = booleanPreferencesKey("ai_enabled")
        val AI_PROVIDER = stringPreferencesKey("ai_provider")
        val OPENAI_API_KEY = stringPreferencesKey("openai_api_key")
        val GEMINI_API_KEY = stringPreferencesKey("gemini_api_key")
        val CLAUDE_API_KEY = stringPreferencesKey("claude_api_key")
        val CUSTOM_API_KEY = stringPreferencesKey("custom_api_key")
        val CUSTOM_API_URL = stringPreferencesKey("custom_api_url")
        val AI_RECOMMENDATIONS_ENABLED = booleanPreferencesKey("ai_recommendations_enabled")
        val AI_ANALYSIS_FREQUENCY = stringPreferencesKey("ai_analysis_frequency")
    }

    val powerProfile: Flow<PowerProfile> = context.dataStore.data
        .map {
            PowerProfile(
                name = it[PreferencesKeys.PROFILE_NAME] ?: "Default",
                brightness = it[PreferencesKeys.BRIGHTNESS] ?: 100,
                bluetooth = it[PreferencesKeys.BLUETOOTH] ?: false,
                sync = it[PreferencesKeys.SYNC] ?: false,
                vibration = it[PreferencesKeys.VIBRATION] ?: false
            )
        }

    val optimalChargingSettings: Flow<Pair<Boolean, Int>> = context.dataStore.data
        .map {
            Pair(
                it[PreferencesKeys.OPTIMAL_CHARGING_LIMIT_ENABLED] ?: false,
                it[PreferencesKeys.OPTIMAL_CHARGING_LIMIT_PERCENTAGE] ?: 80
            )
        }

    suspend fun saveOptimalChargingSettings(enabled: Boolean, percentage: Int) {
        context.dataStore.edit { settings ->
            settings[PreferencesKeys.OPTIMAL_CHARGING_LIMIT_ENABLED] = enabled
            settings[PreferencesKeys.OPTIMAL_CHARGING_LIMIT_PERCENTAGE] = percentage
        }
    }

    suspend fun savePowerProfile(profile: PowerProfile) {
        context.dataStore.edit { settings ->
            settings[PreferencesKeys.PROFILE_NAME] = profile.name
            settings[PreferencesKeys.BRIGHTNESS] = profile.brightness
            settings[PreferencesKeys.BLUETOOTH] = profile.bluetooth
            settings[PreferencesKeys.SYNC] = profile.sync
            settings[PreferencesKeys.VIBRATION] = profile.vibration
        }
    }
    
    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { settings ->
            settings[PreferencesKeys.ONBOARDING_COMPLETED] = completed
        }
    }
    
    suspend fun isOnboardingCompleted(): Boolean {
        return context.dataStore.data.map { preferences ->
            preferences[PreferencesKeys.ONBOARDING_COMPLETED] ?: false
        }.first()
    }
    
    // AI Settings Methods
    suspend fun setAIEnabled(enabled: Boolean) {
        context.dataStore.edit { settings ->
            settings[PreferencesKeys.AI_ENABLED] = enabled
        }
    }
    
    suspend fun isAIEnabled(): Boolean {
        return context.dataStore.data.map { preferences ->
            preferences[PreferencesKeys.AI_ENABLED] ?: false
        }.first()
    }
    
    suspend fun setAIProvider(provider: String) {
        context.dataStore.edit { settings ->
            settings[PreferencesKeys.AI_PROVIDER] = provider
        }
    }
    
    suspend fun getAIProvider(): String {
        return context.dataStore.data.map { preferences ->
            preferences[PreferencesKeys.AI_PROVIDER] ?: "tensorflow_lite"
        }.first()
    }
    
    suspend fun setAPIKey(provider: String, apiKey: String) {
        context.dataStore.edit { settings ->
            when (provider) {
                "openai" -> settings[PreferencesKeys.OPENAI_API_KEY] = apiKey
                "gemini" -> settings[PreferencesKeys.GEMINI_API_KEY] = apiKey
                "claude" -> settings[PreferencesKeys.CLAUDE_API_KEY] = apiKey
                "custom" -> settings[PreferencesKeys.CUSTOM_API_KEY] = apiKey
            }
        }
    }
    
    suspend fun getAPIKey(provider: String): String? {
        return context.dataStore.data.map { preferences ->
            when (provider) {
                "openai" -> preferences[PreferencesKeys.OPENAI_API_KEY]
                "gemini" -> preferences[PreferencesKeys.GEMINI_API_KEY]
                "claude" -> preferences[PreferencesKeys.CLAUDE_API_KEY]
                "custom" -> preferences[PreferencesKeys.CUSTOM_API_KEY]
                else -> null
            }
        }.first()
    }
    
    suspend fun setCustomAPIUrl(url: String) {
        context.dataStore.edit { settings ->
            settings[PreferencesKeys.CUSTOM_API_URL] = url
        }
    }
    
    suspend fun getCustomAPIUrl(): String? {
        return context.dataStore.data.map { preferences ->
            preferences[PreferencesKeys.CUSTOM_API_URL]
        }.first()
    }
    
    suspend fun setAIRecommendationsEnabled(enabled: Boolean) {
        context.dataStore.edit { settings ->
            settings[PreferencesKeys.AI_RECOMMENDATIONS_ENABLED] = enabled
        }
    }
    
    suspend fun isAIRecommendationsEnabled(): Boolean {
        return context.dataStore.data.map { preferences ->
            preferences[PreferencesKeys.AI_RECOMMENDATIONS_ENABLED] ?: true
        }.first()
    }
    
    suspend fun setAIAnalysisFrequency(frequency: String) {
        context.dataStore.edit { settings ->
            settings[PreferencesKeys.AI_ANALYSIS_FREQUENCY] = frequency
        }
    }
    
    suspend fun getAIAnalysisFrequency(): String {
        return context.dataStore.data.map { preferences ->
            preferences[PreferencesKeys.AI_ANALYSIS_FREQUENCY] ?: "daily"
        }.first()
    }
}
