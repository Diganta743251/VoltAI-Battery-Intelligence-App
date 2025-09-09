package com.voltai.smartbatteryguardian.ui.home

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Represents different types of cards that can be displayed on the home screen
 */
@Immutable
enum class CardType(
    val id: String,
    val title: String,
    val description: String,
    val icon: ImageVector,
    val category: CardCategory,
    val defaultSize: CardSize = CardSize.Small,
    val allowedSizes: Set<CardSize> = setOf(CardSize.Small, CardSize.Medium),
    val isPremium: Boolean = false
) {
    BATTERY_LEVEL(
        "battery_level",
        "Battery Level",
        "Main battery percentage and status",
        Icons.Default.Phone,
        CardCategory.BATTERY,
        CardSize.Large,
        setOf(CardSize.Medium, CardSize.Large)
    ),

    CHARGING_STATUS(
        "charging_status",
        "Charging Status",
        "Current charging information",
        Icons.Default.Favorite,
        CardCategory.BATTERY,
        CardSize.Small
    ),

    TEMPERATURE(
        "temperature",
        "Temperature",
        "Battery temperature monitoring",
        Icons.Default.Star,
        CardCategory.BATTERY,
        CardSize.Small
    ),

    HEALTH(
        "health",
        "Battery Health",
        "Long-term battery condition",
        Icons.Default.Favorite,
        CardCategory.BATTERY,
        CardSize.Small
    ),

    CURRENT_VOLTAGE(
        "current_voltage",
        "Current & Voltage",
        "Real-time electrical readings",
        Icons.Default.PlayArrow,
        CardCategory.TECHNICAL,
        CardSize.Small
    ),

    SCREEN_TIME(
        "screen_time",
        "Screen Time",
        "Daily usage statistics",
        Icons.Default.Info,
        CardCategory.USAGE,
        CardSize.Small
    ),

    APP_USAGE(
        "app_usage",
        "Top Apps",
        "Battery consuming applications",
        Icons.Default.List,
        CardCategory.USAGE,
        CardSize.Medium,
        setOf(CardSize.Small, CardSize.Medium)
    ),

    CHARGING_HISTORY(
        "charging_history",
        "Charging History",
        "Historical charging patterns",
        Icons.Default.Build,
        CardCategory.ANALYTICS,
        CardSize.Medium,
        setOf(CardSize.Small, CardSize.Medium)
    ),

    AI_INSIGHTS(
        "ai_insights",
        "AI Insights",
        "Smart battery recommendations",
        Icons.Default.Star,
        CardCategory.AI,
        CardSize.Medium,
        setOf(CardSize.Medium, CardSize.Large),
        true
    ),

    POWER_MODES(
        "power_modes",
        "Power Modes",
        "Battery saving profiles",
        Icons.Default.Settings,
        CardCategory.CONTROLS,
        CardSize.Small
    ),

    QUICK_ACTIONS(
        "quick_actions",
        "Quick Actions",
        "Fast battery controls",
        Icons.Default.PlayArrow,
        CardCategory.CONTROLS,
        CardSize.Medium,
        setOf(CardSize.Small, CardSize.Medium)
    ),

    PERFORMANCE_CHART(
        "performance_chart",
        "Performance Chart",
        "Battery performance over time",
        Icons.Default.Info,
        CardCategory.ANALYTICS,
        CardSize.Large,
        setOf(CardSize.Medium, CardSize.Large),
        true
    ),

    THERMAL_STATUS(
        "thermal_status",
        "Thermal Status",
        "Device temperature monitoring",
        Icons.Default.Star,
        CardCategory.TECHNICAL,
        CardSize.Small,
        isPremium = true
    ),

    BATTERY_TIPS(
        "battery_tips",
        "Battery Tips",
        "Personalized optimization tips",
        Icons.Default.Info,
        CardCategory.AI,
        CardSize.Medium,
        setOf(CardSize.Medium, CardSize.Large),
        true
    ),

    HISTORY(
        "history",
        "History",
        "Battery charging and usage history",
        Icons.Default.List,
        CardCategory.ANALYTICS,
        CardSize.Medium,
        setOf(CardSize.Small, CardSize.Medium)
    ),

    OVERLAYS(
        "overlays",
        "Overlays",
        "Screen overlay controls",
        Icons.Default.Settings,
        CardCategory.CONTROLS,
        CardSize.Small
    ),

    // Advanced Premium Cards
    CHARGING_ANALYTICS(
        "charging_analytics",
        "Charging Analytics",
        "Advanced charging insights and optimization",
        Icons.Default.PlayArrow,
        CardCategory.ANALYTICS,
        CardSize.Large,
        setOf(CardSize.Medium, CardSize.Large),
        true
    ),

    CHEMISTRY_LAB(
        "chemistry_lab",
        "Chemistry Lab",
        "Molecular-level battery analysis",
        Icons.Default.Star,
        CardCategory.TECHNICAL,
        CardSize.Large,
        setOf(CardSize.Medium, CardSize.Large),
        true
    ),

    SYSTEM_OPTIMIZATION(
        "system_optimization",
        "System Optimization",
        "AI-powered device performance tuning",
        Icons.Default.Build,
        CardCategory.AI,
        CardSize.Large,
        setOf(CardSize.Medium, CardSize.Large),
        true
    ),

    PERFORMANCE_BENCHMARKS(
        "performance_benchmarks",
        "Performance Benchmarks",
        "Comprehensive battery and system testing",
        Icons.Default.Settings,
        CardCategory.ANALYTICS,
        CardSize.Large,
        setOf(CardSize.Medium, CardSize.Large),
        true
    ),

    DATA_EXPORT_IMPORT(
        "data_export_import",
        "Data Export/Import",
        "Backup and restore your battery data",
        Icons.Default.Settings,
        CardCategory.CONTROLS,
        CardSize.Medium,
        setOf(CardSize.Small, CardSize.Medium),
        true
    ),

    // New Premium Advanced Analytics Cards
    BATTERY_HEALTH_ANALYTICS(
        "battery_health_analytics",
        "Health Analytics",
        "Advanced AI-powered battery health analysis",
        Icons.Default.Favorite,
        CardCategory.AI,
        CardSize.Large,
        setOf(CardSize.Medium, CardSize.Large),
        true
    ),

    SMART_CHARGING(
        "smart_charging",
        "Smart Charging",
        "Intelligent charging optimization & recommendations",
        Icons.Default.Star,
        CardCategory.AI,
        CardSize.Large,
        setOf(CardSize.Medium, CardSize.Large),
        true
    ),

    BATTERY_DIAGNOSTICS(
        "battery_diagnostics",
        "Battery Diagnostics",
        "Comprehensive diagnostic testing & analysis",
        Icons.Default.Build,
        CardCategory.TECHNICAL,
        CardSize.Large,
        setOf(CardSize.Medium, CardSize.Large),
        true
    ),

    REAL_TIME_MONITORING(
        "real_time_monitoring",
        "Real-time Monitor",
        "Live battery metrics with advanced tracking",
        Icons.Default.PlayArrow,
        CardCategory.ANALYTICS,
        CardSize.Large,
        setOf(CardSize.Medium, CardSize.Large),
        true
    ),

    BATTERY_OPTIMIZATION(
        "battery_optimization",
        "Usage Optimization",
        "AI-powered battery usage optimization suggestions",
        Icons.Default.Settings,
        CardCategory.AI,
        CardSize.Large,
        setOf(CardSize.Medium, CardSize.Large),
        true
    )
}

