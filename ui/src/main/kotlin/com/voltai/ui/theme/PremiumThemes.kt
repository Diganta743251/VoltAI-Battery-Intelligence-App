package com.voltai.ui.theme

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.sin
import kotlin.math.PI

/**
 * Premium Theme System for VoltAI
 * Multiple beautiful themes that surpass market leaders
 */

object PremiumThemes {
    
    // Theme 1: Electric Blue (Default Premium)
    val ElectricBlueLight = lightColorScheme(
        primary = Color(0xFF0066CC),
        onPrimary = Color.White,
        primaryContainer = Color(0xFFE3F2FD),
        onPrimaryContainer = Color(0xFF001D36),
        secondary = Color(0xFF6200EE),
        onSecondary = Color.White,
        secondaryContainer = Color(0xFFF3E5F5),
        onSecondaryContainer = Color(0xFF1A0B23),
        tertiary = Color(0xFF4CAF50),
        onTertiary = Color.White,
        tertiaryContainer = Color(0xFFE8F5E8),
        onTertiaryContainer = Color(0xFF0F2A0F),
        error = Color(0xFFF44336),
        onError = Color.White,
        errorContainer = Color(0xFFFFEDED),
        onErrorContainer = Color(0xFF2D0C0C),
        background = Color(0xFFF8F9FA),
        onBackground = Color(0xFF1A1C1E),
        surface = Color.White,
        onSurface = Color(0xFF1A1C1E),
        surfaceVariant = Color(0xFFE7E0EC),
        onSurfaceVariant = Color(0xFF49454F),
        outline = Color(0xFF79747E),
        outlineVariant = Color(0xFFCAC4D0),
        scrim = Color(0xFF000000),
        inverseSurface = Color(0xFF2F3033),
        inverseOnSurface = Color(0xFFF1F0F4),
        inversePrimary = Color(0xFF90CAF9)
    )
    
    val ElectricBlueDark = darkColorScheme(
        primary = Color(0xFF90CAF9),
        onPrimary = Color(0xFF001D36),
        primaryContainer = Color(0xFF004B87),
        onPrimaryContainer = Color(0xFFCCE7FF),
        secondary = Color(0xFFBB86FC),
        onSecondary = Color(0xFF1A0B23),
        secondaryContainer = Color(0xFF3700B3),
        onSecondaryContainer = Color(0xFFE1BEE7),
        tertiary = Color(0xFF81C784),
        onTertiary = Color(0xFF0F2A0F),
        tertiaryContainer = Color(0xFF2E7D32),
        onTertiaryContainer = Color(0xFFC8E6C9),
        error = Color(0xFFEF5350),
        onError = Color(0xFF2D0C0C),
        errorContainer = Color(0xFFD32F2F),
        onErrorContainer = Color(0xFFFFCDD2),
        background = Color(0xFF0F1419),
        onBackground = Color(0xFFE4E1E6),
        surface = Color(0xFF1A1C1E),
        onSurface = Color(0xFFE4E1E6),
        surfaceVariant = Color(0xFF49454F),
        onSurfaceVariant = Color(0xFFCAC4D0),
        outline = Color(0xFF938F99),
        outlineVariant = Color(0xFF49454F),
        scrim = Color(0xFF000000),
        inverseSurface = Color(0xFFE4E1E6),
        inverseOnSurface = Color(0xFF2F3033),
        inversePrimary = Color(0xFF0066CC)
    )
    
    // Theme 2: Neon Green (Gaming Style)
    val NeonGreenLight = lightColorScheme(
        primary = Color(0xFF00E676),
        onPrimary = Color.Black,
        primaryContainer = Color(0xFFE8F5E8),
        onPrimaryContainer = Color(0xFF0A2E0A),
        secondary = Color(0xFF00BCD4),
        onSecondary = Color.Black,
        secondaryContainer = Color(0xFFE0F7FA),
        onSecondaryContainer = Color(0xFF002329),
        tertiary = Color(0xFFFF5722),
        onTertiary = Color.White,
        tertiaryContainer = Color(0xFFFFE0DB),
        onTertiaryContainer = Color(0xFF2C0B00),
        background = Color(0xFFF1F8E9),
        onBackground = Color(0xFF1B1C18),
        surface = Color(0xFFF9FBE7),
        onSurface = Color(0xFF1B1C18)
    )
    
