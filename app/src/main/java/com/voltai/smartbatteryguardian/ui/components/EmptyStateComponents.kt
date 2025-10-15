package com.voltai.smartbatteryguardian.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

/**
 * Empty state for cards that are collecting initial data
 */
@Composable
fun DataCollectionEmptyState(
    title: String = "Collecting Data",
    message: String = "We're gathering battery information to provide insights.",
    estimatedTime: String = "30 minutes",
    progress: Float = 0f,
    modifier: Modifier = Modifier,
) {
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(200)
        isVisible = true
    }

    AnimatedVisibility(
        visible = isVisible,
        enter =
        fadeIn(animationSpec = tween(600)) +
            slideInVertically(
                initialOffsetY = { it / 3 },
                animationSpec = tween(600),
            ),
        modifier = modifier,
    ) {
        Card(
            modifier =
            Modifier
                .fillMaxWidth()
                .padding(8.dp),
            shape = RoundedCornerShape(16.dp),
            colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            ),
            border =
            BorderStroke(
                width = 1.dp,
                brush =
                Brush.linearGradient(
                    colors =
                    listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f),
                    ),
                ),
            ),
        ) {
            Column(
                modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // Animated collecting icon
                CollectingDataIcon()

                Spacer(modifier = Modifier.height(16.dp))

                // Title
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Message
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp,
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Progress indicator
                DataCollectionProgress(
                    progress = progress,
                    estimatedTime = estimatedTime,
                )
            }
        }
    }
}

/**
 * Empty state for features requiring premium access
 */
@Composable
fun PremiumRequiredEmptyState(
    title: String = "Premium Feature",
    message: String = "Upgrade to access advanced battery analytics and insights.",
    onUpgradeClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Card(
        modifier =
        modifier
            .fillMaxWidth()
            .padding(8.dp),
        shape = RoundedCornerShape(16.dp),
        colors =
        CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f),
        ),
    ) {
        Column(
            modifier =
            Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Premium icon
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = "Premium Required",
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.tertiary,
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onUpgradeClick,
                colors =
                ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.tertiary,
                ),
                shape = RoundedCornerShape(8.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Upgrade Now")
            }
        }
    }
}

/**
 * Empty state for insufficient data scenarios
 */
@Composable
fun InsufficientDataEmptyState(
    title: String = "Not Enough Data",
    message: String = "More battery data needed for accurate analysis.",
    icon: ImageVector = Icons.Default.Info,
    additionalInfo: String? = null,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier =
        modifier
            .fillMaxWidth()
            .padding(8.dp),
        shape = RoundedCornerShape(16.dp),
        colors =
        CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        ),
    ) {
        Column(
            modifier =
            Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
            )

            additionalInfo?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

/**
 * Animated collecting data icon
 */
@Composable
private fun CollectingDataIcon() {
    // Static, battery-efficient collecting data indicator
    Box(
        contentAlignment = Alignment.Center,
    ) {
        // Static background circle
        Box(
            modifier =
            Modifier
                .size(64.dp)
                .background(
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(50),
                ),
        )

        // Static refresh icon
        Icon(
            imageVector = Icons.Default.Refresh,
            contentDescription = "Collecting Data",
            modifier = Modifier.size(32.dp),
            tint = MaterialTheme.colorScheme.primary,
        )
    }
}

/**
 * Data collection progress indicator
 */
@Composable
private fun DataCollectionProgress(
    progress: Float,
    estimatedTime: String,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Progress bar
        LinearProgressIndicator(
            progress = progress,
            modifier =
            Modifier
                .fillMaxWidth()
                .height(4.dp),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Time estimate
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "${(progress * 100).toInt()}% Complete",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            )

            Text(
                text = "~$estimatedTime remaining",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            )
        }
    }
}

/**
 * Welcome message for first-time users
 */
@Composable
fun FirstTimeWelcomeState(
    onGetStarted: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier =
        modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(20.dp),
        colors =
        CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
        ),
    ) {
        Column(
            modifier =
            Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                imageVector = Icons.Default.Home,
                contentDescription = "Welcome",
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary,
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Welcome to VoltAI Guardian!",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "We're starting to collect battery data to provide you with intelligent insights and recommendations.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
                lineHeight = 20.sp,
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = onGetStarted,
                colors =
                ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                ),
                shape = RoundedCornerShape(12.dp),
            ) {
                Text("Get Started")
            }
        }
    }
}

/**
 * Mini empty state for smaller cards
 */
@Composable
fun MiniEmptyState(
    icon: ImageVector,
    message: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            textAlign = TextAlign.Center,
            maxLines = 2,
        )
    }
}
