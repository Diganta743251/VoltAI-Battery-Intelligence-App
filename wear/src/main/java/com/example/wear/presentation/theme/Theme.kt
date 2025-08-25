package com.voltai.wear.presentation.theme

import androidx.compose.runtime.Composable
import androidx.wear.compose.material.MaterialTheme

@Composable
fun VoltAISmartBatteryGuardianTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colors = wearColorPalette,
        typography = Typography,
        // For custom code not covered by the Material Design theme, please provide it here
        content = content
    )
}