    val NeonGreenDark = darkColorScheme(
        primary = Color(0xFF69F0AE),
        onPrimary = Color(0xFF0A2E0A),
        primaryContainer = Color(0xFF00C853),
        onPrimaryContainer = Color(0xFFB9F6CA),
        secondary = Color(0xFF4DD0E1),
        onSecondary = Color(0xFF002329),
        secondaryContainer = Color(0xFF0097A7),
        onSecondaryContainer = Color(0xFFB2EBF2),
        tertiary = Color(0xFFFF8A65),
        onTertiary = Color(0xFF2C0B00),
        tertiaryContainer = Color(0xFFD84315),
        onTertiaryContainer = Color(0xFFFFCCBC),
        background = Color(0xFF0D1F0D),
        onBackground = Color(0xFFE0F2E0),
        surface = Color(0xFF1B2E1B),
        onSurface = Color(0xFFE0F2E0)
    )
    
    // Theme 3: Purple Galaxy (Premium Look)
    val PurpleGalaxyLight = lightColorScheme(
        primary = Color(0xFF7C4DFF),
        onPrimary = Color.White,
        primaryContainer = Color(0xFFF3E5F5),
        onPrimaryContainer = Color(0xFF2A0B35),
        secondary = Color(0xFFE91E63),
        onSecondary = Color.White,
        secondaryContainer = Color(0xFFFFE4E1),
        onSecondaryContainer = Color(0xFF3E0A16),
        tertiary = Color(0xFF673AB7),
        onTertiary = Color.White,
        background = Color(0xFFFAF8FF),
        onBackground = Color(0xFF1C1B1E),
        surface = Color.White,
        onSurface = Color(0xFF1C1B1E)
    )
    
    val PurpleGalaxyDark = darkColorScheme(
        primary = Color(0xFFB388FF),
        onPrimary = Color(0xFF2A0B35),
        primaryContainer = Color(0xFF5E35B1),
        onPrimaryContainer = Color(0xFFE8D5FF),
        secondary = Color(0xFFFF6B9D),
        onSecondary = Color(0xFF3E0A16),
        secondaryContainer = Color(0xFFC2185B),
        onSecondaryContainer = Color(0xFFFFDAD6),
        tertiary = Color(0xFF9C88DF),
        onTertiary = Color(0xFF2D1B33),
        background = Color(0xFF0F0A1A),
        onBackground = Color(0xFFEAE0FF),
        surface = Color(0xFF1A0F2E),
        onSurface = Color(0xFFEAE0FF)
    )
    
    // Theme 4: Sunset Orange (Warm Theme)
    val SunsetOrangeLight = lightColorScheme(
        primary = Color(0xFFFF6F00),
        onPrimary = Color.White,
        primaryContainer = Color(0xFFFFE0B2),
        onPrimaryContainer = Color(0xFF2E1800),
        secondary = Color(0xFFFF5722),
        onSecondary = Color.White,
        secondaryContainer = Color(0xFFFFE0DB),
        onSecondaryContainer = Color(0xFF2C0B00),
        tertiary = Color(0xFFFFC107),
        onTertiary = Color.Black,
        background = Color(0xFFFFF8E1),
        onBackground = Color(0xFF1F1A00),
        surface = Color(0xFFFFFBF0),
        onSurface = Color(0xFF1F1A00)
    )
    
