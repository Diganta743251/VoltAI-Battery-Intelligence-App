package com.voltai.smartbatteryguardian.ui.home

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.staggeredgrid.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.voltai.smartbatteryguardian.BatteryData
import com.voltai.smartbatteryguardian.ui.animation.AnimatedCardEntrance
import com.voltai.smartbatteryguardian.ui.animation.BatteryGuruHaptics
import com.voltai.smartbatteryguardian.ui.components.MaterialCardContent
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.coroutineScope
import kotlin.math.roundToInt

/**
 * Memoized layout metrics for grid calculations
 */
private data class LayoutMetrics(
    val cellWidthPx: Float,
    val rowHeightPx: Float,
    val hSpacePx: Float,
    val vSpacePx: Float,
    val cellWidthDp: androidx.compose.ui.unit.Dp,
)

/**
 * Drag ghost state for visual feedback during drag operations
 */
private data class DragGhost(val id: String, val row: Int, val col: Int, val size: CardSize, val valid: Boolean)

/**
 * Customizable grid layout for home screen cards with drag-and-drop support.
 *
 * Features:
 * - Responsive grid layout with 1-4 columns
 * - Drag-and-drop card repositioning with visual feedback
 * - Auto-scroll during drag operations
 * - Snap-to-grid precision
 * - Card resizing via pinch gestures
 * - Accessibility support with semantic descriptions
 * - Performance optimizations for large card counts
 * - Battery Guru-style taller cards (180dp base height)
 *
 * @param cards List of cards to display
 * @param batteryData Current battery data for card content
 * @param isEditMode Whether the grid is in edit mode
 * @param isDragActive Whether a drag operation is active
 * @param selectedCard Currently selected card in edit mode
 * @param onCardClick Callback when a card is clicked
 * @param onCardLongPress Callback when a card is long-pressed
 * @param onCardRemove Callback to remove a card
 * @param onToggleVisibility Callback to toggle card visibility
 * @param onCardMoveTo Callback when a card is moved to a new position
 * @param onCardResize Callback when a card is resized
 * @param onDragStart Callback when drag starts
 * @param onDragEnd Callback when drag ends
 * @param onAddCardClick Callback to add a new card
 * @param columns Number of grid columns (1-4)
 * @param reduceMotion Whether to reduce animations for accessibility
 * @param modifier Modifier for the grid container
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CustomizableCardsGrid(
    cards: List<HomeCard>,
    batteryData: BatteryData,
    isEditMode: Boolean,
    isDragActive: Boolean,
    selectedCard: HomeCard?,
    onCardClick: (HomeCard) -> Unit,
    onCardLongPress: (HomeCard) -> Unit,
    onCardRemove: (String) -> Unit,
    onToggleVisibility: (String) -> Unit,
    onCardMoveTo: (String, Pair<Int, Int>) -> Unit,
    onCardResize: (String, CardSize) -> Unit,
    onDragStart: (String) -> Unit,
    onDragEnd: () -> Unit,
    onAddCardClick: () -> Unit = {},
    columns: Int = 2,
    reduceMotion: Boolean = false,
    modifier: Modifier = Modifier,
) {
    // Performance optimizations: Reduce animation complexity with many cards
    val shouldUseReducedAnimations = cards.size > 8 || reduceMotion
    val shouldUseFastAnimations = cards.size > 12

    BoxWithConstraints(modifier = modifier.fillMaxSize().padding(horizontal = 12.dp)) {
        // Honor explicit columns passed from caller (e.g., fixed 4 like Android launcher)
        val cols = columns

        // Responsive spacing based on screen width
        val horizontalSpacing =
            remember(maxWidth) {
                when {
                    maxWidth < 360.dp -> 6.dp
                    maxWidth < 600.dp -> 8.dp
                    else -> 12.dp
                }
            }
        val verticalSpacing =
            remember(maxWidth) {
                when {
                    maxWidth < 360.dp -> 6.dp
                    maxWidth < 600.dp -> 8.dp
                    else -> 12.dp
                }
            }

        // Base row height: 2 rows = 180dp total (Battery Guru style)
        val rowBase = 90.dp
        val density = LocalDensity.current

        // Memoize expensive calculations
        val layoutMetrics =
            remember(maxWidth, cols, horizontalSpacing, rowBase, verticalSpacing) {
                val cellWidthDp = (maxWidth - horizontalSpacing * (cols - 1)) / cols
                LayoutMetrics(
                    cellWidthPx = with(density) { cellWidthDp.toPx() },
                    rowHeightPx = with(density) { rowBase.toPx() },
                    hSpacePx = with(density) { horizontalSpacing.toPx() },
                    vSpacePx = with(density) { verticalSpacing.toPx() },
                    cellWidthDp = cellWidthDp,
                )
            }

        val rowStepPx = layoutMetrics.rowHeightPx + layoutMetrics.vSpacePx
        val colStepPx = layoutMetrics.cellWidthPx + layoutMetrics.hSpacePx

        // Viewport + autoscroll helpers
        val viewportHeightPx = with(density) { maxHeight.toPx() }
        val autoScrollSpeedPx = with(density) { 28.dp.toPx() }
        val scope = rememberCoroutineScope()
        var dragGhost by remember { mutableStateOf<DragGhost?>(null) }
        var a11yMessage by remember { mutableStateOf<String?>(null) }

        val visibleCards = remember(cards) { cards.filter { it.isVisible } }

        // Performance optimization: memoize expensive calculation with proper dependencies
        val contentRows =
            remember(
                visibleCards.size,
                visibleCards.maxByOrNull {
                    it.row + it.size.rows
                }?.let { it.row + it.size.rows },
            ) {
                (visibleCards.maxOfOrNull { it.row + it.size.rows } ?: 0).coerceAtLeast(6)
            }
        // Consistent spacing from bars
        val topBarHeight = 16.dp // Reduced for better Battery Guru style
        val bottomBarHeight = 16.dp
        val contentHeightDp = rowBase * contentRows + verticalSpacing * (contentRows - 1).coerceAtLeast(0)
        val scrollState = rememberScrollState()

        // Compute live preview positions for auto-shift while dragging
        val previewPositions: Map<String, Pair<Int, Int>> =
            remember(cards, dragGhost, cols) {
                val g = dragGhost
                if (g == null) emptyMap() else previewShiftPositions(cards, g, cols) ?: emptyMap()
            }

        Box(modifier = Modifier.fillMaxSize().verticalScroll(scrollState)) {
            // Absolute positioning surface with consistent padding
            Box(
                modifier =
                Modifier
                    .height(contentHeightDp + topBarHeight + bottomBarHeight)
                    .fillMaxWidth()
                    .padding(top = topBarHeight, bottom = bottomBarHeight),
            ) {
                visibleCards.forEach { card ->
                    val cardWidth = layoutMetrics.cellWidthDp * card.size.columns + horizontalSpacing * (card.size.columns - 1)
                    val previewPos = previewPositions[card.id].takeIf { dragGhost?.id != card.id }
                    val usedCol = previewPos?.second ?: card.column
                    val usedRow = previewPos?.first ?: card.row
                    val targetBaseX = (layoutMetrics.cellWidthDp + horizontalSpacing) * usedCol
                    val targetBaseY = (rowBase + verticalSpacing) * usedRow
                    val animatedBaseX by animateDpAsState(
                        targetValue = targetBaseX,
                        animationSpec = tween(durationMillis = 250, easing = LinearOutSlowInEasing),
                    )
                    val animatedBaseY by animateDpAsState(
                        targetValue = targetBaseY,
                        animationSpec = tween(durationMillis = 250, easing = LinearOutSlowInEasing),
                    )

                    AnimatedCardEntrance(
                        visible = true,
                        delay =
                        when {
                            shouldUseFastAnimations -> 0 // No delay for performance
                            shouldUseReducedAnimations ->
                                (cards.indexOf(card) * 15).coerceAtMost(
                                    150,
                                ) // Quick cascade
                            else -> (cards.indexOf(card) * 40).coerceAtMost(300) // Smooth staggered reveal
                        },
                    ) {
                        DraggableCard(
                            card = card,
                            batteryData = batteryData,
                            isEditMode = isEditMode,
                            isSelected = selectedCard?.id == card.id,
                            isDragActive = isDragActive,
                            reduceMotion = reduceMotion,
                            cellWidthPx = layoutMetrics.cellWidthPx,
                            rowHeightPx = rowStepPx,
                            colStepPx = colStepPx,
                            gridColumns = cols,
                            contentRows = contentRows,
                            currentScrollY = scrollState.value.toFloat(),
                            viewportHeightPx = viewportHeightPx,
                            autoScrollSpeedPx = autoScrollSpeedPx,
                            isValidTarget = { r, c ->
                                // Clean validation with proper bounds checking
                                isValidPosition(
                                    cards,
                                    card.size,
                                    r.coerceAtLeast(0),
                                    c.coerceAtLeast(0),
                                    cols,
                                    excludeCardId = card.id,
                                )
                            },
                            canFitSize = { newSize ->
                                isValidPosition(
                                    cards,
                                    newSize,
                                    card.row,
                                    card.column,
                                    cols,
                                    excludeCardId = card.id,
                                )
                            },
                            onPreviewChange = { r, c, size, valid ->
                                dragGhost = DragGhost(card.id, r, c, size, valid)
                            },
                            onPreviewEnd = { dragGhost = null },
                            onCardClick = { onCardClick(card) },
                            onCardLongPress = { onCardLongPress(card) },
                            onCardRemove = { onCardRemove(card.id) },
                            onToggleVisibility = { onToggleVisibility(card.id) },
                            onCardResize = { newSize -> onCardResize(card.id, newSize) },
                            onCardMoveTo = { newPos -> onCardMoveTo(card.id, newPos) },
                            onInvalidDrop = {
                                val fallback =
                                    findNextAvailablePosition(
                                        cards = cards,
                                        cardSize = card.size,
                                        gridColumns = cols,
                                    )
                                if (fallback != null) {
                                    onCardMoveTo(card.id, fallback)
                                    a11yMessage = "Card auto-placed"
                                    true
                                } else {
                                    false
                                }
                            },
                            onAutoScrollBy = { delta ->
                                scope.launch { scrollState.scrollBy(delta) }
                            },
                            onDragStart = { onDragStart(card.id) },
                            onDragEnd = onDragEnd,
                            modifier =
                            Modifier
                                .width(cardWidth)
                                .offset(x = animatedBaseX, y = animatedBaseY),
                        )
                    }
                }

                // Removed Add Card placeholder in edit mode (actions moved to top bar)
            }
        }

        // Professional empty state with Battery Guru styling
        if (visibleCards.isEmpty()) {
            Box(
                modifier =
                Modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.size(32.dp),
                    )
                    Text(
                        text = "No cards yet",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = if (isEditMode) "Tap + to add your first card" else "Long-press for 3s to customize, or add a card",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Button(onClick = onAddCardClick) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Add card")
                    }
                }
            }
        }

        // Grid overlay in edit mode (subtle lines), drawn over content
        if (isEditMode) {
            val overlayColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
            val primaryColor = MaterialTheme.colorScheme.primary
            val errorColor = MaterialTheme.colorScheme.error
            // Pulse highlight for valid ghost target
            val infinite = rememberInfiniteTransition(label = "gridPulse")
            val pulseAlpha by infinite.animateFloat(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec =
                infiniteRepeatable(
                    animation = tween(durationMillis = 900, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse,
                ),
                label = "pulse",
            )
            Canvas(modifier = Modifier.matchParentSize()) {
                val w = size.width
                val h = size.height
                val colWidth = colStepPx
                val rowHeight = rowStepPx
                val scrollY = scrollState.value.toFloat()

                // Vertical lines
                for (i in 1 until cols) {
                    val x = i * colWidth
                    drawLine(
                        color = overlayColor,
                        start = Offset(x, 0f),
                        end = Offset(x, h),
                        strokeWidth = 1f,
                    )
                }

                // Horizontal guide lines (align with scroll)
                var y = (rowHeight - (scrollY % rowHeight)).coerceIn(0f, rowHeight)
                while (y < h) {
                    drawLine(
                        color = overlayColor,
                        start = Offset(0f, y),
                        end = Offset(w, y),
                        strokeWidth = 1f,
                    )
                    y += rowHeight
                }

                // Drag ghost preview
                dragGhost?.let { ghost ->
                    val gx = (ghost.col * colWidth)
                    val gy = (ghost.row * rowHeight) - scrollY
                    val gw = (ghost.size.columns * layoutMetrics.cellWidthPx + (ghost.size.columns - 1) * layoutMetrics.hSpacePx)
                    val gh = (ghost.size.rows * layoutMetrics.rowHeightPx + (ghost.size.rows - 1) * layoutMetrics.vSpacePx)
                    val r = 16.dp.toPx()
                    val animatedAlpha = if (ghost.valid) (0.12f + 0.08f * pulseAlpha) else 0.12f
                    val fillColor = (if (ghost.valid) primaryColor else errorColor).copy(alpha = animatedAlpha)
                    val strokeColor = if (ghost.valid) primaryColor else errorColor
                    drawRoundRect(
                        color = fillColor,
                        topLeft = Offset(gx, gy),
                        size = androidx.compose.ui.geometry.Size(gw, gh),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(r, r),
                    )
                    drawRoundRect(
                        color = strokeColor,
                        topLeft = Offset(gx, gy),
                        size = androidx.compose.ui.geometry.Size(gw, gh),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(r, r),
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f),
                    )
                }
            }
            // Announce auto-place results with proper cleanup
            a11yMessage?.let { msg ->
                Text(
                    text = msg,
                    color = Color.Transparent,
                    modifier = Modifier.semantics { liveRegion = LiveRegionMode.Assertive },
                )
                LaunchedEffect(msg) {
                    delay(800)
                    a11yMessage = null
                }
            }

            // Cleanup accessibility message on disposal
            DisposableEffect(Unit) {
                onDispose {
                    a11yMessage = null
                }
            }
        }
    }
}

@Composable
private fun DraggableCard(
    card: HomeCard,
    batteryData: BatteryData,
    isEditMode: Boolean,
    isSelected: Boolean,
    isDragActive: Boolean,
    reduceMotion: Boolean,
    cellWidthPx: Float,
    rowHeightPx: Float,
    colStepPx: Float,
    gridColumns: Int,
    contentRows: Int,
    currentScrollY: Float,
    viewportHeightPx: Float,
    autoScrollSpeedPx: Float,
    isValidTarget: (Int, Int) -> Boolean,
    canFitSize: (CardSize) -> Boolean,
    onPreviewChange: (Int, Int, CardSize, Boolean) -> Unit,
    onPreviewEnd: () -> Unit,
    onCardClick: () -> Unit,
    onCardLongPress: () -> Unit,
    onCardRemove: () -> Unit,
    onToggleVisibility: () -> Unit,
    onCardResize: (CardSize) -> Unit,
    onCardMoveTo: (Pair<Int, Int>) -> Unit,
    onInvalidDrop: () -> Boolean,
    onAutoScrollBy: (Float) -> Unit,
    onDragStart: () -> Unit,
    onDragEnd: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var isPressed by remember { mutableStateOf(false) }
    var isDragging by remember { mutableStateOf(false) }
    var dragOffset by remember { mutableStateOf(Offset.Zero) }
    var startScrollY by remember { mutableStateOf(0f) }
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    val shakeX = remember { androidx.compose.animation.core.Animatable(0f) }
    var potentialRow by remember { mutableStateOf(card.row) }
    var potentialCol by remember { mutableStateOf(card.column) }
    var potentialValid by remember { mutableStateOf(true) }
    val autoScrollEdgePx = with(LocalDensity.current) { 64.dp.toPx() }

    // Smooth animations for widget-like feel
    val scale by animateFloatAsState(
        targetValue =
        when {
            isDragging -> 1.08f // More pronounced lift during drag
            isPressed -> 0.96f // Subtle press feedback
            else -> 1f
        },
        animationSpec =
        spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow,
        ),
    )

    val elevation by animateDpAsState(
        targetValue =
        when {
            isDragging -> 20.dp // Strong elevation during drag (widget-like)
            isSelected && isEditMode -> 10.dp // Selected cards stand out
            isEditMode -> 6.dp // Edit mode cards are elevated
            else -> 8.dp // Stronger base elevation for depth (Battery Guru style)
        },
        animationSpec =
        spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium,
        ),
    )

    val baseBorderColor = if (isSelected) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.outlineVariant
    val borderColor by animateColorAsState(
        targetValue =
        if (isDragging) {
            if (potentialValid) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
        } else {
            baseBorderColor
        },
        animationSpec = tween(durationMillis = 250, easing = LinearOutSlowInEasing),
    )

    val animatedOffset by animateOffsetAsState(
        targetValue = if (isDragging) dragOffset else Offset.Zero,
        animationSpec =
        if (isDragging) {
            tween(0) // Immediate during drag for precise widget control
        } else {
            tween(durationMillis = 250, easing = LinearOutSlowInEasing)
        },
    )

    Box(
        modifier =
        modifier
            .scale(scale)
            .offset {
                IntOffset(
                    (animatedOffset.x + shakeX.value).roundToInt(),
                    animatedOffset.y.roundToInt(),
                )
            }
            .zIndex(if (isDragging) 1f else 0f),
    ) {
        // Calculate card height based on rows
        val rowBaseDp = 90.dp // Base row height: 2 rows = 180dp
        val verticalSpacingDp = 8.dp // Must match grid spacing
        // Card must include spacing between the grid rows it spans
        val cardHeightDp = rowBaseDp * card.size.rows + verticalSpacingDp * (card.size.rows - 1)

        Card(
            modifier =
            Modifier
                .fillMaxWidth()
                .height(height = animateDpAsState(
                    targetValue = cardHeightDp,
                    animationSpec = tween(durationMillis = 250, easing = LinearOutSlowInEasing),
                ).value)
                .border(
                    width = if (isDragging) 2.dp else 1.dp, // Thicker border while dragging
                    color = borderColor,
                    shape = RoundedCornerShape(20.dp), // Widget-like rounded corners
                )
                .then(
                    if (isEditMode) {
                        Modifier.pointerInput(card.id, card.size) {
                            detectTransformGestures { _, _, zoom, _ ->
                                if (zoom.isNaN() || zoom.isInfinite()) return@detectTransformGestures
                                val growThreshold = 1.12f
                                val shrinkThreshold = 0.88f

                                val sizes = card.cardType.allowedSizes.toList()
                                val curIndex = sizes.indexOf(card.size).let { if (it < 0) 0 else it }
                                if (zoom > growThreshold) {
                                    val next = sizes.getOrNull(curIndex + 1) ?: sizes.last()
                                    if (next != card.size && canFitSize(next)) onCardResize(next)
                                } else if (zoom < shrinkThreshold) {
                                    val prev = sizes.getOrNull(curIndex - 1) ?: sizes.first()
                                    if (prev != card.size && canFitSize(prev)) onCardResize(prev)
                                }
                            }
                        }
                    } else {
                        Modifier
                    },
                )
                .then(
                    if (isEditMode) {
                        Modifier.pointerInput(card.id) {
                            detectDragGestures(
                                onDragStart = { _ ->
                                    isDragging = true
                                    onDragStart()
                                    // Enhanced haptic for drag start
                                    BatteryGuruHaptics.run { haptic.dragStart() }
                                    startScrollY = currentScrollY
                                    dragOffset = Offset.Zero // Reset for clean drag start
                                },
                                onDragEnd = {
                                    isDragging = false
                                    // Precise snap to grid: where the card visually stands after scrolling
                                    val effectiveY = dragOffset.y + (startScrollY - currentScrollY)

                                    // Calculate target position with precise rounding
                                    val deltaCols = kotlin.math.round(dragOffset.x / colStepPx).toInt()
                                    val deltaRows = kotlin.math.round(effectiveY / rowHeightPx).toInt()

                                    val targetRow = (card.row + deltaRows).coerceIn(0, contentRows - card.size.rows)
                                    val targetCol =
                                        (card.column + deltaCols).coerceIn(
                                            0,
                                            (gridColumns - card.size.columns).coerceAtLeast(0),
                                        )

                                    // Validate final target; fallback or shake on invalid
                                    if (isValidTarget(targetRow, targetCol)) {
                                        // Request move to final position
                                        onCardMoveTo(Pair(targetRow, targetCol))
                                        BatteryGuruHaptics.run { haptic.dragDrop() }
                                    } else {
                                        val handled = onInvalidDrop()
                                        if (!handled) {
                                            // Subtle shake to indicate invalid drop
                                            scope.launch {
                                                try {
                                                    shakeX.snapTo(0f)
                                                    shakeX.animateTo(-12f, animationSpec = tween(60))
                                                    shakeX.animateTo(12f, animationSpec = tween(120))
                                                    shakeX.animateTo(0f, animationSpec = tween(60))
                                                } catch (_: Exception) { }
                                            }
                                        }
                                    }

                                    // Clean reset
                                    dragOffset = Offset.Zero
                                    startScrollY = 0f
                                    onPreviewEnd()
                                    onDragEnd()
                                },
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    dragOffset += dragAmount

                                    // Real-time position calculation with scroll compensation
                                    val effectiveY = dragOffset.y + (startScrollY - currentScrollY)

                                    // Snap-to-grid with improved precision
                                    val snapThreshold = 0.4f // Snap when within 40% of grid cell
                                    val colOffset = dragOffset.x / colStepPx
                                    val rowOffset = effectiveY / rowHeightPx

                                    val dCols =
                                        if (kotlin.math.abs(colOffset - kotlin.math.round(colOffset)) < snapThreshold) {
                                            kotlin.math.round(colOffset).toInt()
                                        } else {
                                            kotlin.math.floor(colOffset + 0.5f).toInt()
                                        }

                                    val dRows =
                                        if (kotlin.math.abs(rowOffset - kotlin.math.round(rowOffset)) < snapThreshold) {
                                            kotlin.math.round(rowOffset).toInt()
                                        } else {
                                            kotlin.math.floor(rowOffset + 0.5f).toInt()
                                        }

                                    potentialRow = (card.row + dRows).coerceIn(0, contentRows - card.size.rows)
                                    potentialCol = (card.column + dCols).coerceIn(0, (gridColumns - card.size.columns).coerceAtLeast(0))
                                    potentialValid = isValidTarget(potentialRow, potentialCol)

                                    // Update ghost preview
                                    onPreviewChange(potentialRow, potentialCol, card.size, potentialValid)

                                    // Auto-scroll when ghost approaches viewport edges with smooth acceleration
                                    val ghostTopPx = potentialRow * rowHeightPx - currentScrollY
                                    val ghostBottomPx = ghostTopPx + (card.size.rows * rowHeightPx)
                                    val edgeDistance =
                                        when {
                                            ghostTopPx < autoScrollEdgePx -> autoScrollEdgePx - ghostTopPx
                                            ghostBottomPx > (viewportHeightPx - autoScrollEdgePx) -> ghostBottomPx - (viewportHeightPx - autoScrollEdgePx)
                                            else -> 0f
                                        }

                                    if (edgeDistance > 0f) {
                                        // Accelerate scroll speed based on distance from edge
                                        val scrollMultiplier = (edgeDistance / autoScrollEdgePx).coerceIn(0.5f, 2f)
                                        val scrollAmount = autoScrollSpeedPx * scrollMultiplier
                                        when {
                                            ghostTopPx < autoScrollEdgePx -> onAutoScrollBy(-scrollAmount)
                                            else -> onAutoScrollBy(scrollAmount)
                                        }
                                    }
                                },
                            )
                        }
                    } else {
                        Modifier
                    },
                )
                .then(
                    if (!isEditMode) {
                        Modifier.pointerInput(card.id) {
                            detectTapGestures(
                                onTap = {
                                    onCardClick()
                                    BatteryGuruHaptics.run { haptic.cardClick() }
                                },
                                onPress = {
                                    coroutineScope {
                                        val job = launch {
                                            delay(3000)
                                            BatteryGuruHaptics.run { haptic.cardLongPress() }
                                            onCardLongPress()
                                        }
                                        try {
                                            tryAwaitRelease()
                                        } finally {
                                            job.cancel()
                                        }
                                    }
                                },
                            )
                        }
                    } else {
                        Modifier
                    },
                )
                .then(
                    if (isEditMode) {
                        Modifier.clickable(enabled = !isDragging) {
                            // In edit mode, tap selects card
                            onCardClick()
                            BatteryGuruHaptics.run { haptic.cardClick() }
                        }
                    } else {
                        Modifier
                    },
                )
                .semantics(mergeDescendants = true) {
                    contentDescription =
                        if (isDragging) {
                            "${card.cardType.title} card " + if (potentialValid) "valid placement" else "invalid placement"
                        } else {
                            "${card.cardType.title} card"
                        }
                    role = Role.Button
                    liveRegion = LiveRegionMode.Polite
                },
            shape = RoundedCornerShape(20.dp), // Consistent widget-like corners
            colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
            elevation =
            CardDefaults.cardElevation(
                defaultElevation = elevation,
                pressedElevation = elevation + 2.dp,
                draggedElevation = elevation + 4.dp,
            ),
        ) {
            Box(
                modifier =
                Modifier
                    .fillMaxSize()
                    .padding(
                        when {
                            isEditMode && card.size.rows >= 2 -> 14.dp
                            isEditMode && card.size.columns >= 2 -> 12.dp
                            isEditMode -> 10.dp
                            else -> 12.dp
                        },
                    ), // Reduced padding for tighter layout
            ) {
                // Card Content (Material 3 compliant) with smoother edit mode transitions
                androidx.compose.animation.AnimatedVisibility(
                    visible = true,
                    enter =
                    if (reduceMotion) {
                        fadeIn(animationSpec = tween(150))
                    } else {
                        scaleIn(animationSpec = tween(220), initialScale = 0.98f) + fadeIn(animationSpec = tween(220))
                    },
                ) {
                    MaterialCardContent(
                        cardType = card.cardType,
                        batteryData = batteryData,
                        size = card.size,
                        isEditMode = isEditMode,
                        modifier = Modifier.fillMaxSize(),
                        reduceMotion = reduceMotion,
                    )
                }

                // Edit Mode Overlay
                if (isEditMode) {
                    EditModeOverlay(
                        card = card,
                        isSelected = isSelected,
                        onRemove = onCardRemove,
                        onResize = onCardResize,
                        modifier = Modifier.fillMaxSize(),
                    )
                }

                // Accessibility live region announcements while dragging
                if (isEditMode && isDragging) {
                    androidx.compose.material3.Text(
                        text = if (potentialValid) "Valid placement" else "Invalid placement",
                        color = Color.Transparent,
                        modifier = Modifier.semantics { liveRegion = LiveRegionMode.Assertive },
                    )
                }
            }
        }

        // Visibility Toggle (Top-Start)
        AnimatedVisibility(
            visible = isSelected,
            enter = scaleIn(animationSpec = tween(200)) + fadeIn(),
            exit = scaleOut(animationSpec = tween(200)) + fadeOut(),
            modifier =
            Modifier
                .align(Alignment.TopStart)
                .offset(x = (-8).dp, y = (-8).dp),
        ) {
            Surface(
                modifier =
                Modifier
                    .size(48.dp)
                    .clickable { onToggleVisibility() }
                    .semantics {
                        role = Role.Button
                        contentDescription = if (card.isVisible) "Hide ${card.cardType.title} card" else "Show ${card.cardType.title} card"
                    },
                shape = CircleShape,
                color = MaterialTheme.colorScheme.secondary,
                shadowElevation = 6.dp,
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize(),
                ) {
                    Icon(
                        imageVector = if (card.isVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = if (card.isVisible) "Hide ${card.cardType.title} card" else "Show ${card.cardType.title} card",
                        tint = MaterialTheme.colorScheme.onSecondary,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun EditModeOverlay(
    card: HomeCard,
    isSelected: Boolean,
    onRemove: () -> Unit,
    onResize: (CardSize) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {
        // Remove Button
        AnimatedVisibility(
            visible = isSelected,
            enter = scaleIn(animationSpec = tween(200)) + fadeIn(),
            exit = scaleOut(animationSpec = tween(200)) + fadeOut(),
            modifier =
            Modifier
                .align(Alignment.TopEnd)
                .offset(x = 8.dp, y = (-8).dp),
        ) {
            Surface(
                modifier =
                Modifier
                    .size(48.dp)
                    .clickable { onRemove() }
                    .semantics {
                        role = Role.Button
                        contentDescription = "Remove ${card.cardType.title} card"
                    },
                shape = CircleShape,
                color = MaterialTheme.colorScheme.error,
                shadowElevation = 6.dp,
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize(),
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Remove ${card.cardType.title} card",
                        tint = MaterialTheme.colorScheme.onError,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        }

        // Resize Handle (bottom-right)
        if (card.cardType.allowedSizes.size > 1) {
            AnimatedVisibility(
                visible = isSelected,
                enter = scaleIn(animationSpec = tween(200)) + fadeIn(),
                exit = scaleOut(animationSpec = tween(200)) + fadeOut(),
                modifier =
                Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = 8.dp, y = 8.dp),
            ) {
                Surface(
                    modifier =
                    Modifier
                        .size(48.dp)
                        .clickable {
                            // Cycle through available sizes; validation handled at higher level if needed
                            val currentIndex = card.cardType.allowedSizes.indexOf(card.size)
                            val nextIndex = (currentIndex + 1) % card.cardType.allowedSizes.size
                            val newSize = card.cardType.allowedSizes.elementAt(nextIndex)
                            onResize(newSize)
                        }
                        .semantics {
                            role = Role.Button
                            contentDescription = "Resize ${card.cardType.title} card"
                        },
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    shadowElevation = 4.dp,
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Resize ${card.cardType.title} card",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
            }
        }

        // Edit Mode Overlay Background with smoother transitions
        AnimatedVisibility(
            visible = isSelected,
            enter = fadeIn(animationSpec = tween(300, easing = EaseInOutCubic)),
            exit = fadeOut(animationSpec = tween(200, easing = EaseInOutCubic)),
        ) {
            Box(
                modifier =
                Modifier
                    .fillMaxSize()
                    .background(
                        Color.Black.copy(alpha = 0.08f),
                        RoundedCornerShape(16.dp),
                    ),
            )
        }
    }
}

@Composable
private fun AddCardPlaceholder(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier =
        modifier
            .fillMaxWidth()
            .height(180.dp) // Match Battery Guru card height
            .clickable { onClick() }
            .semantics {
                role = Role.Button
                contentDescription = "Add card"
            },
        shape = RoundedCornerShape(20.dp),
        colors =
        CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        ),
        border =
        BorderStroke(
            width = 2.dp,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
        ),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize(),
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    modifier = Modifier.size(48.dp),
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Card",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp),
                        )
                    }
                }

                Text(
                    text = "Add Card",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

@Composable
private fun animateOffsetAsState(
    targetValue: Offset,
    animationSpec: AnimationSpec<Offset>,
): State<Offset> {
    return animateValueAsState(
        targetValue = targetValue,
        typeConverter = Offset.VectorConverter,
        animationSpec = animationSpec,
    )
}

/**
 * Compute a temporary mapping of cardId -> (row, col) for a live drag preview.
 * Moves overlapping cards aside (scanning rows top-to-bottom) while the ghost is hovering.
 * Returns empty map if the ghost is invalid or no shifts are needed.
 */
