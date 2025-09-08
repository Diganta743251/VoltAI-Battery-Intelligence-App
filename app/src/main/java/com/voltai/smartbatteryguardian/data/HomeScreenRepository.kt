package com.voltai.smartbatteryguardian.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.voltai.smartbatteryguardian.ui.home.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

val Context.homeScreenDataStore: DataStore<Preferences> by preferencesDataStore(name = "home_screen_prefs")

@Singleton
class HomeScreenRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object PreferencesKeys {
        val HOME_SCREEN_CONFIG = stringPreferencesKey("home_screen_config")
        val GRID_COLUMNS = intPreferencesKey("grid_columns")
        val CARD_SPACING = floatPreferencesKey("card_spacing")
        val ENABLE_ANIMATIONS = booleanPreferencesKey("enable_animations")
        val ENABLE_HAPTICS = booleanPreferencesKey("enable_haptics")
        val HOME_THEME = stringPreferencesKey("home_theme")
        val CONFIG_VERSION = intPreferencesKey("config_version")
    }
    
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }
    
    /**
     * Save the complete home screen configuration
     */
    suspend fun saveHomeScreenConfig(config: HomeScreenConfig) {
        try {
            context.homeScreenDataStore.edit { preferences ->
                // Save serialized card configuration
                val cardsJson = json.encodeToString(config.cards.map { card ->
                    SerializableHomeCard(
                        id = card.id,
                        cardTypeId = card.cardType.id,
                        sizeName = card.size.name,
                        row = card.row,
                        column = card.column,
                        isVisible = card.isVisible,
                        customTitle = card.customTitle,
                        lastUpdated = card.lastUpdated
                    )
                })
                preferences[PreferencesKeys.HOME_SCREEN_CONFIG] = cardsJson
                
                // Save other config properties
                preferences[PreferencesKeys.GRID_COLUMNS] = config.gridColumns
                preferences[PreferencesKeys.CARD_SPACING] = config.cardSpacing
                preferences[PreferencesKeys.ENABLE_ANIMATIONS] = config.enableAnimations
                preferences[PreferencesKeys.ENABLE_HAPTICS] = config.enableHaptics
                preferences[PreferencesKeys.HOME_THEME] = config.theme.name
                preferences[PreferencesKeys.CONFIG_VERSION] = config.version
            }
        } catch (e: Exception) {
            // Handle serialization errors gracefully
            throw HomeScreenPersistenceException("Failed to save home screen config", e)
        }
    }
    
    /**
     * Load the home screen configuration
     */
    fun loadHomeScreenConfig(): Flow<HomeScreenConfig> {
        return context.homeScreenDataStore.data
            .catch { exception ->
                // If we can't load, emit default config
                emit(emptyPreferences())
            }
            .map { preferences ->
                try {
                    val cardsJson = preferences[PreferencesKeys.HOME_SCREEN_CONFIG]
                    val cards = if (cardsJson != null) {
                        val serializableCards = json.decodeFromString<List<SerializableHomeCard>>(cardsJson)
                        serializableCards.mapNotNull { serializableCard ->
                            try {
                                HomeCard(
                                    id = serializableCard.id,
                                    cardType = CardType.values().find { it.id == serializableCard.cardTypeId }
                                        ?: return@mapNotNull null,
                                    size = CardSize.values().find { it.name == serializableCard.sizeName }
                                        ?: CardSize.Medium,
                                    row = serializableCard.row,
                                    column = serializableCard.column,
                                    isVisible = serializableCard.isVisible,
                                    customTitle = serializableCard.customTitle,
                                    lastUpdated = serializableCard.lastUpdated
                                )
                            } catch (e: Exception) {
                                null // Skip invalid cards
                            }
                        }
                    } else {
                        getDefaultHomeCards()
                    }
                    
                    HomeScreenConfig(
                        cards = cards,
                        gridColumns = preferences[PreferencesKeys.GRID_COLUMNS] ?: 2,
                        cardSpacing = preferences[PreferencesKeys.CARD_SPACING] ?: 12f,
                        enableAnimations = preferences[PreferencesKeys.ENABLE_ANIMATIONS] ?: true,
                        enableHaptics = preferences[PreferencesKeys.ENABLE_HAPTICS] ?: true,
                        theme = preferences[PreferencesKeys.HOME_THEME]?.let { themeName ->
                            HomeTheme.values().find { it.name == themeName }
                        } ?: HomeTheme.AUTO,
                        version = preferences[PreferencesKeys.CONFIG_VERSION] ?: 1
                    )
                } catch (e: Exception) {
                    // Return default config if loading fails
                    HomeScreenConfig()
                }
            }
    }
    
    /**
     * Clear all saved configurations
     */
    suspend fun clearHomeScreenConfig() {
        context.homeScreenDataStore.edit { preferences ->
            preferences.clear()
        }
    }
    
    /**
     * Check if config exists
     */
    fun hasStoredConfig(): Flow<Boolean> {
        return context.homeScreenDataStore.data
            .catch { emit(emptyPreferences()) }
            .map { preferences ->
                preferences[PreferencesKeys.HOME_SCREEN_CONFIG] != null
            }
    }
    
    /**
     * Save individual card state
     */
    suspend fun saveCardState(cardId: String, isVisible: Boolean) {
        try {
            // Get current configuration
            loadHomeScreenConfig().first().let { currentConfig ->
                // Update the specific card's visibility
                val updatedCards = currentConfig.cards.map { card ->
                    if (card.id == cardId) {
                        card.copy(isVisible = isVisible)
                    } else {
                        card
                    }
                }
                
                // Save updated configuration
                val updatedConfig = currentConfig.copy(cards = updatedCards)
                saveHomeScreenConfig(updatedConfig)
            }
        } catch (e: Exception) {
            throw HomeScreenPersistenceException("Failed to save card state for $cardId", e)
        }
    }
}

/**
 * Serializable version of HomeCard for DataStore persistence
 */
@kotlinx.serialization.Serializable
data class SerializableHomeCard(
    val id: String,
    val cardTypeId: String,
    val sizeName: String,
    val row: Int,
    val column: Int,
    val isVisible: Boolean = true,
    val customTitle: String? = null,
    val lastUpdated: Long = System.currentTimeMillis()
)

/**
 * Exception thrown when persistence operations fail
 */
class HomeScreenPersistenceException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)
