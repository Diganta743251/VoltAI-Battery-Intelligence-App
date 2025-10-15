package com.voltai.smartbatteryguardian.ui.home

import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CustomizableHomeViewModel
@Inject
constructor(
    private val homeScreenRepository: com.voltai.smartbatteryguardian.data.HomeScreenRepository,
    private val premiumManager: com.voltai.smartbatteryguardian.premium.PremiumManager,
) : ViewModel() {
    val isPremium: StateFlow<Boolean> = premiumManager.premiumState

    private val _uiState = MutableStateFlow(HomeScreenUiState())
    val uiState: StateFlow<HomeScreenUiState> = _uiState.asStateFlow()

    private val _effects = MutableSharedFlow<HomeScreenEffect>()
    val effects: SharedFlow<HomeScreenEffect> = _effects.asSharedFlow()

    // Undo/Redo stack management (max 20 operations)
    private val undoStack = mutableListOf<HomeScreenConfig>()
    private val redoStack = mutableListOf<HomeScreenConfig>()
    private val maxHistorySize = 20

    init {
        loadConfiguration()
    }

    // Handler methods (distinct names to avoid potential symbol resolution issues)
    private fun handleUpdateConfig(config: HomeScreenConfig) {
        _uiState.value = _uiState.value.copy(config = config)
        emitEffect(HomeScreenEffect.ConfigurationSaved)
        saveConfiguration()
    }

    private fun handleResetToDefaults(
        keepCustomizations: Boolean,
        keepPremiumCards: Boolean,
    ) {
        val defaults = getDefaultHomeCards().filter { keepPremiumCards || !it.cardType.isPremium }
        val current = _uiState.value.config
        val newConfig =
            if (keepCustomizations) {
                current.copy(
                    cards = defaults,
                )
            } else {
                HomeScreenConfig(cards = defaults)
            }
        _uiState.value = _uiState.value.copy(config = newConfig, selectedCard = null, isEditMode = false)
        emitEffect(HomeScreenEffect.ConfigurationReset)
        saveConfiguration()
    }

    private fun handleUpdateGridColumns(columns: Int) {
        val clamped = columns.coerceIn(1, 4)
        val current = _uiState.value.config
        if (current.gridColumns == clamped) return
        val updated = current.copy(gridColumns = clamped)
        _uiState.value = _uiState.value.copy(config = updated)
        saveConfiguration()
    }

    private fun toggleVisibility(cardId: String) {
        val current = _uiState.value.config
        val updatedCards =
            current.cards.map { card ->
                if (card.id == cardId) card.copy(isVisible = !card.isVisible) else card
            }
        val updatedConfig = current.copy(cards = updatedCards)
        _uiState.value = _uiState.value.copy(config = updatedConfig)

        // Persist visibility change
        viewModelScope.launch {
            try {
                val isVisibleNow = updatedCards.firstOrNull { it.id == cardId }?.isVisible ?: true
                homeScreenRepository.saveCardState(cardId, isVisibleNow)
                emitEffect(HomeScreenEffect.PerformHapticFeedback(HapticType.Click))
            } catch (e: Exception) {
                emitEffect(HomeScreenEffect.ShowError("Failed to update visibility"))
            }
        }
    }

    fun onEvent(event: HomeScreenEvent) {
        try {
            when (event) {
                is HomeScreenEvent.EnterEditMode -> enterEditMode()
                is HomeScreenEvent.ExitEditMode -> exitEditMode()
                is HomeScreenEvent.ShowAddCardSheet -> showAddCardSheet()
                is HomeScreenEvent.HideAddCardSheet -> hideAddCardSheet()
                is HomeScreenEvent.AddCard -> addCard(event.cardType, event.position)
                is HomeScreenEvent.RemoveCard -> removeCard(event.cardId)
                is HomeScreenEvent.MoveCard -> moveCard(event.cardId, event.newPosition)
                is HomeScreenEvent.ResizeCard -> resizeCard(event.cardId, event.newSize)
                is HomeScreenEvent.SelectCard -> selectCard(event.card)
                is HomeScreenEvent.ToggleVisibility -> toggleVisibility(event.cardId)
                is HomeScreenEvent.UpdateConfig -> handleUpdateConfig(event.config)
                is HomeScreenEvent.ResetToDefaults ->
                    handleResetToDefaults(
                        event.keepCustomizations,
                        event.keepPremiumCards,
                    )
                is HomeScreenEvent.UpdateGridColumns -> handleUpdateGridColumns(event.columns)
                is HomeScreenEvent.SaveConfig -> saveConfiguration()
                is HomeScreenEvent.LoadConfig -> loadConfiguration()
                is HomeScreenEvent.Undo -> performUndo()
                is HomeScreenEvent.Redo -> performRedo()
                is HomeScreenEvent.DismissError -> dismissError()
                is HomeScreenEvent.DismissSuccess -> dismissSuccess()
            }
        } catch (e: Exception) {
            handleError("Failed to process customization event", e)
        }
    }

    private fun enterEditMode() {
        _uiState.value = _uiState.value.copy(isEditMode = true)
        emitEffect(HomeScreenEffect.ShowEditModeHint)
        emitEffect(HomeScreenEffect.PerformHapticFeedback(HapticType.LongPress))
    }

    private fun exitEditMode() {
        _uiState.value =
            _uiState.value.copy(
                isEditMode = false,
                selectedCard = null,
                isDragActive = false,
            )
        saveConfiguration()
        emitEffect(HomeScreenEffect.PerformHapticFeedback(HapticType.Success))
    }

    private fun showAddCardSheet() {
        _uiState.value = _uiState.value.copy(showAddCardSheet = true)
        emitEffect(HomeScreenEffect.PerformHapticFeedback(HapticType.Click))
    }

    private fun hideAddCardSheet() {
        _uiState.value = _uiState.value.copy(showAddCardSheet = false)
    }

    private fun updateConfig(config: HomeScreenConfig) {
        _uiState.value = _uiState.value.copy(config = config)
        emitEffect(HomeScreenEffect.ConfigurationSaved)
        saveConfiguration()
    }

    private fun resetToDefaults(
        keepCustomizations: Boolean,
        keepPremiumCards: Boolean,
    ) {
        val defaults = getDefaultHomeCards().filter { keepPremiumCards || !it.cardType.isPremium }
        val current = _uiState.value.config
        val newConfig =
            if (keepCustomizations) {
                current.copy(
                    cards = defaults,
                )
            } else {
                HomeScreenConfig(cards = defaults)
            }
        _uiState.value = _uiState.value.copy(config = newConfig, selectedCard = null, isEditMode = false)
        emitEffect(HomeScreenEffect.ConfigurationReset)
        saveConfiguration()
    }

    private fun updateGridColumns(columns: Int) {
        val clamped = columns.coerceIn(1, 4)
        val current = _uiState.value.config
        if (current.gridColumns == clamped) return
        val updated = current.copy(gridColumns = clamped)
        _uiState.value = _uiState.value.copy(config = updated)
        saveConfiguration()
    }

    private fun addCard(
        cardType: CardType,
        position: Pair<Int, Int>,
    ) {
        pushToUndoStack() // Save current state for undo

        val currentConfig = _uiState.value.config

        // Enhanced position finding with multiple fallback strategies
        val availablePosition =
            findBestAvailablePosition(
                currentConfig.cards,
                cardType.defaultSize,
                currentConfig.gridColumns,
                preferredPosition = position,
            )

        if (availablePosition == null) {
            emitEffect(HomeScreenEffect.ShowError("No space available for this card size"))
            emitEffect(HomeScreenEffect.PerformHapticFeedback(HapticType.Error))
            return
        }

        val newCard =
            HomeCard(
                cardType = cardType,
                size = cardType.defaultSize,
                row = availablePosition.first,
                column = availablePosition.second,
            )

        // Double-check position validity before adding
        if (!isValidCardPosition(currentConfig.cards, newCard, currentConfig.gridColumns)) {
            emitEffect(HomeScreenEffect.ShowError("Invalid card position detected"))
            emitEffect(HomeScreenEffect.PerformHapticFeedback(HapticType.Error))
            return
        }

        val updatedCards = currentConfig.cards + newCard
        val updatedConfig = currentConfig.copy(cards = updatedCards)

        _uiState.value =
            _uiState.value.copy(
                config = updatedConfig,
                showAddCardSheet = false,
                selectedCard = newCard, // Auto-select new card for immediate editing
                successMessage = "Card added successfully",
            )

        emitEffect(HomeScreenEffect.CardAdded(newCard.id))
        emitEffect(HomeScreenEffect.PerformHapticFeedback(HapticType.Success))

        // Auto-save after adding card
        saveConfiguration()
    }

    private fun removeCard(cardId: String) {
        pushToUndoStack() // Save current state for undo

        val currentConfig = _uiState.value.config
        val removedCard = currentConfig.cards.find { it.id == cardId }
        val updatedCards = currentConfig.cards.filter { it.id != cardId }
        val updatedConfig = currentConfig.copy(cards = updatedCards)

        _uiState.value =
            _uiState.value.copy(
                config = updatedConfig,
                selectedCard = null,
                successMessage = "${removedCard?.cardType?.title ?: "Card"} removed",
            )

        // Persist removal so view mode reflects immediately
        saveConfiguration()

        emitEffect(HomeScreenEffect.CardRemoved(cardId))
        emitEffect(HomeScreenEffect.PerformHapticFeedback(HapticType.LongPress))
    }

    fun moveCardWithDrag(
        cardId: String,
        dragOffset: Offset,
    ) {
        try {
            val currentConfig = _uiState.value.config
            val cards = currentConfig.cards
            val cardToMove = cards.find { it.id == cardId } ?: return

            // Enhanced thresholds for better UX
            val horizontalThreshold = 80f
            val verticalThreshold = 120f

            // Calculate new grid position based on drag direction
            val newRow: Int
            val newColumn: Int

            when {
                dragOffset.x > horizontalThreshold -> {
                    // Move right in grid
                    newRow = cardToMove.row
                    newColumn = minOf(cardToMove.column + 1, currentConfig.gridColumns - cardToMove.size.columns)
                }
                dragOffset.x < -horizontalThreshold -> {
                    // Move left in grid
                    newRow = cardToMove.row
                    newColumn = maxOf(cardToMove.column - 1, 0)
                }
                dragOffset.y > verticalThreshold -> {
                    // Move down in grid
                    newRow = cardToMove.row + 1
                    newColumn = cardToMove.column
                }
                dragOffset.y < -verticalThreshold -> {
                    // Move up in grid
                    newRow = maxOf(cardToMove.row - 1, 0)
                    newColumn = cardToMove.column
                }
                else -> {
                    // No significant movement
                    return
                }
            }

            // Use the same validation logic as moveCard
            if (isValidPosition(
                    cards,
                    cardToMove.size,
                    newRow,
                    newColumn,
                    currentConfig.gridColumns,
                    cardId,
                )
            ) {
                val updatedCards =
                    cards.map { card ->
                        if (card.id == cardId) {
                            card.copy(row = newRow, column = newColumn)
                        } else {
                            card
                        }
                    }

                val updatedConfig = currentConfig.copy(cards = updatedCards)
                _uiState.value = _uiState.value.copy(config = updatedConfig)

                emitEffect(HomeScreenEffect.CardMoved(cardId, Pair(newRow, newColumn)))
                emitEffect(HomeScreenEffect.PerformHapticFeedback(HapticType.Success))

                // Auto-save after successful move
                saveConfiguration()
            } else {
                // Position is invalid, provide feedback
                emitEffect(HomeScreenEffect.ShowError("Cannot move card to that position"))
                emitEffect(HomeScreenEffect.PerformHapticFeedback(HapticType.Error))
            }
        } catch (e: Exception) {
            emitEffect(HomeScreenEffect.ShowError("Failed to move card: ${e.message}"))
            emitEffect(HomeScreenEffect.PerformHapticFeedback(HapticType.Error))
        }
    }

    private fun moveCard(
        cardId: String,
        newPosition: Pair<Int, Int>,
    ) {
        pushToUndoStack() // Save current state for undo

        val currentConfig = _uiState.value.config
        val cardToMove = currentConfig.cards.find { it.id == cardId } ?: return

        // Check if the new position is valid
        if (!isValidPosition(
                currentConfig.cards,
                cardToMove.size,
                newPosition.first,
                newPosition.second,
                currentConfig.gridColumns,
                cardId,
            )
        ) {
            // Try auto-shift to make room like a launcher
            val shifted =
                autoShiftAndPlace(
                    cards = currentConfig.cards,
                    movingCardId = cardId,
                    targetRow = newPosition.first,
                    targetCol = newPosition.second,
                    gridColumns = currentConfig.gridColumns,
                )
            if (shifted == null) {
                emitEffect(HomeScreenEffect.ShowError("No room at that position"))
                emitEffect(HomeScreenEffect.PerformHapticFeedback(HapticType.Error))
                return
            } else {
                _uiState.value =
                    _uiState.value.copy(
                        config = currentConfig.copy(cards = shifted),
                        successMessage = "Card moved with auto-shift",
                    )
                emitEffect(HomeScreenEffect.CardMoved(cardId, newPosition))
                emitEffect(HomeScreenEffect.PerformHapticFeedback(HapticType.Success))
                saveConfiguration()
                return
            }
        }

        val updatedCards =
            currentConfig.cards.map { card ->
                if (card.id == cardId) {
                    card.copy(row = newPosition.first, column = newPosition.second)
                } else {
                    card
                }
            }

        val updatedConfig = currentConfig.copy(cards = updatedCards)
        _uiState.value =
            _uiState.value.copy(
                config = updatedConfig,
                successMessage = "Card moved successfully",
            )

        emitEffect(HomeScreenEffect.CardMoved(cardId, newPosition))
        emitEffect(HomeScreenEffect.PerformHapticFeedback(HapticType.Click))
        // Persist move immediately so viewing mode reflects the new placement
        saveConfiguration()
    }

    private fun resizeCard(
        cardId: String,
        newSize: CardSize,
    ) {
        try {
            pushToUndoStack() // Save current state for undo

            val currentConfig = _uiState.value.config
            val cardToResize = currentConfig.cards.find { it.id == cardId }

            if (cardToResize == null) {
                emitEffect(HomeScreenEffect.ShowError("Card not found"))
                emitEffect(HomeScreenEffect.PerformHapticFeedback(HapticType.Error))
                return
            }

            // Enforce: allowed sizes and minimum default dimensions
            val def = cardToResize.cardType.defaultSize
            val allowed = cardToResize.cardType.allowedSizes
            val normalized = if (allowed.contains(newSize)) newSize else def
            if (normalized.columns < def.columns || normalized.rows < def.rows) {
                emitEffect(HomeScreenEffect.ShowError("Cannot resize smaller than default"))
                emitEffect(HomeScreenEffect.PerformHapticFeedback(HapticType.Error))
                return
            }

            // Check if the card fits with the new size
            if (!isValidPosition(
                    currentConfig.cards,
                    normalized,
                    cardToResize.row,
                    cardToResize.column,
                    currentConfig.gridColumns,
                    cardId,
                )
            ) {
                emitEffect(HomeScreenEffect.ShowError("Not enough space to resize to ${normalized.displayName}"))
                emitEffect(HomeScreenEffect.PerformHapticFeedback(HapticType.Error))
                return
            }

            val updatedCards =
                currentConfig.cards.map { card ->
                    if (card.id == cardId) {
                        card.copy(size = normalized)
                    } else {
                        card
                    }
                }

            val updatedConfig = currentConfig.copy(cards = updatedCards)
            _uiState.value =
                _uiState.value.copy(
                    config = updatedConfig,
                    successMessage = "Card resized to ${normalized.displayName}",
                )

            emitEffect(HomeScreenEffect.CardResized(cardId, normalized))
            emitEffect(HomeScreenEffect.PerformHapticFeedback(HapticType.Success))

            // Auto-save after successful resize
            saveConfiguration()
        } catch (e: Exception) {
            handleError("Failed to resize card", e)
        }
    }

    /**
     * Attempt to make room by shifting overlapping cards to the next available spaces,
     * then place the moving card at the target. Returns updated cards or null if not possible.
     */
    private fun autoShiftAndPlace(
        cards: List<HomeCard>,
        movingCardId: String,
        targetRow: Int,
        targetCol: Int,
        gridColumns: Int,
    ): List<HomeCard>? {
        val moving = cards.find { it.id == movingCardId } ?: return null
        // If already valid, just place
        if (isValidPosition(cards, moving.size, targetRow, targetCol, gridColumns, movingCardId)) {
            return cards.map { if (it.id == movingCardId) it.copy(row = targetRow, column = targetCol) else it }
        }

        // Work on a mutable copy and virtually place the moving card at target
        val working = cards.map { it.copy() }.toMutableList()
        val idxMoving = working.indexOfFirst { it.id == movingCardId }
        if (idxMoving < 0) return null
        working[idxMoving] = working[idxMoving].copy(row = targetRow, column = targetCol)

        // Helper to test validity against current working list
        fun canPlace(
            card: HomeCard,
            r: Int,
            c: Int,
            excludeId: String? = null,
        ): Boolean {
            return isValidPosition(working, card.size, r, c, gridColumns, excludeId)
        }

        // Collect cards that overlap with the moving card at target
        fun overlaps(
            a: HomeCard,
            b: HomeCard,
        ): Boolean {
            val aCols = a.column until (a.column + a.size.columns)
            val aRows = a.row until (a.row + a.size.rows)
            val bCols = b.column until (b.column + b.size.columns)
            val bRows = b.row until (b.row + b.size.rows)
            return aCols.first < bCols.last && bCols.first < aCols.last &&
                aRows.first < bRows.last && bRows.first < aRows.last
        }

        val toResolve = working.filter { it.id != movingCardId && it.isVisible && overlaps(it, working[idxMoving]) }
        val movedIds = mutableSetOf<String>()
        for (conflict in toResolve) {
            val confIdx = working.indexOfFirst { it.id == conflict.id }
            if (confIdx < 0) continue
            // Temporarily remove the conflicting card to search for new spot
            val removed = working.removeAt(confIdx)

            // Find next available position scanning top-to-bottom
            var placed: Pair<Int, Int>? = null
            run {
                var r = 0
                while (r < 200 && placed == null) {
                    for (c in 0 until gridColumns) {
                        if (isValidPosition(working, removed.size, r, c, gridColumns, removed.id)) {
                            placed = Pair(r, c)
                            break
                        }
                    }
                    r++
                }
            }

            if (placed == null) return null // cannot shift
            val newCard = removed.copy(row = placed!!.first, column = placed!!.second)
            working.add(newCard)
            movedIds.add(newCard.id)
        }

        // Final validation that the moving card still fits after shifts
        if (!canPlace(
                working[idxMoving],
                working[idxMoving].row,
                working[idxMoving].column,
                movingCardId,
            )
        ) {
            return null
        }
        return working
    }

    private fun selectCard(card: HomeCard?) {
        _uiState.value = _uiState.value.copy(selectedCard = card)
        if (card != null) {
            emitEffect(HomeScreenEffect.PerformHapticFeedback(HapticType.Click))
        }
    }

    private fun loadConfiguration() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
                try {
                    homeScreenRepository.ensureInitializedWithDefaults()
                } catch (_: Exception) {
                }
                homeScreenRepository.loadHomeScreenConfig().collect { config ->
                    _uiState.value =
                        _uiState.value.copy(
                            config = config,
                            isLoading = false,
                        )
                }
            } catch (e: Exception) {
                _uiState.value =
                    _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Failed to load configuration: ${e.message}",
                    )
                emitEffect(HomeScreenEffect.ShowError("Failed to load layout"))
            }
        }
    }

    private fun saveConfiguration() {
        viewModelScope.launch {
            try {
                val currentConfig = _uiState.value.config
                homeScreenRepository.saveHomeScreenConfig(currentConfig)
                emitEffect(HomeScreenEffect.ConfigurationSaved)
            } catch (e: Exception) {
                emitEffect(HomeScreenEffect.ShowError("Failed to save layout"))
            }
        }
    }

    fun onDragStart(cardId: String) {
        _uiState.value = _uiState.value.copy(isDragActive = true)
        emitEffect(HomeScreenEffect.PerformHapticFeedback(HapticType.LongPress))
    }

    fun onDragEnd() {
        _uiState.value = _uiState.value.copy(isDragActive = false)
        emitEffect(HomeScreenEffect.PerformHapticFeedback(HapticType.Click))
    }

    fun dismissError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    private fun dismissSuccess() {
        _uiState.value = _uiState.value.copy(successMessage = null)
    }

    /**
     * Unified error handling with logging and user feedback
     */
    private fun handleError(
        context: String,
        exception: Exception,
    ) {
        val message = "$context: ${exception.localizedMessage ?: "Unknown error"}"
        _uiState.value =
            _uiState.value.copy(
                errorMessage = message,
                isLoading = false,
                isPerformingBulkOperation = false,
            )
        emitEffect(HomeScreenEffect.ShowError(context))
        emitEffect(HomeScreenEffect.PerformHapticFeedback(HapticType.Error))
        // Log error for debugging (in production, send to analytics)
        android.util.Log.e("CustomizableHomeViewModel", message, exception)
    }

    /**
     * Save current config to undo stack before making changes
     */
    private fun pushToUndoStack() {
        val current = _uiState.value.config
        undoStack.add(current)
        if (undoStack.size > maxHistorySize) {
            undoStack.removeAt(0) // Remove oldest
        }
        redoStack.clear() // Clear redo stack on new action
        updateUndoRedoState()
    }

    /**
     * Perform undo operation
     */
    private fun performUndo() {
        if (undoStack.isEmpty()) {
            emitEffect(HomeScreenEffect.ShowError("Nothing to undo"))
            return
        }

        val currentConfig = _uiState.value.config
        redoStack.add(currentConfig)

        val previousConfig = undoStack.removeAt(undoStack.lastIndex)
        _uiState.value =
            _uiState.value.copy(
                config = previousConfig,
                undoMessage = "Undo successful",
            )

        updateUndoRedoState()
        saveConfiguration()
        emitEffect(HomeScreenEffect.PerformHapticFeedback(HapticType.Success))

        // Clear undo message after delay
        viewModelScope.launch {
            kotlinx.coroutines.delay(2000)
            _uiState.value = _uiState.value.copy(undoMessage = null)
        }
    }

    /**
     * Perform redo operation
     */
    private fun performRedo() {
        if (redoStack.isEmpty()) {
            emitEffect(HomeScreenEffect.ShowError("Nothing to redo"))
            return
        }

        val currentConfig = _uiState.value.config
        undoStack.add(currentConfig)

        val nextConfig = redoStack.removeAt(redoStack.lastIndex)
        _uiState.value =
            _uiState.value.copy(
                config = nextConfig,
                undoMessage = "Redo successful",
            )

        updateUndoRedoState()
        saveConfiguration()
        emitEffect(HomeScreenEffect.PerformHapticFeedback(HapticType.Success))

        // Clear undo message after delay
        viewModelScope.launch {
            kotlinx.coroutines.delay(2000)
            _uiState.value = _uiState.value.copy(undoMessage = null)
        }
    }

    /**
     * Update undo/redo state flags
     */
    private fun updateUndoRedoState() {
        _uiState.value =
            _uiState.value.copy(
                canUndo = undoStack.isNotEmpty(),
                canRedo = redoStack.isNotEmpty(),
            )
    }

    private fun emitEffect(effect: HomeScreenEffect) {
        viewModelScope.launch {
            _effects.emit(effect)
        }
    }

    /**
     * Enhanced position finding with multiple strategies
     */
    private fun findBestAvailablePosition(
        cards: List<HomeCard>,
        cardSize: CardSize,
        gridColumns: Int,
        preferredPosition: Pair<Int, Int>? = null,
    ): Pair<Int, Int>? {
        // First try preferred position if valid
        preferredPosition?.let { pos ->
            if (isValidPosition(cards, cardSize, pos.first, pos.second, gridColumns)) {
                return pos
            }
        }

        // Strategy 1: Find first available position (original logic)
        val simplePosition = findNextAvailablePosition(cards, cardSize, gridColumns)
        if (simplePosition != null) return simplePosition

        // Strategy 2: Try compact arrangement - look for gaps in existing layout
        val occupiedPositions = getOccupiedPositions(cards)
        for (row in 0 until 10) {
            for (col in 0 until gridColumns) {
                if (canPlaceCard(occupiedPositions, cardSize, row, col, gridColumns)) {
                    return Pair(row, col)
                }
            }
        }

        return null // No position found
    }

    /**
     * Check if a card can be placed at a specific position
     */
    private fun canPlaceCard(
        occupiedPositions: Set<Pair<Int, Int>>,
        cardSize: CardSize,
        row: Int,
        col: Int,
        gridColumns: Int,
    ): Boolean {
        // Check bounds
        if (col + cardSize.columns > gridColumns) return false

        // Check for overlaps
        for (r in row until row + cardSize.rows) {
            for (c in col until col + cardSize.columns) {
                if (occupiedPositions.contains(Pair(r, c))) {
                    return false
                }
            }
        }
        return true
    }

    /**
     * Get all occupied positions from current cards
     */
    private fun getOccupiedPositions(cards: List<HomeCard>): Set<Pair<Int, Int>> {
        return cards.filter { it.isVisible }.flatMap { card ->
            (card.row until card.row + card.size.rows).flatMap { r ->
                (card.column until card.column + card.size.columns).map { c ->
                    Pair(r, c)
                }
            }
        }.toSet()
    }

    /**
     * Enhanced validation for card positions
     */
    private fun isValidCardPosition(
        existingCards: List<HomeCard>,
        newCard: HomeCard,
        gridColumns: Int,
    ): Boolean {
        return isValidPosition(
            existingCards,
            newCard.size,
            newCard.row,
            newCard.column,
            gridColumns,
            newCard.id,
        )
    }
}

/**
 * Side effects that can be triggered by the home screen
 */
sealed class HomeScreenEffect {
    object ShowEditModeHint : HomeScreenEffect()

    object ConfigurationSaved : HomeScreenEffect()

    object ConfigurationReset : HomeScreenEffect()

    data class CardAdded(val cardId: String) : HomeScreenEffect()

    data class CardRemoved(val cardId: String) : HomeScreenEffect()

    data class CardMoved(val cardId: String, val newPosition: Pair<Int, Int>) : HomeScreenEffect()

    data class CardResized(val cardId: String, val newSize: CardSize) : HomeScreenEffect()

    data class ShowError(val message: String) : HomeScreenEffect()

    data class ShowSuccess(val message: String) : HomeScreenEffect()

    data class PerformHapticFeedback(val type: HapticType) : HomeScreenEffect()
}

enum class HapticType {
    Click,
    LongPress,
    Success,
    Error,
    Warning,
}