@Immutable
enum class CardCategory(val displayName: String, val color: androidx.compose.ui.graphics.Color) {
    BATTERY("Battery", androidx.compose.ui.graphics.Color(0xFF4CAF50)),
    USAGE("Usage", androidx.compose.ui.graphics.Color(0xFF2196F3)),
    TECHNICAL("Technical", androidx.compose.ui.graphics.Color(0xFFFF9800)),
    ANALYTICS("Analytics", androidx.compose.ui.graphics.Color(0xFF9C27B0)),
    AI("AI Powered", androidx.compose.ui.graphics.Color(0xFFE91E63)),
    CONTROLS("Controls", androidx.compose.ui.graphics.Color(0xFF607D8B))
}

@Immutable
enum class CardSize(val columns: Int, val rows: Int, val displayName: String) {
    Small(1, 1, "Small"),
    Medium(2, 1, "Medium"),
    Large(2, 2, "Large"),
    Wide(3, 1, "Wide") // For special layouts
}

/**
 * Represents a positioned card on the home screen grid
 */
@Immutable
data class HomeCard(
    val id: String = java.util.UUID.randomUUID().toString(),
    val cardType: CardType,
    val size: CardSize = cardType.defaultSize,
    val row: Int,
    val column: Int,
    val isVisible: Boolean = true,
    val customTitle: String? = null,
    val lastUpdated: Long = System.currentTimeMillis()
)

/**
 * Configuration for the entire home screen layout
 */
@Immutable
data class HomeScreenConfig(
    val cards: List<HomeCard> = getDefaultHomeCards(),
    val gridColumns: Int = 2,
    val cardSpacing: Float = 12f,
    val enableAnimations: Boolean = true,
    val enableHaptics: Boolean = true,
    val theme: HomeTheme = HomeTheme.AUTO,
    val version: Int = 1
)

