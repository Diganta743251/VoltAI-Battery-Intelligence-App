package com.voltai.smartbatteryguardian.ui.premium

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.voltai.smartbatteryguardian.premium.PremiumManager
import com.voltai.smartbatteryguardian.ui.theme.extendedColors
import com.voltai.smartbatteryguardian.viewmodels.PremiumViewModel

/**
 * Component that gates content behind premium access
 */
@Composable
fun PremiumGate(
    featureId: String,
    modifier: Modifier = Modifier,
    premiumViewModel: PremiumViewModel = hiltViewModel(),
    onUpgradeClick: () -> Unit = {},
    content: @Composable () -> Unit,
) {
    val isFeatureAvailable = premiumViewModel.isFeatureAvailable(featureId)

    if (isFeatureAvailable) {
        content()
    } else {
        PremiumLockedContent(
            featureId = featureId,
            modifier = modifier,
            onUpgradeClick = onUpgradeClick,
            premiumViewModel = premiumViewModel,
        ) {
            Box(modifier = Modifier.blur(8.dp)) {
                content()
            }
        }
    }
}

/**
 * Component that shows locked premium content with upgrade prompt
 */
@Composable
fun PremiumLockedContent(
    featureId: String,
    modifier: Modifier = Modifier,
    premiumViewModel: PremiumViewModel = hiltViewModel(),
    onUpgradeClick: () -> Unit = {},
    content: @Composable () -> Unit,
) {
    val paywallData = premiumViewModel.showPremiumPaywall(featureId)

    Box(
        modifier = modifier.fillMaxSize(),
    ) {
        // Blurred background content
        content()

        // Premium overlay
        Box(
            modifier =
            Modifier
                .fillMaxSize()
                .background(
                    MaterialTheme.colorScheme.scrim.copy(alpha = 0.6f),
                ),
            contentAlignment = Alignment.Center,
        ) {
            PremiumUpgradeCard(
                paywallData = paywallData,
                onUpgradeClick = onUpgradeClick,
            )
        }
    }
}

/**
 * Premium upgrade card component
 */
