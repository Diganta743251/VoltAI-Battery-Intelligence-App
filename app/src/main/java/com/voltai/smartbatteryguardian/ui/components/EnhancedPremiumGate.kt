package com.voltai.smartbatteryguardian.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.voltai.smartbatteryguardian.ui.theme.extendedColors

@Composable
fun EnhancedPremiumGate(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    onUpgradeClick: () -> Unit,
    onLearnMoreClick: () -> Unit,
    featureName: String,
    featureDescription: String,
    featureIcon: String = "💎",
) {
    AnimatedVisibility(
        visible = isVisible,
        enter =
        fadeIn(animationSpec = tween(300)) +
            scaleIn(
                animationSpec = tween(300),
                initialScale = 0.8f,
            ),
        exit =
        fadeOut(animationSpec = tween(200)) +
            scaleOut(
                animationSpec = tween(200),
                targetScale = 0.8f,
            ),
    ) {
        Dialog(
            onDismissRequest = onDismiss,
            properties =
            DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = true,
            ),
        ) {
            PremiumGateContent(
                onDismiss = onDismiss,
                onUpgradeClick = onUpgradeClick,
                onLearnMoreClick = onLearnMoreClick,
                featureName = featureName,
                featureDescription = featureDescription,
                featureIcon = featureIcon,
            )
        }
    }
}

@Composable
private fun PremiumGateContent(
    onDismiss: () -> Unit,
    onUpgradeClick: () -> Unit,
    onLearnMoreClick: () -> Unit,
    featureName: String,
    featureDescription: String,
    featureIcon: String,
) {
    // Static, battery-efficient premium gate design
    Card(
        modifier =
        Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(24.dp),
        colors =
        CardDefaults.cardColors(
            containerColor = Color.White,
        ),
        elevation =
        CardDefaults.cardElevation(
            defaultElevation = 16.dp,
        ),
    ) {
        Box {
            // Background gradient overlay
            Box(
                modifier =
                Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .background(
                        brush =
                        Brush.verticalGradient(
                            colors =
                            listOf(
                                Color(0xFF6366F1).copy(alpha = 0.1f),
                                Color(0xFF8B5CF6).copy(alpha = 0.05f),
                            ),
                        ),
                    ),
            )

            // Static premium accent bar
            Box(
                modifier =
                Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(
                        MaterialTheme.extendedColors.premiumGold.copy(alpha = 0.6f),
                    ),
            )

            Column(
                modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // Close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                // Premium icon with animation
                Box(
                    modifier =
                    Modifier
                        .size(80.dp)
                        .background(
                            brush =
                            Brush.radialGradient(
                                colors =
                                listOf(
                                    MaterialTheme.extendedColors.premiumGold,
                                    MaterialTheme.extendedColors.premiumAmber,
                                ),
                            ),
                            CircleShape,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = featureIcon,
                        fontSize = 32.sp,
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Premium Feature",
                    fontSize = 14.sp,
                    color = Color(0xFF6366F1),
                    fontWeight = FontWeight.Medium,
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = featureName,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    textAlign = TextAlign.Center,
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = featureDescription,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    lineHeight = 24.sp,
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Features preview
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    PremiumFeatureItem("🚀 Advanced AI Analytics")
                    PremiumFeatureItem("⚡ Smart Optimization")
                    PremiumFeatureItem("📊 Detailed Insights")
                    PremiumFeatureItem("🎯 Personalized Recommendations")
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Action buttons
                Button(
                    onClick = onUpgradeClick,
                    modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors =
                    ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF6366F1),
                    ),
                ) {
                    Text(
                        text = "🚀 Upgrade to Premium",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                TextButton(
                    onClick = onLearnMoreClick,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = "Learn More About Premium",
                        fontSize = 14.sp,
                        color = Color(0xFF6366F1),
                    )
                }
            }
        }
    }
}

@Composable
private fun PremiumFeatureItem(text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = Color(0xFF4CAF50),
            modifier = Modifier.size(16.dp),
        )
        Text(
            text = text,
            fontSize = 14.sp,
            color = Color.Black.copy(alpha = 0.8f),
        )
    }
}

@Composable
fun PremiumFeatureBanner(
    isVisible: Boolean,
    onUpgradeClick: () -> Unit,
    onDismiss: () -> Unit,
    title: String = "Unlock Premium Features",
    description: String = "Get AI-powered insights and advanced analytics",
) {
    AnimatedVisibility(
        visible = isVisible,
        enter =
        slideInVertically(
            initialOffsetY = { -it },
            animationSpec = tween(300),
        ) + fadeIn(),
        exit =
        slideOutVertically(
            targetOffsetY = { -it },
            animationSpec = tween(200),
        ) + fadeOut(),
    ) {
        Card(
            modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            colors =
            CardDefaults.cardColors(
                containerColor = Color(0xFF6366F1),
            ),
            shape = RoundedCornerShape(12.dp),
        ) {
            Row(
                modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                ) {
                    Text(
                        text = title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                    )
                    Text(
                        text = description,
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.8f),
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TextButton(
                        onClick = onUpgradeClick,
                        colors =
                        ButtonDefaults.textButtonColors(
                            contentColor = Color.White,
                        ),
                    ) {
                        Text("Upgrade", fontWeight = FontWeight.Bold)
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(24.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.size(16.dp),
                        )
                    }
                }
            }
        }
    }
}