@Immutable
enum class HomeTheme {
    AUTO, LIGHT, DARK, DYNAMIC
}

/**
 * UI State for the home screen
 */
@Immutable
data class HomeScreenUiState(
    val config: HomeScreenConfig = HomeScreenConfig(),
    val isEditMode: Boolean = false,
    val isLoading: Boolean = false,
    val showAddCardSheet: Boolean = false,
    val selectedCard: HomeCard? = null,
    val isDragActive: Boolean = false,
    val errorMessage: String? = null
)

/**
 * Events that can be triggered on the home screen
 */
sealed class HomeScreenEvent {
    object EnterEditMode : HomeScreenEvent()
    object ExitEditMode : HomeScreenEvent()
    object ShowAddCardSheet : HomeScreenEvent()
    object HideAddCardSheet : HomeScreenEvent()

    data class AddCard(val cardType: CardType, val position: Pair<Int, Int>) : HomeScreenEvent()
    data class RemoveCard(val cardId: String) : HomeScreenEvent()
    data class MoveCard(val cardId: String, val newPosition: Pair<Int, Int>) : HomeScreenEvent()
    data class ResizeCard(val cardId: String, val newSize: CardSize) : HomeScreenEvent()
    data class SelectCard(val card: HomeCard?) : HomeScreenEvent()

    data class UpdateConfig(val config: HomeScreenConfig) : HomeScreenEvent()
    data class ResetToDefaults(val keepCustomizations: Boolean = false) : HomeScreenEvent()

    object SaveConfig : HomeScreenEvent()
    object LoadConfig : HomeScreenEvent()
}

/**
 * Default card layout for new users
 */
fun getDefaultHomeCards(): List<HomeCard> {
    return listOf(
        HomeCard(cardType = CardType.BATTERY_LEVEL, size = CardSize.Large, row = 0, column = 0),
        HomeCard(cardType = CardType.CHARGING_STATUS, size = CardSize.Small, row = 0, column = 2),
        HomeCard(cardType = CardType.TEMPERATURE, size = CardSize.Small, row = 1, column = 0),
        HomeCard(cardType = CardType.HEALTH, size = CardSize.Small, row = 1, column = 1),
        HomeCard(cardType = CardType.CURRENT_VOLTAGE, size = CardSize.Small, row = 1, column = 2),
        HomeCard(cardType = CardType.SCREEN_TIME, size = CardSize.Medium, row = 2, column = 0),
        HomeCard(cardType = CardType.QUICK_ACTIONS, size = CardSize.Medium, row = 2, column = 2),
        HomeCard(cardType = CardType.AI_INSIGHTS, size = CardSize.Medium, row = 3, column = 0),
        HomeCard(cardType = CardType.POWER_MODES, size = CardSize.Small, row = 3, column = 2),
        HomeCard(cardType = CardType.HISTORY, size = CardSize.Medium, row = 4, column = 0),
        HomeCard(cardType = CardType.OVERLAYS, size = CardSize.Small, row = 4, column = 2)
    )
}

/**
 * Get available card types by category
 */
fun getCardsByCategory(): Map<CardCategory, List<CardType>> {
    return CardType.values().groupBy { it.category }
}

/**
 * Check if a position is valid for a card size in the grid
 */
fun isValidPosition(
    cards: List<HomeCard>,
    cardSize: CardSize,
    row: Int,
    column: Int,
    gridColumns: Int,
    excludeCardId: String? = null
): Boolean {
    // Check if card fits within grid bounds
    if (column + cardSize.columns > gridColumns) return false

    // Check for overlaps with existing cards
    val occupiedPositions = cards
        .filter { it.isVisible && it.id != excludeCardId }
        .flatMap { card ->
            (card.row until card.row + card.size.rows).flatMap { r ->
                (card.column until card.column + card.size.columns).map { c ->
                    Pair(r, c)
                }
            }
        }.toSet()

    val newCardPositions = (row until row + cardSize.rows).flatMap { r ->
        (column until column + cardSize.columns).map { c ->
            Pair(r, c)
        }
    }.toSet()

    return newCardPositions.intersect(occupiedPositions).isEmpty()
}

/**
 * Find the next available position for a card
 */
fun findNextAvailablePosition(
    cards: List<HomeCard>,
    cardSize: CardSize,
    gridColumns: Int
): Pair<Int, Int>? {
    var row = 0
    while (row < 20) { // Reasonable limit
        for (column in 0 until gridColumns) {
            if (isValidPosition(cards, cardSize, row, column, gridColumns)) {
                return Pair(row, column)
            }
        }
        row++
    }
    return null
}