@Composable
fun PremiumUpgradeCard(
    paywallData: PremiumManager.PremiumPaywallData,
    onUpgradeClick: () -> Unit = {},
) {
    Card(
        modifier =
        Modifier
            .fillMaxWidth()
            .padding(24.dp),
        shape = RoundedCornerShape(20.dp),
        colors =
        CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
    ) {
        Column(
            modifier =
            Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Premium badge
            PremiumBadge()

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = paywallData.title,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = paywallData.description,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Benefits list
            paywallData.benefits.take(3).forEach { benefit ->
                PremiumBenefitItem(benefit = benefit)
                Spacer(modifier = Modifier.height(8.dp))
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Upgrade button
            Button(
                onClick = onUpgradeClick,
                modifier =
                Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors =
                ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ),
            ) {
                Text(
                    text = "Upgrade to Premium",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Starting from ${paywallData.recommendedPlan.price}",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/**
 * Premium badge component
 */
@Composable
fun PremiumBadge(
    modifier: Modifier = Modifier,
    size: Int = 64,
) {
    // Static, battery-efficient premium badge
    Box(
        modifier =
        modifier
            .size(size.dp)
            .background(
                MaterialTheme.extendedColors.premiumGold,
                shape = CircleShape,
            )
            .border(
                2.dp,
                MaterialTheme.extendedColors.premiumGold.copy(alpha = 0.85f),
                CircleShape,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "💎",
            fontSize = (size * 0.4).sp,
            color = Color.White,
        )
    }
}

/**
 * Premium benefit item with checkmark
 */
@Composable
fun PremiumBenefitItem(
    benefit: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = "Benefit",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp),
        )

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = benefit,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
    }
}

/**
 * Premium status indicator for top bar
 */
@Composable
fun PremiumStatusIndicator(
    premiumViewModel: PremiumViewModel = hiltViewModel(),
    onPremiumClick: () -> Unit = {},
) {
    val premiumStatus by premiumViewModel.premiumState.collectAsState()
    val isPremium = premiumStatus != PremiumManager.PremiumStatus.FREE

    Surface(
        modifier = Modifier.clickable { onPremiumClick() },
        shape = RoundedCornerShape(16.dp),
        color =
        if (isPremium) {
            MaterialTheme.extendedColors.premiumGoldContainer
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        },
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = if (isPremium) "💎" else "⭐",
                fontSize = 14.sp,
            )

            Spacer(modifier = Modifier.width(4.dp))

            Text(
                text = if (isPremium) "Pro" else "Free",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = if (isPremium) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/**
 * Premium feature banner for bottom of screens
 */
@Composable
fun PremiumFeatureBanner(
    title: String,
    description: String,
    icon: ImageVector = Icons.Default.Star,
    onUpgradeClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Card(
        modifier =
        modifier
            .fillMaxWidth()
            .clickable { onUpgradeClick() },
        shape = RoundedCornerShape(16.dp),
        colors =
        CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Row(
            modifier =
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier =
                Modifier
                    .size(48.dp)
                    .background(
                        MaterialTheme.extendedColors.premiumGoldContainer,
                        CircleShape,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp),
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )

                Text(
                    text = description,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Upgrade",
                tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

/**
 * Premium features showcase grid
 */
@Composable
fun PremiumFeaturesShowcase(
    features: List<PremiumManager.PremiumFeature>,
    onFeatureClick: (String) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(features.chunked(2)) { rowFeatures ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                rowFeatures.forEach { feature ->
                    PremiumFeatureShowcaseCard(
                        feature = feature,
                        onClick = { onFeatureClick(feature.id) },
                        modifier = Modifier.weight(1f),
                    )
                }
                // Fill remaining space if odd number of items
                if (rowFeatures.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

/**
 * Individual premium feature card for showcase
 */
@Composable
fun PremiumFeatureShowcaseCard(
    feature: PremiumManager.PremiumFeature,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val categoryColor =
        when (feature.category) {
            PremiumManager.FeatureCategory.ANALYTICS -> Color(0xFF4CAF50)
            PremiumManager.FeatureCategory.OPTIMIZATION -> Color(0xFF2196F3)
            PremiumManager.FeatureCategory.CUSTOMIZATION -> Color(0xFF9C27B0)
            PremiumManager.FeatureCategory.BACKUP -> Color(0xFFFF9800)
            PremiumManager.FeatureCategory.ALERTS -> Color(0xFFE91E63)
            PremiumManager.FeatureCategory.WIDGETS -> Color(0xFF607D8B)
        }

    Card(
        modifier =
        modifier
            .height(140.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors =
        CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier =
            Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Box(
                    modifier =
                    Modifier
                        .size(40.dp)
                        .background(
                            categoryColor.copy(alpha = 0.2f),
                            CircleShape,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text =
                        when (feature.category) {
                            PremiumManager.FeatureCategory.ANALYTICS -> "📊"
                            PremiumManager.FeatureCategory.OPTIMIZATION -> "⚡"
                            PremiumManager.FeatureCategory.CUSTOMIZATION -> "🎨"
                            PremiumManager.FeatureCategory.BACKUP -> "☁️"
                            PremiumManager.FeatureCategory.ALERTS -> "🔔"
                            PremiumManager.FeatureCategory.WIDGETS -> "📱"
                        },
                        fontSize = 16.sp,
                    )
                }

                if (feature.isPremium) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.extendedColors.premiumGoldContainer,
                    ) {
                        Text(
                            text = "PRO",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        )
                    }
                }
            }

            Column {
                Text(
                    text = feature.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                Text(
                    text = feature.description,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                )
            }
        }
    }
}

/**
 * Composable to conditionally show content based on premium status
 */
@Composable
fun IfPremium(
    premiumViewModel: PremiumViewModel = hiltViewModel(),
    content: @Composable () -> Unit,
) {
    if (premiumViewModel.isPremium()) {
        content()
    }
}

/**
 * Composable to show content only for free users
 */
@Composable
fun IfFree(
    premiumViewModel: PremiumViewModel = hiltViewModel(),
    content: @Composable () -> Unit,
) {
    if (!premiumViewModel.isPremium()) {
        content()
    }
}
