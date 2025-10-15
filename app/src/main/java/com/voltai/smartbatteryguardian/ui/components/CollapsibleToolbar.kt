package com.voltai.smartbatteryguardian.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.max
import kotlin.math.min

/**
 * A collapsible toolbar that shrinks as the user scrolls down and expands when scrolling up.
 * Follows Material Design 3 guidelines for collapsing app bars.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollapsibleToolbar(
    title: String,
    subtitle: String? = null,
    onBackClick: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    scrollState: LazyListState = rememberLazyListState(),
    expandedHeight: androidx.compose.ui.unit.Dp = 120.dp,
    collapsedHeight: androidx.compose.ui.unit.Dp = 64.dp,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    modifier: Modifier = Modifier,
    content: @Composable (PaddingValues) -> Unit,
) {
    val density = LocalDensity.current

    // Calculate collapse progress based on scroll
    val maxScroll = with(density) { (expandedHeight - collapsedHeight).toPx() }
    val currentScroll = min(scrollState.firstVisibleItemScrollOffset.toFloat(), maxScroll)
    val collapseProgress = max(0f, currentScroll / maxScroll)

    // Calculate dynamic height
    val currentHeight = expandedHeight * (1f - collapseProgress) + collapsedHeight * collapseProgress

    // Calculate title and subtitle alpha and sizes
    val titleAlpha = 1f - (collapseProgress * 0.5f)
    val subtitleAlpha = 1f - collapseProgress

    Column(modifier = modifier.fillMaxSize()) {
        // Collapsible Header
        Surface(
            modifier =
            Modifier
                .fillMaxWidth()
                .height(currentHeight),
            color = backgroundColor,
            shadowElevation = (4.dp * collapseProgress),
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
            ) {
                // Background gradient
                Box(
                    modifier =
                    Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors =
                                listOf(
                                    backgroundColor,
                                    backgroundColor.copy(alpha = 0.95f),
                                ),
                            ),
                        ),
                )

                // Top row with back button and actions
                Row(
                    modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(collapsedHeight)
                        .padding(horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // Back button
                    onBackClick?.let { callback ->
                        IconButton(
                            onClick = callback,
                            modifier = Modifier.size(48.dp),
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = contentColor,
                            )
                        }
                    }

                    // Title (collapsed state)
                    if (collapseProgress > 0.5f) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = contentColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier =
                            Modifier
                                .weight(1f)
                                .alpha(collapseProgress * 2f - 1f)
                                .padding(horizontal = 8.dp),
                        )
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }

                    // Action buttons
                    Row {
                        actions()
                    }
                }

                // Expanded content (title and subtitle)
                if (collapseProgress < 1f) {
                    Column(
                        modifier =
                        Modifier
                            .align(Alignment.BottomStart)
                            .fillMaxWidth()
                            .padding(
                                horizontal = 24.dp,
                                vertical = 16.dp,
                            )
                            .alpha(titleAlpha),
                        verticalArrangement = Arrangement.Bottom,
                    ) {
                        Text(
                            text = title,
                            fontSize = (20 + (8 * (1f - collapseProgress))).sp,
                            fontWeight = FontWeight.Bold,
                            color = contentColor,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )

                        subtitle?.let {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = it,
                                style = MaterialTheme.typography.bodyMedium,
                                color = contentColor.copy(alpha = 0.8f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.alpha(subtitleAlpha),
                            )
                        }
                    }
                }

                // Bottom gradient for smooth transition
                Box(
                    modifier =
                    Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .height(8.dp)
                        .background(
                            Brush.verticalGradient(
                                colors =
                                listOf(
                                    Color.Transparent,
                                    backgroundColor.copy(alpha = 0.1f),
                                ),
                            ),
                        ),
                )
            }
        }

        // Content with top padding to account for toolbar
        content(PaddingValues(top = 0.dp))
    }
}

/**
 * Extension to make any LazyColumn work with CollapsibleToolbar
 */
@Composable
fun CollapsibleScreen(
    title: String,
    subtitle: String? = null,
    onBackClick: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    modifier: Modifier = Modifier,
    content: LazyListScope.() -> Unit,
) {
    val scrollState = rememberLazyListState()

    CollapsibleToolbar(
        title = title,
        subtitle = subtitle,
        onBackClick = onBackClick,
        actions = actions,
        scrollState = scrollState,
        backgroundColor = backgroundColor,
        contentColor = contentColor,
        modifier = modifier,
    ) {
        LazyColumn(
            state = scrollState,
            modifier = Modifier.fillMaxSize(),
            contentPadding =
            PaddingValues(
                horizontal = 16.dp,
                vertical = 16.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            content = content,
        )
    }
}
