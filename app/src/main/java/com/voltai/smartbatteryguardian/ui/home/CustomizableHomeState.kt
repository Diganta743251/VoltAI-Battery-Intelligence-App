package com.voltai.smartbatteryguardian.ui.home

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Represents different types of cards that can be displayed on the home screen.
 *
 * Each card type defines:
 * @property id Unique identifier for the card type
 * @property title Display title shown to users
 * @property description Brief description of the card's purpose
 * @property icon Material icon representing the card
 * @property category Category grouping for organization
 * @property defaultSize Default size when the card is first added
 * @property allowedSizes Set of sizes the card can be resized to
 * @property isPremium Whether this card requires premium subscription
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
    val isPremium: Boolean = false,
) {
    BATTERY_LEVEL(
        "battery_level",
        "Battery Level",
        "Main battery percentage and status",
        Icons.Default.Phone,
        CardCategory.BATTERY,
        CardSize.FullWidthShort,
        setOf(CardSize.Medium, CardSize.MediumTall, CardSize.Large, CardSize.FullWidth, CardSize.FullWidthShort),
    ),

    CHARGING_STATUS(
        "charging_status",
        "Charging Status",
        "Current charging information",
        Icons.Default.Favorite,
        CardCategory.BATTERY,
        CardSize.Medium,
        setOf(CardSize.Medium, CardSize.Large),
    ),

    TEMPERATURE(
        "temperature",
        "Temperature",
        "Battery temperature monitoring",
        Icons.Default.Star,
        CardCategory.BATTERY,
        CardSize.Medium,
        setOf(CardSize.Medium, CardSize.Large),
    ),

    HEALTH(
        "health",
        "Battery Health",
        "Long-term battery condition",
        Icons.Default.Favorite,
        CardCategory.BATTERY,
        CardSize.Medium,
        setOf(CardSize.Medium, CardSize.Large),
    ),

    CURRENT_VOLTAGE(
        "current_voltage",
        "Current & Voltage",
        "Real-time electrical readings",
        Icons.Default.PlayArrow,
        CardCategory.TECHNICAL,
        CardSize.Medium,
        setOf(CardSize.Medium, CardSize.Large),
    ),

    SCREEN_TIME(
        "screen_time",
        "Screen Time",
        "Daily usage statistics",
        Icons.Default.Info,
        CardCategory.USAGE,
        CardSize.Medium,
        setOf(CardSize.Medium, CardSize.Large),
    ),

    APP_USAGE(
        "app_usage",
        "Top Apps",
        "Battery consuming applications",
        Icons.Default.List,
        CardCategory.USAGE,
        CardSize.Medium,
        setOf(CardSize.Small, CardSize.Medium, CardSize.Large),
    ),

    CHARGING_HISTORY(
        "charging_history",
        "Charging History",
        "Historical charging patterns",
        Icons.Default.Build,
        CardCategory.ANALYTICS,
        CardSize.Medium,
        setOf(CardSize.Medium, CardSize.Large),
    ),

    AI_INSIGHTS(
        "ai_insights",
        "AI Insights",
        "Smart battery recommendations",
        Icons.Default.Star,
        CardCategory.AI,
        CardSize.Medium,
        setOf(CardSize.Medium, CardSize.Large),
        true,
    ),

    POWER_MODES(
        "power_modes",
        "Power Modes",
        "Battery saving profiles",
        Icons.Default.Settings,
        CardCategory.CONTROLS,
        CardSize.Small,
    ),

    QUICK_ACTIONS(
        "quick_actions",
        "Quick Actions",
        "Fast battery controls",
        Icons.Default.PlayArrow,
        CardCategory.CONTROLS,
        CardSize.Medium,
        setOf(CardSize.Small, CardSize.Medium),
    ),

    PERFORMANCE_CHART(
        "performance_chart",
        "Performance Chart",
        "Battery performance over time",
        Icons.Default.Info,
        CardCategory.ANALYTICS,
        CardSize.Large,
        setOf(CardSize.Medium, CardSize.Large),
        true,
    ),

    THERMAL_STATUS(
        "thermal_status",
        "Thermal Status",
        "Device temperature monitoring",
        Icons.Default.Star,
        CardCategory.TECHNICAL,
        CardSize.Small,
        isPremium = true,
    ),

    BATTERY_TIPS(
        "battery_tips",
        "Battery Tips",
        "Personalized optimization tips",
        Icons.Default.Info,
        CardCategory.AI,
        CardSize.Medium,
        setOf(CardSize.Medium, CardSize.Large),
        true,
    ),

    HISTORY(
        "history",
        "History",
        "Battery charging and usage history",
        Icons.Default.List,
        CardCategory.ANALYTICS,
        CardSize.Large,
        setOf(CardSize.Medium, CardSize.Large),
    ),

    OVERLAYS(
        "overlays",
        "Overlays",
        "Screen overlay controls",
        Icons.Default.Settings,
        CardCategory.CONTROLS,
        CardSize.Small,
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
        true,
    ),

    CHEMISTRY_LAB(
        "chemistry_lab",
        "Chemistry Lab",
        "Molecular-level battery analysis",
        Icons.Default.Star,
        CardCategory.TECHNICAL,
        CardSize.Large,
        setOf(CardSize.Medium, CardSize.Large),
        true,
    ),

    SYSTEM_OPTIMIZATION(
        "system_optimization",
        "System Optimization",
        "AI-powered device performance tuning",
        Icons.Default.Build,
        CardCategory.AI,
        CardSize.Large,
        setOf(CardSize.Medium, CardSize.Large),
        true,
    ),

    PERFORMANCE_BENCHMARKS(
        "performance_benchmarks",
        "Performance Benchmarks",
        "Comprehensive battery and system testing",
        Icons.Default.Settings,
        CardCategory.ANALYTICS,
        CardSize.Large,
        setOf(CardSize.Medium, CardSize.Large),
        true,
    ),

    DATA_EXPORT_IMPORT(
        "data_export_import",
        "Data Export/Import",
        "Backup and restore your battery data",
        Icons.Default.Settings,
        CardCategory.CONTROLS,
        CardSize.Medium,
        setOf(CardSize.Small, CardSize.Medium),
        true,
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
        true,
    ),

    SMART_CHARGING(
        "smart_charging",
        "Smart Charging",
        "Intelligent charging optimization & recommendations",
        Icons.Default.Star,
        CardCategory.AI,
        CardSize.Large,
        setOf(CardSize.Medium, CardSize.Large),
        true,
    ),

    BATTERY_DIAGNOSTICS(
        "battery_diagnostics",
        "Battery Diagnostics",
        "Comprehensive diagnostic testing & analysis",
        Icons.Default.Build,
        CardCategory.TECHNICAL,
        CardSize.Large,
        setOf(CardSize.Medium, CardSize.Large),
        true,
    ),

    REAL_TIME_MONITORING(
        "real_time_monitoring",
        "Real-time Monitor",
        "Live battery metrics with advanced tracking",
        Icons.Default.PlayArrow,
        CardCategory.ANALYTICS,
        CardSize.Large,
        setOf(CardSize.Medium, CardSize.Large),
        true,
    ),

    BATTERY_OPTIMIZATION(
        "battery_optimization",
        "Usage Optimization",
        "AI-powered battery usage optimization suggestions",
        Icons.Default.Settings,
        CardCategory.AI,
        CardSize.Large,
        setOf(CardSize.Medium, CardSize.Large),
        true,
    ),
}