    val SunsetOrangeDark = darkColorScheme(
        primary = Color(0xFFFFB74D),
        onPrimary = Color(0xFF2E1800),
        primaryContainer = Color(0xFFE65100),
        onPrimaryContainer = Color(0xFFFFCC80),
        secondary = Color(0xFFFF8A65),
        onSecondary = Color(0xFF2C0B00),
        secondaryContainer = Color(0xFFD84315),
        onSecondaryContainer = Color(0xFFFFCCBC),
        tertiary = Color(0xFFFFD54F),
        onTertiary = Color(0xFF1F1500),
        background = Color(0xFF1A0F00),
        onBackground = Color(0xFFFFF0CC),
        surface = Color(0xFF2E1F00),
        onSurface = Color(0xFFFFF0CC)
    )
    
    // Theme 5: Ocean Blue (Professional)
    val OceanBlueLight = lightColorScheme(
        primary = Color(0xFF2196F3),
        onPrimary = Color.White,
        primaryContainer = Color(0xFFE1F5FE),
        onPrimaryContainer = Color(0xFF0D47A1),
        secondary = Color(0xFF00BCD4),
        onSecondary = Color.Black,
        secondaryContainer = Color(0xFFE0F7FA),
        onSecondaryContainer = Color(0xFF006064),
        tertiary = Color(0xFF009688),
        onTertiary = Color.White,
        background = Color(0xFFF0F9FF),
        onBackground = Color(0xFF0A1929),
        surface = Color(0xFFFAFDFF),
        onSurface = Color(0xFF0A1929)
    )
    
    val OceanBlueDark = darkColorScheme(
        primary = Color(0xFF64B5F6),
        onPrimary = Color(0xFF0D47A1),
        primaryContainer = Color(0xFF1976D2),
        onPrimaryContainer = Color(0xFFBBDEFB),
        secondary = Color(0xFF4DD0E1),
        onSecondary = Color(0xFF006064),
        secondaryContainer = Color(0xFF0097A7),
        onSecondaryContainer = Color(0xFFB2EBF2),
        tertiary = Color(0xFF4DB6AC),
        onTertiary = Color(0xFF00251A),
        background = Color(0xFF001829),
        onBackground = Color(0xFFB8E5FF),
        surface = Color(0xFF0F2B3C),
        onSurface = Color(0xFFB8E5FF)
    )

    // Theme 6: E-Paper (Monochrome, eye comfort, minimal animations)
    val EPaperLight = lightColorScheme(
        primary = Color(0xFF000000),
        onPrimary = Color(0xFFFFFFFF),
        background = Color(0xFFF5F5F2),
        onBackground = Color(0xFF111111),
        surface = Color(0xFFFFFFFF),
        onSurface = Color(0xFF111111),
        secondary = Color(0xFF000000),
        onSecondary = Color(0xFFFFFFFF),
        surfaceVariant = Color(0xFFE0E0E0),
        onSurfaceVariant = Color(0xFF444444),
        outline = Color(0xFFBDBDBD)
    )

    val EPaperDark = darkColorScheme(
        primary = Color(0xFFFFFFFF),
        onPrimary = Color(0xFF000000),
        background = Color(0xFF0C0C0C),
        onBackground = Color(0xFFE6E6E6),
        surface = Color(0xFF121212),
        onSurface = Color(0xFFE6E6E6),
        secondary = Color(0xFFE0E0E0),
        onSecondary = Color(0xFF000000),
        surfaceVariant = Color(0xFF2A2A2A),
        onSurfaceVariant = Color(0xFFB0B0B0),
        outline = Color(0xFF555555)
    )
}

/**
 * Premium Animation System
 */
object PremiumAnimations {
    
    // Battery Level Animation
    fun batteryLevelAnimation(
        targetLevel: Float,
        durationMillis: Int = 1000
    ): AnimationSpec<Float> {
        return tween(
            durationMillis = durationMillis,
            easing = EaseInOutCubic
        )
    }
    
