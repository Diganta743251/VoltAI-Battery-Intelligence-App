package com.voltai.smartbatteryguardian.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color

@Stable
class PremiumColors(
    val success: Color,
    val onSuccess: Color,
    val warning: Color,
    val onWarning: Color,
    val info: Color,
    val onInfo: Color,
    val premium: Color,
    val onPremium: Color,
    val batteryGreen: Color,
    val batteryYellow: Color,
    val batteryRed: Color,
)

val LocalPremiumColors =
    compositionLocalOf<PremiumColors> {
        error("LocalPremiumColors not provided. Wrap your UI in VoltAITheme/ProvidePremiumColors.")
    }

@Composable
fun ProvidePremiumColors(
    colors: PremiumColors,
    content: @Composable () -> Unit,
) {
    androidx.compose.runtime.CompositionLocalProvider(LocalPremiumColors provides colors) {
        content()
    }
}

@Composable
fun rememberPremiumColorsFromExtended(): PremiumColors {
    // Map premium brand to ExtendedColors, keep existing non-gold semantic colors
    val premium = MaterialTheme.extendedColors.premiumGold
    val onPremium = MaterialTheme.extendedColors.onPremiumGold
    return remember(premium, onPremium) {
        PremiumColors(
            success = Color(0xFF10B981),
            onSuccess = Color.White,
            warning = Color(0xFFF59E0B),
            onWarning = Color.White,
            info = Color(0xFF3B82F6),
            onInfo = Color.White,
            premium = premium,
            onPremium = onPremium,
            batteryGreen = Color(0xFF22C55E),
            batteryYellow = Color(0xFFFBBF24),
            batteryRed = Color(0xFFEF4444),
        )
    }
}