/**
 * Categories for organizing card types.
 * Each category has a display name and associated color for visual grouping.
 */
@Immutable
enum class CardCategory(val displayName: String, val color: androidx.compose.ui.graphics.Color) {
    BATTERY("Battery", androidx.compose.ui.graphics.Color(0xFF4CAF50)),
    USAGE("Usage", androidx.compose.ui.graphics.Color(0xFF2196F3)),
    TECHNICAL("Technical", androidx.compose.ui.graphics.Color(0xFFFF9800)),
    ANALYTICS("Analytics", androidx.compose.ui.graphics.Color(0xFF9C27B0)),
    AI("AI Powered", androidx.compose.ui.graphics.Color(0xFFE91E63)),
    CONTROLS("Controls", androidx.compose.ui.graphics.Color(0xFF607D8B)),
}

/**
 * Defines the size of a card in grid units.
 *
 * @property columns Number of grid columns the card spans (1-4)
 * @property rows Number of grid rows the card spans
 * @property displayName Human-readable size name
 * @property heightMultiplier Multiplier for calculating actual height (defaults to rows)
 */
@Immutable
enum class CardSize(
    val columns: Int,
    val rows: Int,
    val displayName: String,
    val heightMultiplier: Float = rows.toFloat(),
) {
    Small(1, 2, "Small"),
    Medium(2, 2, "Medium"),
    MediumTall(2, 3, "Medium Tall"), // 3 rows = 270dp (1.5x of 180dp)
    Large(2, 4, "Large"),
    FullWidth(4, 4, "Full"),
    FullWidthShort(4, 3, "Full Width Short"), // Full width but 3 rows = 270dp
    Wide(3, 2, "Wide"), // For special layouts
    XLarge(3, 6, "3x3"),
    XXLarge(4, 8, "4x4"),
}

