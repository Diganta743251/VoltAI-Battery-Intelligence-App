package com.voltai.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Monitoring alert dialog component in the :ui module that warns the user
 * when battery temperature exceeds safe operating thresholds identified by domain logic.
 */
@Composable
fun ThermalWarningAlertDialog(
    currentTemperature: Float,
    safeThreshold: Float = 38.0f,
    onDismiss: () -> Unit,
    onOptimizeCooling: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (currentTemperature >= safeThreshold) {
        val isCritical = currentTemperature >= 42.0f
        val title = if (isCritical) "⚠️ Critical Battery Temperature!" else "⚡ High Temperature Warning"
        val message = "Battery temperature has reached ${String.format("%.1f", currentTemperature)}°C (Safe limit: ${safeThreshold}°C). ${
            if (isCritical) "Stop charging immediately and move device to a cool location to prevent permanent cell degradation." else "Consider closing heavy background applications or pausing fast charging."
        }"

        AlertDialog(
            onDismissRequest = onDismiss,
            icon = {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Thermal Warning",
                    tint = if (isCritical) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
            },
            text = {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            },
            confirmButton = {
                Button(
                    onClick = onOptimizeCooling,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isCritical) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                    ),
                    modifier = Modifier.testTag("thermal_alert_confirm_button"),
                ) {
                    Text("Optimize & Cool Down")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("thermal_alert_dismiss_button"),
                ) {
                    Text("Dismiss")
                }
            },
            modifier = modifier.testTag("thermal_warning_dialog"),
        )
    }
}
