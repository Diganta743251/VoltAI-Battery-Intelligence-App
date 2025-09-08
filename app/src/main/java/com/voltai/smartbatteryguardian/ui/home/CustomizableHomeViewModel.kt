package com.voltai.smartbatteryguardian.ui.home

import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CustomizableHomeViewModel @Inject constructor(
    private val homeScreenRepository: com.voltai.smartbatteryguardian.data.HomeScreenRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(HomeScreenUiState())
    val uiState: StateFlow<HomeScreenUiState> = _uiState.asStateFlow()
    
    private val _effects = MutableSharedFlow<HomeScreenEffect>()
    val effects: SharedFlow<HomeScreenEffect> = _effects.asSharedFlow()
    
    init {
        loadConfiguration()
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
                is HomeScreenEvent.UpdateConfig -> updateConfig(event.config)
                is HomeScreenEvent.ResetToDefaults -> resetToDefaults(event.keepCustomizations)
                is HomeScreenEvent.SaveConfig -> saveConfiguration()
                is HomeScreenEvent.LoadConfig -> loadConfiguration()
            }
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(errorMessage = "Failed to process customization event: ${e.localizedMessage ?: "Unknown error"}")
            emitEffect(HomeScreenEffect.ShowError("Failed to apply home screen change. Please try again."))
            emitEffect(HomeScreenEffect.PerformHapticFeedback(HapticType.Error))
        }
    }
    
    private fun enterEditMode() {
        _uiState.value = _uiState.value.copy(isEditMode = true)
        emitEffect(HomeScreenEffect.ShowEditModeHint)
        emitEffect(HomeScreenEffect.PerformHapticFeedback(HapticType.LongPress))
    }
    
    private fun exitEditMode() {
        _uiState.value = _uiState.value.copy(
            isEditMode = false,
            selectedCard = null,
            isDragActive = false
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
    
    private fun addCard(cardType: CardType, position: Pair<Int, Int>) {
        val currentConfig = _uiState.value.config
        
        // Enhanced position finding with multiple fallback strategies
        val availablePosition = findBestAvailablePosition(
            currentConfig.cards,
            cardType.defaultSize,
            currentConfig.gridColumns,
            preferredPosition = position
        )
        
        if (availablePosition == null) {
            emitEffect(HomeScreenEffect.ShowError("No space available for this card size"))
            emitEffect(HomeScreenEffect.PerformHapticFeedback(HapticType.Error))
            return
        }
        
        val newCard = HomeCard(
            cardType = cardType,
            size = cardType.defaultSize,
            row = availablePosition.first,
            column = availablePosition.second
        )
        
        // Double-check position validity before adding
        if (!isValidCardPosition(currentConfig.cards, newCard, currentConfig.gridColumns)) {
            emitEffect(HomeScreenEffect.ShowError("Invalid card position detected"))
            emitEffect(HomeScreenEffect.PerformHapticFeedback(HapticType.Error))
            return
        }
        
        val updatedCards = currentConfig.cards + newCard
        val updatedConfig = currentConfig.copy(cards = updatedCards)
        
        _uiState.value = _uiState.value.copy(
            config = updatedConfig,
            showAddCardSheet = false,
            selectedCard = newCard // Auto-select new card for immediate editing
        )
        
        emitEffect(HomeScreenEffect.CardAdded(newCard.id))
        emitEffect(HomeScreenEffect.PerformHapticFeedback(HapticType.Success))
        
        // Auto-save after adding card
        saveConfiguration()
    }
    
    private fun removeCard(cardId: String) {
        val currentConfig = _uiState.value.config
        val updatedCards = currentConfig.cards.filter { it.id != cardId }
        val updatedConfig = currentConfig.copy(cards = updatedCards)
        
        _uiState.value = _uiState.value.copy(
            config = updatedConfig,
            selectedCard = null
        )
        
        emitEffect(HomeScreenEffect.CardRemoved(cardId))
        emitEffect(HomeScreenEffect.PerformHapticFeedback(HapticType.LongPress))
    }
    
    fun moveCardWithDrag(cardId: String, dragOffset: Offset) {
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
                    cardId
                )) {
                
                val updatedCards = cards.map { card ->
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
    
    private fun moveCard(cardId: String, newPosition: Pair<Int, Int>) {
        val currentConfig = _uiState.value.config
        val cardToMove = currentConfig.cards.find { it.id == cardId } ?: return
        
        // Check if the new position is valid
        if (!isValidPosition(
                currentConfig.cards,
                cardToMove.size,
                newPosition.first,
                newPosition.second,
                currentConfig.gridColumns,
                cardId
            )) {
            emitEffect(HomeScreenEffect.ShowError("Invalid position"))
            emitEffect(HomeScreenEffect.PerformHapticFeedback(HapticType.Error))
            return
        }
        
        val updatedCards = currentConfig.cards.map { card ->
            if (card.id == cardId) {
                card.copy(row = newPosition.first, column = newPosition.second)
            } else {
                card
            }
        }
        
        val updatedConfig = currentConfig.copy(cards = updatedCards)
        _uiState.value = _uiState.value.copy(config = updatedConfig)
        
        emitEffect(HomeScreenEffect.CardMoved(cardId, newPosition))
        emitEffect(HomeScreenEffect.PerformHapticFeedback(HapticType.Click))
    }
    
    private fun resizeCard(cardId: String, newSize: CardSize) {
        try {
            val currentConfig = _uiState.value.config
            val cardToResize = currentConfig.cards.find { it.id == cardId }
            
            if (cardToResize == null) {
                emitEffect(HomeScreenEffect.ShowError("Card not found"))
                emitEffect(HomeScreenEffect.PerformHapticFeedback(HapticType.Error))
                return
            }
            
            // Check if the new size is allowed for this card type
            if (newSize !in cardToResize.cardType.allowedSizes) {
                emitEffect(HomeScreenEffect.ShowError("Size ${newSize.displayName} not supported for this card"))
                emitEffect(HomeScreenEffect.PerformHapticFeedback(HapticType.Error))
                return
            }
            
            // Check if the card fits with the new size
            if (!isValidPosition(
                    currentConfig.cards,
                    newSize,
                    cardToResize.row,
                    cardToResize.column,
                    currentConfig.gridColumns,
                    cardId
                )) {
                emitEffect(HomeScreenEffect.ShowError("Not enough space to resize to ${newSize.displayName}"))
                emitEffect(HomeScreenEffect.PerformHapticFeedback(HapticType.Error))
                return
            }
            
            val updatedCards = currentConfig.cards.map { card ->
                if (card.id == cardId) {
                    card.copy(size = newSize)
                } else {
                    card
                }
            }
            
            val updatedConfig = currentConfig.copy(cards = updatedCards)
            _uiState.value = _uiState.value.copy(config = updatedConfig)
            
            emitEffect(HomeScreenEffect.CardResized(cardId, newSize))
            emitEffect(HomeScreenEffect.PerformHapticFeedback(HapticType.Success))
            
            // Auto-save after successful resize
            saveConfiguration()
            
        } catch (e: Exception) {
            emitEffect(HomeScreenEffect.ShowError("Failed to resize card: ${e.message}"))
            emitEffect(HomeScreenEffect.PerformHapticFeedback(HapticType.Error))
        }
    }
    
    private fun selectCard(card: HomeCard?) {
        _uiState.value = _uiState.value.copy(selectedCard = card)
        if (card != null) {
            emitEffect(HomeScreenEffect.PerformHapticFeedback(HapticType.Click))
        }
    }
    
    private fun updateConfig(config: HomeScreenConfig) {
        _uiState.value = _uiState.value.copy(config = config)
    }
    
    private fun resetToDefaults(keepCustomizations: Boolean) {
        val defaultCards = getDefaultHomeCards()
        val currentConfig = _uiState.value.config
        
        val newConfig = if (keepCustomizations) {
            currentConfig.copy(cards = defaultCards)
        } else {
            HomeScreenConfig(cards = defaultCards)
        }
        
        _uiState.value = _uiState.value.copy(
            config = newConfig,
            isEditMode = false,
            selectedCard = null
        )
        
        emitEffect(HomeScreenEffect.ConfigurationReset)
        emitEffect(HomeScreenEffect.PerformHapticFeedback(HapticType.Success))
        saveConfiguration()
    }
    
    private fun loadConfiguration() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                homeScreenRepository.loadHomeScreenConfig().collect { config ->
                    _uiState.value = _uiState.value.copy(
                        config = config,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Failed to load configuration: ${e.message}"
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
        preferredPosition: Pair<Int, Int>? = null
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
        gridColumns: Int
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
        gridColumns: Int
    ): Boolean {
        return isValidPosition(
            existingCards,
            newCard.size,
            newCard.row,
            newCard.column,
            gridColumns,
            newCard.id
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
    Click, LongPress, Success, Error, Warning
}
