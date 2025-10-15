package com.voltai.smartbatteryguardian.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Extended color palette for brand accents (e.g., premium gold) that are not part of the core M3 ColorScheme.
 * Keeps brand identity while adapting contrast for dark/light themes.
 */
data class ExtendedColors(
    val premiumGold: Color,
    val onPremiumGold: Color,
    val premiumGoldContainer: Color,
    val onPremiumGoldContainer: Color,
    val premiumAmber: Color,
    val onPremiumAmber: Color,
    val premiumAmberContainer: Color,
    val onPremiumAmberContainer: Color,
)

val LocalExtendedColors =
    staticCompositionLocalOf {
        ExtendedColors(
            premiumGold = Color(0xFFFFD700),
            onPremiumGold = Color(0xFF000000),
            premiumGoldContainer = Color(0xFFFFD700).copy(alpha = 0.2f),
            onPremiumGoldContainer = Color(0xFF000000),
            premiumAmber = Color(0xFFFFA000),
            onPremiumAmber = Color(0xFF000000),
            premiumAmberContainer = Color(0xFFFFA000).copy(alpha = 0.2f),
            onPremiumAmberContainer = Color(0xFF000000),
        )
    }

@Composable
fun ProvideExtendedColors(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val premiumGold = Color(0xFFFFD700)
    val onPremiumGold = Color(0xFF000000) // gold is bright; black text provides best contrast generally
    val premiumGoldContainer = premiumGold.copy(alpha = if (darkTheme) 0.3f else 0.2f)
    val onPremiumGoldContainer = onPremiumGold

    val premiumAmber = Color(0xFFFFA000)
    val onPremiumAmber = Color(0xFF000000)
    val premiumAmberContainer = premiumAmber.copy(alpha = if (darkTheme) 0.3f else 0.2f)
    val onPremiumAmberContainer = onPremiumAmber

    val ext =
        ExtendedColors(
            premiumGold = premiumGold,
            onPremiumGold = onPremiumGold,
            premiumGoldContainer = premiumGoldContainer,
            onPremiumGoldContainer = onPremiumGoldContainer,
            premiumAmber = premiumAmber,
            onPremiumAmber = onPremiumAmber,
            premiumAmberContainer = premiumAmberContainer,
            onPremiumAmberContainer = onPremiumAmberContainer,
        )

    CompositionLocalProvider(LocalExtendedColors provides ext) {
        content()
    }
}

val MaterialTheme.extendedColors: ExtendedColors
    @Composable
    @ReadOnlyComposable
    get() = LocalExtendedColors.current
