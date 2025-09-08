package com.voltai.smartbatteryguardian.ui

import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*

// Enum for different card types
enum class CardType(
    val id: String,
    val title: String,
    val description: String,
    val icon: ImageVector,
    val isResizable: Boolean = false,
    val minWidth: Int = 1,
    val maxWidth: Int = 2,
    val defaultHeight: Int = 1
) {
    BATTERY_INFO("battery_info", "Battery Level", "Main battery percentage and status", Icons.Default.Phone, true, 2, 2, 1),
    ELECTRIC_CURRENT("electric_current", "Electric Current", "Current and voltage readings", Icons.Default.Star, false, 1, 1, 1),
    ONGOING("ongoing", "Ongoing Usage", "Screen time and battery usage", Icons.Default.PlayArrow, false, 1, 1, 1),
    TEMPERATURE("temperature", "Temperature", "Battery temperature monitoring", Icons.Default.AccountCircle, false, 1, 1, 1),
    HEALTH("health", "Battery Health", "Battery health percentage", Icons.Default.Favorite, false, 1, 1, 1),
    AVERAGE_USAGE("average_usage", "Average Usage", "Screen on/off usage patterns", Icons.Default.Info, false, 1, 1, 1),
    CHARGE_ESTIMATION("charge_estimation", "Charge Time", "Full charge time estimation", Icons.Default.Add, false, 1, 1, 1),
    HISTORY("history", "History", "Battery usage history", Icons.Default.List, true, 2, 2, 2),
    TOOLS("tools", "Tools", "Battery management tools", Icons.Default.Build, true, 2, 2, 1),
    AI_INSIGHTS("ai_insights", "AI Insights", "AI-powered battery optimization", Icons.Default.Star, true, 1, 2, 1),
    QUICK_ACTIONS("quick_actions", "Quick Actions", "Fast battery controls", Icons.Default.PlayArrow, false, 1, 2, 1),
    POWER_MODES("power_modes", "Power Modes", "Battery saving modes", Icons.Default.Settings, false, 1, 2, 1)
}

// Data class for positioned cards
data class PositionedCard(
    val cardType: CardType,
    val row: Int,
    val column: Int,
    val width: Int = cardType.minWidth,
    val height: Int = cardType.defaultHeight,
    val isVisible: Boolean = true
)

// Home page configuration
data class HomePageConfig(
    val cards: List<PositionedCard> = getDefaultCards(),
    val gridColumns: Int = 2,
    val isEditMode: Boolean = false
)

fun getDefaultCards(): List<PositionedCard> {
    return listOf(
        PositionedCard(CardType.BATTERY_INFO, 0, 0, 2, 1),
        PositionedCard(CardType.ELECTRIC_CURRENT, 1, 0, 1, 1),
        PositionedCard(CardType.ONGOING, 1, 1, 1, 1),
        PositionedCard(CardType.TEMPERATURE, 2, 0, 1, 1),
        PositionedCard(CardType.HEALTH, 2, 1, 1, 1),
        PositionedCard(CardType.AVERAGE_USAGE, 3, 0, 1, 1),
        PositionedCard(CardType.CHARGE_ESTIMATION, 3, 1, 1, 1),
        PositionedCard(CardType.HISTORY, 4, 0, 2, 2),
        PositionedCard(CardType.TOOLS, 6, 0, 2, 1)
    )
}

fun getAllAvailableCards(): List<CardType> {
    return CardType.values().toList()
}