    // Charging Pulse Animation
    fun chargingPulseAnimation(): InfiniteRepeatableSpec<Float> {
        return infiniteRepeatable(
            animation = tween(
                durationMillis = 1500,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Reverse
        )
    }
    
    // Card Entrance Animation
    fun cardEntranceAnimation(
        delayMillis: Int = 0
    ): EnterTransition {
        return slideInVertically(
            initialOffsetY = { it },
            animationSpec = tween(
                durationMillis = 600,
                delayMillis = delayMillis,
                easing = EaseOutCubic
            )
        ) + fadeIn(
            animationSpec = tween(
                durationMillis = 400,
                delayMillis = delayMillis + 200
            )
        )
    }
    
    // Floating Action Animation
    fun floatingActionAnimation(): AnimationSpec<Float> {
        return infiniteRepeatable(
            animation = tween(
                durationMillis = 2000,
                easing = EaseInOutSine
            ),
            repeatMode = RepeatMode.Reverse
        )
    }
    
    // Temperature Warning Animation
    fun temperatureWarningAnimation(): InfiniteRepeatableSpec<Float> {
        return infiniteRepeatable(
            animation = tween(
                durationMillis = 500,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Reverse
        )
    }
    
    // Shimmer Loading Animation
    fun shimmerAnimation(): InfiniteRepeatableSpec<Float> {
        return infiniteRepeatable(
            animation = tween(
                durationMillis = 1200,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        )
    }
}

/**
 * Dynamic Gradient Generator
 */
object PremiumGradients {
    
    fun chargingGradient(isCharging: Boolean): Brush {
        return if (isCharging) {
            Brush.linearGradient(
                colors = listOf(
                    Color(0xFF4CAF50),
                    Color(0xFF8BC34A),
                    Color(0xFFCDDC39)
                )
            )
        } else {
            Brush.linearGradient(
                colors = listOf(
                    Color(0xFF2196F3),
                    Color(0xFF03A9F4),
                    Color(0xFF00BCD4)
                )
            )
        }
    }
    
    fun batteryLevelGradient(level: Int): Brush {
        val colors = when {
            level <= 20 -> listOf(Color(0xFFF44336), Color(0xFFE91E63))
            level <= 50 -> listOf(Color(0xFFFF9800), Color(0xFFFF5722))
            level <= 80 -> listOf(Color(0xFF4CAF50), Color(0xFF8BC34A))
            else -> listOf(Color(0xFF2196F3), Color(0xFF03A9F4))
        }
        
        return Brush.linearGradient(colors = colors)
    }
    
    fun temperatureGradient(temperature: Float): Brush {
        val colors = when {
            temperature >= 45f -> listOf(Color(0xFFF44336), Color(0xFFD32F2F))
            temperature >= 40f -> listOf(Color(0xFFFF9800), Color(0xFFE65100))
            temperature >= 35f -> listOf(Color(0xFFFFEB3B), Color(0xFFFBC02D))
            else -> listOf(Color(0xFF4CAF50), Color(0xFF388E3C))
        }
        
        return Brush.radialGradient(colors = colors)
    }
    
    fun backgroundGradient(isDarkTheme: Boolean): Brush {
        return if (isDarkTheme) {
            Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF0F1419),
                    Color(0xFF1A1C1E),
                    Color(0xFF252831)
                )
            )
        } else {
            Brush.verticalGradient(
                colors = listOf(
                    Color(0xFFF8F9FA),
                    Color(0xFFE8F4FD),
                    Color(0xFFE3F2FD)
                )
            )
        }
    }
}

// Motion preference: true means minimize animations (used by E-Paper theme)
val LocalReducedMotion = compositionLocalOf { false }

/**
 * Premium Particle Effects
 */