/**
 * Represents a positioned card on the home screen grid.
 *
 * @property id Unique identifier for this card instance
 * @property cardType Type of card defining its content and behavior
 * @property size Current size of the card
 * @property row Grid row position (0-based)
 * @property column Grid column position (0-based)
 * @property isVisible Whether the card is currently visible
 * @property customTitle Optional custom title overriding the default
 * @property lastUpdated Timestamp of last modification
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
    val lastUpdated: Long = System.currentTimeMillis(),
)

/**
 * Configuration for the entire home screen layout.
 *
 * @property cards List of cards displayed on the home screen
 * @property gridColumns Number of columns in the grid (1-4)
 * @property cardSpacing Spacing between cards in dp
 * @property enableAnimations Whether to show entrance/exit animations
 * @property enableHaptics Whether to provide haptic feedback
 * @property theme Visual theme for the home screen
 * @property version Configuration version for migration support
 */
@Immutable
data class HomeScreenConfig(
    val cards: List<HomeCard> = getDefaultHomeCards(),
    val gridColumns: Int = 4,
    val cardSpacing: Float = 12f,
    val enableAnimations: Boolean = true,
    val enableHaptics: Boolean = true,
    val theme: HomeTheme = HomeTheme.AUTO,
    val version: Int = 1,
)

@Immutable
enum class HomeTheme {
    AUTO,
    LIGHT,
    DARK,
    DYNAMIC,
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
    val errorMessage: String? = null,
    val canUndo: Boolean = false,
    val canRedo: Boolean = false,
    val undoMessage: String? = null,
    val successMessage: String? = null,
    val isPerformingBulkOperation: Boolean = false,
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

    // Toggle a card's visibility on the dashboard
    data class ToggleVisibility(val cardId: String) : HomeScreenEvent()

    data class UpdateConfig(val config: HomeScreenConfig) : HomeScreenEvent()

    data class ResetToDefaults(
        val keepCustomizations: Boolean = false,
        val keepPremiumCards: Boolean = false,
    ) : HomeScreenEvent()

    data class UpdateGridColumns(val columns: Int) : HomeScreenEvent()

    object SaveConfig : HomeScreenEvent()

    object LoadConfig : HomeScreenEvent()

    // Undo/Redo functionality
    object Undo : HomeScreenEvent()

    object Redo : HomeScreenEvent()

    // Dismiss messages
    object DismissError : HomeScreenEvent()

    object DismissSuccess : HomeScreenEvent()
}

/**
 * Default card layout for new users - Non-overlapping layout
 */