private fun previewShiftPositions(
    cards: List<HomeCard>,
    ghost: DragGhost,
    gridColumns: Int,
): Map<String, Pair<Int, Int>>? {
    if (!ghost.valid) return emptyMap()

    // Work only with visible cards
    val visible = cards.filter { it.isVisible }

    // Place ghost by replacing the moving card's position
    val working = visible.map { it.copy() }.toMutableList()
    val movingIdx = working.indexOfFirst { it.id == ghost.id }
    if (movingIdx < 0) return emptyMap()
    val movingCard = working[movingIdx].copy(row = ghost.row, column = ghost.col)
    working[movingIdx] = movingCard

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

    val moved = mutableMapOf<String, Pair<Int, Int>>()

    // Resolve conflicts deterministically: by row then column
    val conflicts =
        working.filter { it.id != ghost.id && overlaps(it, movingCard) }
            .sortedWith(compareBy<HomeCard> { it.row }.thenBy { it.column })

    conflicts.forEach { conflict ->
        val idx = working.indexOfFirst { it.id == conflict.id }
        if (idx < 0) return@forEach
        val removed = working.removeAt(idx)

        // Find next available position scanning top-to-bottom; prefer same or below current row
        var placed: Pair<Int, Int>? = null
        var r = removed.row
        var attempts = 0
        while (placed == null && r < 200 && attempts < 400) {
            for (c in 0 until gridColumns) {
                if (isValidPosition(working, removed.size, r, c, gridColumns, removed.id)) {
                    placed = Pair(r, c)
                    break
                }
            }
            r++
            attempts++
        }

        // If we can't place it, abort preview shifts
        if (placed == null) return emptyMap()

        val p = placed
        val newCard = removed.copy(row = p.first, column = p.second)
        working.add(newCard)
        moved[newCard.id] = p
    }

    return moved
}
