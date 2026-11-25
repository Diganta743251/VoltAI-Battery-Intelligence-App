package com.voltai.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF69F0AE),
    onPrimary = Color(0xFF003820),
    primaryContainer = Color(0xFF00522F),
    onPrimaryContainer = Color(0xFF85FDC8),
    secondary = Color(0xFF90CAF9),
    onSecondary = Color(0xFF003258),
    secondaryContainer = Color(0xFF004880),
    onSecondaryContainer = Color(0xFFC8E6FF),
    tertiary = Color(0xFFFFB300),
    onTertiary = Color(0xFF3E2D00),
    tertiaryContainer = Color(0xFF5A4300),
    onTertiaryContainer = Color(0xFFFFDF9E),
    background = Color(0xFF111415),
    surface = Color(0xFF181C1E),
    onBackground = Color(0xFFE1E3E4),
    onSurface = Color(0xFFE1E3E4),
    surfaceVariant = Color(0xFF40484B),
    onSurfaceVariant = Color(0xFFBFC8CB),
    outline = Color(0xFF899295),
    outlineVariant = Color(0xFF40484B),
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF006C3B),
    onPrimary = Color.White,
    primaryContainer = Color(0xFF85FDC8),
    onPrimaryContainer = Color(0xFF00210F),
    secondary = Color(0xFF00639B),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFCDE5FF),
    onSecondaryContainer = Color(0xFF001D32),
    tertiary = Color(0xFF7B5800),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFDF9E),
    onTertiaryContainer = Color(0xFF261900),
    background = Color(0xFFFBFDFE),
    surface = Color(0xFFFBFDFE),
    onBackground = Color(0xFF191C1D),
    onSurface = Color(0xFF191C1D),
    surfaceVariant = Color(0xFFDBE4E7),
    onSurfaceVariant = Color(0xFF40484B),
    outline = Color(0xFF70787C),
    outlineVariant = Color(0xFFBFC8CB),
)

/**
 * Dynamic VoltAI theme supporting runtime Light/Dark mode toggling backed by Preferences DataStore.
 */
@Composable
fun VoltAITheme(
    useDataStore: Boolean = true,
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val themeState = rememberThemeState()

    val effectiveDarkTheme = if (useDataStore) themeState.isDark else darkTheme
    val effectiveDynamicColor = if (useDataStore) themeState.dynamicColor else dynamicColor

    val context = LocalContext.current
    val colorScheme = when {
        effectiveDynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (effectiveDarkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        effectiveDarkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            window.statusBarColor = colorScheme.surface.toArgb()
            window.navigationBarColor = colorScheme.surface.toArgb()
            val controller = WindowCompat.getInsetsController(window, view)
            controller.isAppearanceLightStatusBars = !effectiveDarkTheme
            controller.isAppearanceLightNavigationBars = !effectiveDarkTheme
        }
    }

    CompositionLocalProvider(
        LocalThemeMode provides themeState.themeMode,
        LocalDynamicColor provides themeState.dynamicColor,
        LocalThemeToggleAction provides themeState.onToggleTheme,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}