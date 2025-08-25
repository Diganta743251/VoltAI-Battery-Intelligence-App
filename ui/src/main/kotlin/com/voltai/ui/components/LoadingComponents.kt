package com.voltai.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp

@Composable
fun ShimmerEffect(
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateX by transition.animateFloat(
        initialValue = -1000f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_translate"
    )
    
    val shimmerColors = listOf(
        MaterialTheme.colorScheme.surface.copy(alpha = 0.3f),
        MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
        MaterialTheme.colorScheme.surface.copy(alpha = 0.3f)
    )
    
    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(translateX, 0f),
        end = Offset(translateX + 1000f, 0f)
    )
    
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(brush = brush)
    )
}

@Composable
fun SkeletonCard(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Main content skeleton
            ShimmerEffect(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            )
            
            // Stats row skeleton
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                repeat(3) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ShimmerEffect(
                            modifier = Modifier
                                .size(24.dp)
                        )
                        ShimmerEffect(
                            modifier = Modifier
                                .width(60.dp)
                                .height(16.dp)
                        )
                        ShimmerEffect(
                            modifier = Modifier
                                .width(40.dp)
                                .height(12.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SkeletonBatteryRing(
    modifier: Modifier = Modifier,
    size: Int = 120
) {
    Box(
        modifier = modifier.size(size.dp),
        contentAlignment = Alignment.Center
    ) {
        ShimmerEffect(
            modifier = Modifier
                .size(size.dp)
                .clip(RoundedCornerShape(size.dp / 2))
        )
        
        // Center text skeleton
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            ShimmerEffect(
                modifier = Modifier
                    .width(40.dp)
                    .height(20.dp)
            )
            ShimmerEffect(
                modifier = Modifier
                    .width(60.dp)
                    .height(12.dp)
            )
        }
    }
}

@Composable
fun SkeletonGraph(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Graph title skeleton
        ShimmerEffect(
            modifier = Modifier
                .width(120.dp)
                .height(16.dp)
        )
        
        // Graph area skeleton
        ShimmerEffect(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
        )
        
        // Graph legend skeleton
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            repeat(3) {
                ShimmerEffect(
                    modifier = Modifier
                        .width(60.dp)
                        .height(12.dp)
                )
            }
        }
    }
}

@Composable
fun SkeletonToolsGrid(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Tools title skeleton
        ShimmerEffect(
            modifier = Modifier
                .width(100.dp)
                .height(20.dp)
        )
        
        // Tools grid skeleton
        repeat(2) { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                repeat(3) { col ->
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ShimmerEffect(
                                modifier = Modifier.size(32.dp)
                            )
                            ShimmerEffect(
                                modifier = Modifier
                                    .width(60.dp)
                                    .height(12.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PulseEffect(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )
    
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )
    
    Box(
        modifier = modifier
            .background(
                color = color.copy(alpha = alpha),
                shape = RoundedCornerShape(50)
            )
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
    )
}