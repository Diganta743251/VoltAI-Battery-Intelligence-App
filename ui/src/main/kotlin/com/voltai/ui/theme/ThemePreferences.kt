package com.voltai.ui.theme

import android.content.Context
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * Supported Theme Modes for VoltAI UI
 */
enum class ThemeMode(val title: String) {
    SYSTEM("Follow System"),
    LIGHT("Light Mode"),
    DARK("Dark Mode");

    companion object {
        fun fromString(value: String?): ThemeMode {
            return when (value?.uppercase()) {
                "LIGHT" -> LIGHT
                "DARK" -> DARK
                else -> SYSTEM
            }
        }
    }
}

val Context.themePreferencesDataStore: DataStore<Preferences> by preferencesDataStore(name = "voltai_theme_preferences")

object ThemePreferences {
    private val KEY_THEME_MODE = stringPreferencesKey("theme_mode")
    private val KEY_DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color_enabled")

    fun getThemeModeFlow(context: Context): Flow<ThemeMode> {
        return context.themePreferencesDataStore.data.map { prefs ->
            ThemeMode.fromString(prefs[KEY_THEME_MODE])
        }
    }

    fun getDynamicColorFlow(context: Context): Flow<Boolean> {
        return context.themePreferencesDataStore.data.map { prefs ->
            prefs[KEY_DYNAMIC_COLOR] ?: true
        }
    }

    suspend fun setThemeMode(context: Context, mode: ThemeMode) {
        context.themePreferencesDataStore.edit { prefs ->
            prefs[KEY_THEME_MODE] = mode.name
        }
    }

    suspend fun setDynamicColor(context: Context, enabled: Boolean) {
        context.themePreferencesDataStore.edit { prefs ->
            prefs[KEY_DYNAMIC_COLOR] = enabled
        }
    }

    suspend fun toggleTheme(context: Context, currentIsDark: Boolean) {
        val nextMode = if (currentIsDark) ThemeMode.LIGHT else ThemeMode.DARK
        setThemeMode(context, nextMode)
    }
}

val LocalThemeMode = compositionLocalOf { ThemeMode.SYSTEM }
val LocalDynamicColor = compositionLocalOf { true }
val LocalThemeToggleAction = compositionLocalOf<() -> Unit> { {} }

data class ThemeState(
    val themeMode: ThemeMode,
    val isDark: Boolean,
    val dynamicColor: Boolean,
    val onToggleTheme: () -> Unit,
    val onSetThemeMode: (ThemeMode) -> Unit,
    val onSetDynamicColor: (Boolean) -> Unit,
)

@Composable
fun rememberThemeState(
    context: Context = LocalContext.current,
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
): ThemeState {
    val systemDark = isSystemInDarkTheme()
    val themeModeFlow = remember(context) { ThemePreferences.getThemeModeFlow(context) }
    val dynamicColorFlow = remember(context) { ThemePreferences.getDynamicColorFlow(context) }

    val themeMode by themeModeFlow.collectAsState(initial = ThemeMode.SYSTEM)
    val dynamicColor by dynamicColorFlow.collectAsState(initial = true)

    val isDark = when (themeMode) {
        ThemeMode.SYSTEM -> systemDark
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    return remember(themeMode, isDark, dynamicColor) {
        ThemeState(
            themeMode = themeMode,
            isDark = isDark,
            dynamicColor = dynamicColor,
            onToggleTheme = {
                coroutineScope.launch {
                    ThemePreferences.toggleTheme(context, isDark)
                }
            },
            onSetThemeMode = { newMode ->
                coroutineScope.launch {
                    ThemePreferences.setThemeMode(context, newMode)
                }
            },
            onSetDynamicColor = { enabled ->
                coroutineScope.launch {
                    ThemePreferences.setDynamicColor(context, enabled)
                }
            },
        )
    }
}