@Composable
fun ParticleEffect(
    isActive: Boolean,
    particleCount: Int = 20
) {
    val infiniteTransition = rememberInfiniteTransition(label = "particle_effect")
    
    if (isActive) {
        repeat(particleCount) { index ->
            val animationDelay = (index * 100).toLong()
            
            val particleAnimation by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = 2000,
                        delayMillis = animationDelay.toInt(),
                        easing = LinearEasing
                    ),
                    repeatMode = RepeatMode.Restart
                ),
                label = "particle_$index"
            )
        }
    }
}

/**
 * Theme Selector Component
 */
@Composable
fun ThemeSelector(
    currentTheme: String,
    onThemeSelected: (String) -> Unit
) {
    val themes = listOf(
        "Electric Blue" to "electric_blue",
        "Neon Green" to "neon_green", 
        "Purple Galaxy" to "purple_galaxy",
        "Sunset Orange" to "sunset_orange",
        "Ocean Blue" to "ocean_blue",
        "E-Paper" to "e_paper"
    )
    
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(16.dp)
    ) {
        items(themes) { (name, id) ->
            ThemeOption(
                name = name,
                isSelected = currentTheme == id,
                onClick = { onThemeSelected(id) }
            )
        }
    }
}

@Composable
private fun ThemeOption(
    name: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(120.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) 
                MaterialTheme.colorScheme.primaryContainer
            else 
                MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 8.dp else 2.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Theme color preview
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        getThemePreviewGradient(name),
                        CircleShape
                    )
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = name,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

private fun getThemePreviewGradient(themeName: String): Brush {
    return when (themeName) {
        "Electric Blue" -> Brush.linearGradient(
            listOf(Color(0xFF0066CC), Color(0xFF2196F3))
        )
        "Neon Green" -> Brush.linearGradient(
            listOf(Color(0xFF00E676), Color(0xFF4CAF50))
        )
        "Purple Galaxy" -> Brush.linearGradient(
            listOf(Color(0xFF7C4DFF), Color(0xFFE91E63))
        )
        "Sunset Orange" -> Brush.linearGradient(
            listOf(Color(0xFFFF6F00), Color(0xFFFFC107))
        )
        "Ocean Blue" -> Brush.linearGradient(
            listOf(Color(0xFF2196F3), Color(0xFF00BCD4))
        )
        "E-Paper" -> Brush.linearGradient(
            listOf(Color(0xFF111111), Color(0xFFEDEDE9))
        )
        else -> Brush.linearGradient(
            listOf(Color.Gray, Color.LightGray)
        )
    }
}

/**
 * Main Premium Theme Composable
 */
@Composable
fun VoltAIPremiumTheme(
    selectedTheme: String = "electric_blue",
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val isEPaper = selectedTheme == "e_paper"
    val colorScheme = when {
        dynamicColor && !isEPaper && android.os.Build.VERSION.SDK_INT >= 31 -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context)
            else dynamicLightColorScheme(context)
        }
        else -> {
            when (selectedTheme) {
                "electric_blue" -> if (darkTheme) PremiumThemes.ElectricBlueDark else PremiumThemes.ElectricBlueLight
                "neon_green" -> if (darkTheme) PremiumThemes.NeonGreenDark else PremiumThemes.NeonGreenLight
                "purple_galaxy" -> if (darkTheme) PremiumThemes.PurpleGalaxyDark else PremiumThemes.PurpleGalaxyLight
                "sunset_orange" -> if (darkTheme) PremiumThemes.SunsetOrangeDark else PremiumThemes.SunsetOrangeLight
                "ocean_blue" -> if (darkTheme) PremiumThemes.OceanBlueDark else PremiumThemes.OceanBlueLight
                "e_paper" -> if (darkTheme) PremiumThemes.EPaperDark else PremiumThemes.EPaperLight
                else -> if (darkTheme) PremiumThemes.ElectricBlueDark else PremiumThemes.ElectricBlueLight
            }
        }
    }

    CompositionLocalProvider(LocalReducedMotion provides isEPaper) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = MaterialTheme.typography,
            content = content
        )
    }
}
