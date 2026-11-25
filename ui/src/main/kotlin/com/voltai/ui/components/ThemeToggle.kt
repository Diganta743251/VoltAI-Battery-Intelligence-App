package com.voltai.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.voltai.ui.theme.LocalThemeToggleAction
import com.voltai.ui.theme.ThemeMode
import com.voltai.ui.theme.rememberThemeState

/**
 * DataStore-connected ThemeToggle that automatically binds to ThemePreferences DataStore.
 */
@Composable
fun ThemeToggle(
    modifier: Modifier = Modifier
) {
    val themeState = rememberThemeState()
    ThemeToggle(
        isDarkTheme = themeState.isDark,
        onToggle = themeState.onToggleTheme,
        modifier = modifier
    )
}

/**
 * Animated Pill Theme Toggle between Light and Dark modes.
 */
@Composable
fun ThemeToggle(
    isDarkTheme: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor by animateColorAsState(
        targetValue = if (isDarkTheme) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.primaryContainer,
        animationSpec = tween(300),
        label = "toggleBackground"
    )

    val thumbBg by animateColorAsState(
        targetValue = if (isDarkTheme) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onPrimaryContainer,
        animationSpec = tween(300),
        label = "thumbBackground"
    )

    val thumbIconTint by animateColorAsState(
        targetValue = if (isDarkTheme) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primaryContainer,
        animationSpec = tween(300),
        label = "thumbIconTint"
    )

    val thumbOffset by animateDpAsState(
        targetValue = if (isDarkTheme) 28.dp else 0.dp,
        animationSpec = tween(300),
        label = "thumbOffset"
    )

    Box(
        modifier = modifier
            .size(width = 64.dp, height = 48.dp) // Ensure minimum 48dp touch target
            .semantics {
                contentDescription = if (isDarkTheme) "Switch to light mode" else "Switch to dark mode"
                role = Role.Switch
            }
            .clickable(onClick = onToggle)
            .testTag("theme_toggle_switch"),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .width(56.dp)
                .height(30.dp)
                .clip(RoundedCornerShape(15.dp))
                .background(bgColor)
                .padding(3.dp)
        ) {
            Box(
                modifier = Modifier
                    .offset(x = thumbOffset)
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(thumbBg),
                contentAlignment = Alignment.Center
            ) {
                if (isDarkTheme) {
                    MoonIcon(
                        tint = thumbIconTint,
                        modifier = Modifier.size(13.dp)
                    )
                } else {
                    SunIcon(
                        tint = thumbIconTint,
                        modifier = Modifier.size(13.dp)
                    )
                }
            }
        }
    }
}

/**
 * 3-Way Segmented Control for System / Light / Dark modes backed by Preferences DataStore.
 */
@Composable
fun ThemeModeSelector(
    modifier: Modifier = Modifier
) {
    val themeState = rememberThemeState()

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("theme_mode_selector"),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier
                .padding(4.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            ThemeMode.values().forEach { mode ->
                val selected = themeState.themeMode == mode
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { themeState.onSetThemeMode(mode) }
                        .testTag("theme_option_${mode.name.lowercase()}"),
                    shape = RoundedCornerShape(12.dp),
                    color = if (selected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = mode.title,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                            color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

/**
 * Material 3 Setting Card for Light/Dark Theme management.
 * Persists user preference via DataStore in the ':ui' module and automatically updates app MaterialTheme.
 */
@Composable
fun ThemeToggleSettingCard(
    modifier: Modifier = Modifier
) {
    val themeState = rememberThemeState()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("theme_toggle_setting_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(42.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            if (themeState.isDark) {
                                MoonIcon(
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(20.dp)
                                )
                            } else {
                                SunIcon(
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "Theme Appearance",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = when (themeState.themeMode) {
                                ThemeMode.SYSTEM -> "Following system theme (${if (themeState.isDark) "Dark" else "Light"})"
                                ThemeMode.LIGHT -> "Light theme active"
                                ThemeMode.DARK -> "Dark theme active"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Quick toggle pill
                ThemeToggle(
                    isDarkTheme = themeState.isDark,
                    onToggle = themeState.onToggleTheme
                )
            }

            // 3-Way Segmented Mode Selector
            ThemeModeSelector()

            // Dynamic Color Toggle (Android 12+)
            if (android.os.Build.VERSION.SDK_INT >= 31) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Dynamic Color (Material You)",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Derive accent palette from device wallpaper",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Switch(
                        checked = themeState.dynamicColor,
                        onCheckedChange = { themeState.onSetDynamicColor(it) },
                        modifier = Modifier.testTag("dynamic_color_switch")
                    )
                }
            }
        }
    }
}

/**
 * TopBar Icon button for one-tap Light/Dark theme switching backed by DataStore.
 */
@Composable
fun ThemeToggleIconButton(
    modifier: Modifier = Modifier
) {
    val themeState = rememberThemeState()

    IconButton(
        onClick = themeState.onToggleTheme,
        modifier = modifier.testTag("theme_toggle_icon_button")
    ) {
        if (themeState.isDark) {
            SunIcon(
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp)
            )
        } else {
            MoonIcon(
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}