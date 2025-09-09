package com.voltai.smartbatteryguardian.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.pager.*
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class PremiumFeatureShowcase(
    val title: String,
    val description: String,
    val icon: String,
    val color: Color,
    val benefits: List<String>
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PremiumOnboardingScreen(
    onUpgradeClick: () -> Unit = {},
    onSkipClick: () -> Unit = {},
    onBackClick: () -> Unit = {}
) {
    val features = remember { getPremiumFeatures() }
    val pagerState = rememberPagerState(pageCount = { features.size })

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1A1A2E),
                        Color(0xFF16213E),
                        Color(0xFF0F3460)
                    )
                )
            )
    ) {
        // Background particles animation
        PremiumBackgroundAnimation()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                Text(
                    text = "VoltAI Premium",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                TextButton(onClick = onSkipClick) {
                    Text(
                        text = "Skip",
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Premium badge with animation
            Box(
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                PremiumBadgeAnimation()
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Unlock Advanced Battery Intelligence",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Experience AI-powered optimization and professional analytics",
                fontSize = 16.sp,
                color = Color.White.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Feature pager
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { page ->
                PremiumFeatureCard(
                    feature = features[page],
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Page indicators
            Row(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                repeat(features.size) { index ->
                    Box(
                        modifier = Modifier
                            .size(if (pagerState.currentPage == index) 12.dp else 8.dp)
                            .background(
                                if (pagerState.currentPage == index) {
                                    Color.White
                                } else {
                                    Color.White.copy(alpha = 0.5f)
                                },
                                CircleShape
                            )
                            .animateContentSize()
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Pricing
            PricingSection(onUpgradeClick = onUpgradeClick)
        }
    }
}

@Composable
private fun PremiumBackgroundAnimation() {
    val infiniteTransition = rememberInfiniteTransition(label = "background")

    repeat(15) { index ->
        val animationDelay = (index * 200)
        val xOffset by infiniteTransition.animateFloat(
            initialValue = -50f,
            targetValue = 1000f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = 8000 + (index * 500),
                    delayMillis = animationDelay,
                    easing = LinearEasing
                ),
                repeatMode = RepeatMode.Restart
            ),
            label = "x$index"
        )

        val yOffset by infiniteTransition.animateFloat(
            initialValue = (index * 60f),
            targetValue = (index * 60f) + 20f,
            animationSpec = infiniteRepeatable(
                animation = tween(2000, easing = EaseInOutSine),
                repeatMode = RepeatMode.Reverse
            ),
            label = "y$index"
        )

        Box(
            modifier = Modifier
                .offset(x = xOffset.dp, y = yOffset.dp)
                .size((4 + index % 6).dp)
                .background(
                    Color.White.copy(alpha = 0.1f + (index % 3) * 0.05f),
                    CircleShape
                )
        )
    }
}

@Composable
private fun PremiumBadgeAnimation() {
    val rotation by rememberInfiniteTransition(label = "badge").animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    val scale by rememberInfiniteTransition(label = "scale").animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(
        modifier = Modifier
            .size(80.dp)
            .graphicsLayer {
                rotationZ = rotation
                scaleX = scale
                scaleY = scale
            }
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFFFD700),
                        Color(0xFFFFA000)
                    )
                ),
                CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "💎",
            fontSize = 32.sp
        )
    }
}

@Composable
private fun PremiumFeatureCard(
    feature: PremiumFeatureShowcase,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.padding(16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.1f)
        ),
        border = BorderStroke(
            1.dp,
            Color.White.copy(alpha = 0.2f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .background(
                        feature.color.copy(alpha = 0.2f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = feature.icon,
                    fontSize = 48.sp
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = feature.title,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = feature.description,
                    fontSize = 16.sp,
                    color = Color.White.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center,
                    lineHeight = 24.sp
                )
            }

            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(feature.benefits) { benefit ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(20.dp)
                        )

                        Text(
                            text = benefit,
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.9f),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PricingSection(onUpgradeClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Special offer badge
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFFFF6B35)
        ) {
            Text(
                text = "🔥 Limited Time: 50% OFF",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        // Pricing card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White.copy(alpha = 0.15f)
            ),
            border = BorderStroke(2.dp, Color(0xFFFFD700).copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "$2.99",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "/month",
                        fontSize = 16.sp,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }

                Text(
                    text = "Was $5.99/month",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.6f),
                    textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                )
            }
        }

        // CTA Button
        Button(
            onClick = onUpgradeClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFFD700)
            )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "🚀 Unlock Premium Features",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
        }

        Text(
            text = "Cancel anytime • No commitment • 7-day free trial",
            fontSize = 12.sp,
            color = Color.White.copy(alpha = 0.6f),
            textAlign = TextAlign.Center
        )
    }
}

private fun getPremiumFeatures(): List<PremiumFeatureShowcase> {
    return listOf(
        PremiumFeatureShowcase(
            title = "AI-Powered Health Analytics",
            description = "Get deep insights into your battery's molecular composition and predictive health analysis",
            icon = "🧠",
            color = Color(0xFF6366F1),
            benefits = listOf(
                "Advanced health scoring with 95% accuracy",
                "Predictive battery lifespan analysis",
                "Molecular degradation tracking",
                "Personalized optimization recommendations"
            )
        ),
        PremiumFeatureShowcase(
            title = "Smart Charging Optimization",
            description = "Intelligent charging algorithms that learn your patterns to extend battery life by 40%",
            icon = "⚡",
            color = Color(0xFF8B5CF6),
            benefits = listOf(
                "Adaptive charging schedules",
                "Temperature-optimized charging",
                "Cost-saving charging windows",
                "Efficiency monitoring and alerts"
            )
        ),
        PremiumFeatureShowcase(
            title = "Real-time Monitoring",
            description = "Professional-grade battery monitoring with live metrics and advanced diagnostics",
            icon = "📊",
            color = Color(0xFF06B6D4),
            benefits = listOf(
                "Live voltage, current, and power tracking",
                "Advanced diagnostic testing suite",
                "Historical trend analysis",
                "Export detailed reports"
            )
        ),
        PremiumFeatureShowcase(
            title = "Premium Support & Features",
            description = "Unlock all advanced features with priority support and exclusive access",
            icon = "💎",
            color = Color(0xFFEF4444),
            benefits = listOf(
                "Priority customer support",
                "Early access to new features",
                "Advanced customization options",
                "Cloud backup and sync"
            )
        )
    )
}