fun getDefaultHomeCards(): List<HomeCard> {
    // Carefully planned non-overlapping layout for 4-column grid:
    // Row 0-2: Battery Level (FullWidthShort 4x3) - FULL WIDTH, 3 rows
    // Row 3-4: Temperature (Medium 2x2) at (3,0-1)
    // Row 3-4: Health (Medium 2x2) at (3,2-3)
    // Row 5-8: History (Large 2x4) at (5,0-1)
    // Row 5-6: Current & Voltage (Medium 2x2) at (5,2-3)
    // Row 7-8: Screen Time (Medium 2x2) at (7,2-3)
    return listOf(
        HomeCard(cardType = CardType.BATTERY_LEVEL, row = 0, column = 0, size = CardSize.FullWidthShort),
        HomeCard(cardType = CardType.TEMPERATURE, row = 3, column = 0, size = CardSize.Medium),
        HomeCard(cardType = CardType.HEALTH, row = 3, column = 2, size = CardSize.Medium),
        HomeCard(cardType = CardType.HISTORY, row = 5, column = 0, size = CardSize.Large),
        HomeCard(cardType = CardType.CURRENT_VOLTAGE, row = 5, column = 2, size = CardSize.Medium),
        HomeCard(cardType = CardType.SCREEN_TIME, row = 7, column = 2, size = CardSize.Medium),
    )
}

/**
 * Cards that have dedicated detail screens in AppNavHost.
 */
fun getNavigableCardTypes(): Set<CardType> =
    setOf(
        CardType.BATTERY_LEVEL,
        CardType.CURRENT_VOLTAGE,
        CardType.TEMPERATURE,
        CardType.HEALTH,
        CardType.SCREEN_TIME,
        CardType.HISTORY,
        CardType.OVERLAYS,
        CardType.APP_USAGE,
    )

/**
 * Get available card types by category
 */
fun getCardsByCategory(): Map<CardCategory, List<CardType>> {
    return CardType.values().groupBy { it.category }
}

/**
 * Check if a position is valid for a card size in the grid.
 *
 * Validates that:
 * - The card fits within grid bounds
 * - The position doesn't overlap with existing visible cards
 *
 * @param cards Existing cards in the grid
 * @param cardSize Size of the card to place
 * @param row Target row position
 * @param column Target column position
 * @param gridColumns Total number of columns in the grid
 * @param excludeCardId Optional card ID to exclude from overlap checking (for moving cards)
 * @return true if the position is valid, false otherwise
 */
fun isValidPosition(
    cards: List<HomeCard>,
    cardSize: CardSize,
    row: Int,
    column: Int,
    gridColumns: Int,
    excludeCardId: String? = null,
): Boolean {
    // Check if card fits within grid bounds (strict boundary check)
    if (column < 0 || row < 0 || column + cardSize.columns > gridColumns) return false

    // Check for overlaps with existing cards
    val occupiedPositions =
        cards
            .filter { it.isVisible && it.id != excludeCardId }
            .flatMap { card ->
                (card.row until card.row + card.size.rows).flatMap { r ->
                    (card.column until card.column + card.size.columns).map { c ->
                        Pair(r, c)
                    }
                }
            }.toSet()

    val newCardPositions =
        (row until row + cardSize.rows).flatMap { r ->
            (column until column + cardSize.columns).map { c ->
                Pair(r, c)
            }
        }.toSet()

    return newCardPositions.intersect(occupiedPositions).isEmpty()
}

/**
 * Find the next available position for a card using top-to-bottom, left-to-right scanning.
 *
 * @param cards Existing cards in the grid
 * @param cardSize Size of the card to place
 * @param gridColumns Total number of columns in the grid
 * @return Pair of (row, column) if found, null if no valid position exists
 */
fun findNextAvailablePosition(
    cards: List<HomeCard>,
    cardSize: CardSize,
    gridColumns: Int,
): Pair<Int, Int>? {
    var row = 0
    while (row < 120) { // Expanded limit to support long, scrollable home screens
        for (column in 0 until gridColumns) {
            if (isValidPosition(cards, cardSize, row, column, gridColumns)) {
                return Pair(row, column)
            }
        }
        row++
    }
    return null
}
