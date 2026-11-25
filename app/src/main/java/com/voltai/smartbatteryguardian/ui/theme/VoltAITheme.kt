package com.voltai.smartbatteryguardian.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

// Seed color for dynamic theming (green for battery/energy theme)
private val SeedColor = Color(0xFF4CAF50)

private val DarkColorScheme =
    darkColorScheme(
        primary = Color(0xFF69F0AE),
        onPrimary = Color(0xFF003820),
        primaryContainer = Color(0xFF00522F),
        onPrimaryContainer = Color(0xFF85FDC8),
        secondary = Color(0xFF90CAF9),
        onSecondary = Color(0xFF003258),
        secondaryContainer = Color(0xFF004880),
        onSecondaryContainer = Color(0xFFC8E6FF),
        tertiary = Color(0xFFFFCC02),
        onTertiary = Color(0xFF3A3000),
        tertiaryContainer = Color(0xFF554600),
        onTertiaryContainer = Color(0xFFFFE15C),
        error = Color(0xFFFFB4AB),
        errorContainer = Color(0xFF93000A),
        onError = Color(0xFF690005),
        onErrorContainer = Color(0xFFFFDAD6),
        background = Color(0xFF0F1417),
        onBackground = Color(0xFFDFE3E6),
        surface = Color(0xFF0F1417),
        onSurface = Color(0xFFDFE3E6),
        surfaceVariant = Color(0xFF3F484D),
        onSurfaceVariant = Color(0xFFBFC8CE),
        surfaceTint = Color(0xFF69F0AE),
        inverseSurface = Color(0xFFDFE3E6),
        inverseOnSurface = Color(0xFF2E3135),
        inversePrimary = Color(0xFF006C3C),
        outline = Color(0xFF899297),
        outlineVariant = Color(0xFF3F484D),
        scrim = Color(0xFF000000),
    )

private val LightColorScheme =
    lightColorScheme(
        primary = Color(0xFF006C3C),
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFF85FDC8),
        onPrimaryContainer = Color(0xFF002112),
        secondary = Color(0xFF0066A8),
        onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFFC8E6FF),
        onSecondaryContainer = Color(0xFF001E32),
        tertiary = Color(0xFF6F5E00),
        onTertiary = Color(0xFFFFFFFF),
        tertiaryContainer = Color(0xFFFFE15C),
        onTertiaryContainer = Color(0xFF211B00),
        error = Color(0xFFBA1A1A),
        errorContainer = Color(0xFFFFDAD6),
        onError = Color(0xFFFFFFFF),
        onErrorContainer = Color(0xFF410002),
        background = Color(0xFFF7FAFA),
        onBackground = Color(0xFF181C1D),
        surface = Color(0xFFF7FAFA),
        onSurface = Color(0xFF181C1D),
        surfaceVariant = Color(0xFFDAE5E8),
        onSurfaceVariant = Color(0xFF3F484D),
        surfaceTint = Color(0xFF006C3C),
        inverseSurface = Color(0xFF2D3132),
        inverseOnSurface = Color(0xFFEEF2F3),
        inversePrimary = Color(0xFF69F0AE),
        outline = Color(0xFF6F797E),
        outlineVariant = Color(0xFFBFC8CE),
        scrim = Color(0xFF000000),
    )

@Composable
fun VoltAITheme(
    useDataStore: Boolean = true,
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val themeState = com.voltai.ui.theme.rememberThemeState()
    val effectiveDarkTheme = if (useDataStore) themeState.isDark else darkTheme
    val effectiveDynamicColor = if (useDataStore) themeState.dynamicColor else dynamicColor

    val colorScheme =
        when {
            effectiveDynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                val context = LocalContext.current
                if (effectiveDarkTheme) {
                    dynamicDarkColorScheme(context)
                } else {
                    dynamicLightColorScheme(context)
                }
            }
            effectiveDarkTheme -> DarkColorScheme
            else -> LightColorScheme
        }

    ProvideExtendedColors(darkTheme = effectiveDarkTheme) {
        ProvidePremiumColors(rememberPremiumColorsFromExtended()) {
            MaterialTheme(
                colorScheme = colorScheme,
                typography = AppTypography,
                shapes = AppShapes,
                content = content,
            )
        }
    }
}
