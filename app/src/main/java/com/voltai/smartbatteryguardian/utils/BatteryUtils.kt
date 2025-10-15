package com.voltai.smartbatteryguardian.utils

import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Utility functions for battery health calculations and status
 */

fun getHealthStatus(health: Int): String {
    return when {
        health >= 90 -> "Excellent"
        health >= 80 -> "Very Good"
        health >= 70 -> "Good"
        health >= 60 -> "Fair"
        health >= 50 -> "Poor"
        else -> "Critical"
    }
}

fun getHealthColor(health: Int): Color {
    return when {
        health >= 85 -> Color(0xFF4CAF50)
        health >= 70 -> Color(0xFFFF9800)
        health >= 50 -> Color(0xFFFF5722)
        else -> Color(0xFFF44336)
    }
}

@Composable
fun getHealthCardColor(health: Int): CardColors {
    val containerColor =
        when {
            health >= 85 -> Color(0xFF4CAF50).copy(alpha = 0.1f)
            health >= 70 -> Color(0xFFFF9800).copy(alpha = 0.1f)
            health >= 50 -> Color(0xFFFF5722).copy(alpha = 0.1f)
            else -> Color(0xFFF44336).copy(alpha = 0.1f)
        }

    return CardDefaults.cardColors(
        containerColor = containerColor,
        contentColor = MaterialTheme.colorScheme.onSurface,
    )
}

fun getHealthDescription(health: Int): String {
    return when {
        health >= 85 -> "Your battery is in excellent condition! Continue your current charging habits to maintain optimal health."
        health >= 70 -> "Good battery health. Consider avoiding overnight charging and extreme temperatures to prevent further degradation."
        health >= 50 -> "Battery health is declining. Avoid deep discharges and consider replacing the battery within 6-12 months."
        else -> "Critical battery health. Immediate battery replacement recommended for optimal device performance."
    }
}

fun getBatteryColor(batteryLevel: Int): Color {
    return when {
        batteryLevel >= 80 -> Color(0xFF4CAF50)
        batteryLevel >= 50 -> Color(0xFFFF9800)
        batteryLevel >= 20 -> Color(0xFFFF5722)
        else -> Color(0xFFF44336)
    }
}

fun getTemperatureColor(temperature: Float): Color {
    return when {
        temperature < 30f -> Color(0xFF4CAF50)
        temperature < 35f -> Color(0xFF8BC34A)
        temperature < 40f -> Color(0xFFFF9800)
        else -> Color(0xFFF44336)
    }
}
