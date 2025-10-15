package com.voltai.smartbatteryguardian.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.voltai.smartbatteryguardian.R
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WelcomeScreen(
    onGetStarted: () -> Unit = {},
    onSkip: () -> Unit = {},
) {
    val context = LocalContext.current
    // val firstTimeUserManager: FirstTimeUserManager = hiltViewModel()
    var currentPage by remember { mutableIntStateOf(0) }
    var isVisible by remember { mutableStateOf(false) }

    // Animation for entrance
    LaunchedEffect(Unit) {
        delay(300)
        isVisible = true
    }

    // Auto-advance pages
    LaunchedEffect(currentPage) {
        if (currentPage < 3) { // Fixed size instead of welcomePages.size
            delay(4000) // Auto-advance after 4 seconds
            currentPage = (currentPage + 1) % 4
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
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
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                                MaterialTheme.colorScheme.surface,
                                MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.2f),
                            ),
                        ),
                    ),
            )

            AnimatedVisibility(
                visible = isVisible,
                enter =
                fadeIn(animationSpec = tween(800)) +
                    slideInVertically(
                        initialOffsetY = { it / 4 },
                        animationSpec = tween(800),
                    ),
            ) {
                Column(
                    modifier =
                    Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Spacer(modifier = Modifier.height(48.dp))

                    // App Logo and Title
                    WelcomeHeader()

                    Spacer(modifier = Modifier.height(48.dp))

                    // Welcome Pages
                    WelcomePageContent(
                        page = createWelcomePages()[currentPage],
                        isVisible = isVisible,
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // Page Indicators
                    PageIndicators(
                        totalPages = 4,
                        currentPage = currentPage,
                        onPageClick = { currentPage = it },
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    // Action Buttons
                    WelcomeActions(
                        currentPage = currentPage,
                        totalPages = 4,
                        onNext = { currentPage = minOf(currentPage + 1, 3) },
                        onGetStarted = {
                            // firstTimeUserManager.markOnboardingCompleted()
                            // firstTimeUserManager.startDataCollection()
                            onGetStarted()
                        },
                        onSkip = {
                            // firstTimeUserManager.markOnboardingCompleted()
                            // firstTimeUserManager.startDataCollection()
                            onSkip()
                        },
                    )

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
private fun WelcomeHeader() {
    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec =
        spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow,
        ),
        label = "logo_scale",
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // App Icon/Logo placeholder
        Card(
            modifier =
            Modifier
                .size(80.dp)
                .scale(scale),
            shape = RoundedCornerShape(20.dp),
            colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primary,
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = stringResource(id = R.string.cd_voltai_logo),
                    modifier = Modifier.size(40.dp),
                    tint = MaterialTheme.colorScheme.onPrimary,
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(id = R.string.welcome_header_title),
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
        )

        Text(
            text = stringResource(id = R.string.welcome_header_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
        )
    }
}

@Composable
private fun WelcomePageContent(
    page: WelcomePage,
    isVisible: Boolean,
) {
    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(600),
        label = "content_alpha",
    )

    Card(
        modifier =
        Modifier
            .fillMaxWidth()
            .alpha(alpha),
        shape = RoundedCornerShape(20.dp),
        colors =
        CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Icon
            Card(
                modifier = Modifier.size(64.dp),
                shape = RoundedCornerShape(16.dp),
                colors =
                CardDefaults.cardColors(
                    containerColor = page.iconBackground,
                ),
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = page.icon,
                        contentDescription = page.title,
                        modifier = Modifier.size(32.dp),
                        tint = page.iconTint,
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Title
            Text(
                text = page.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Description
            Text(
                text = page.description,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                lineHeight = 20.sp,
            )
        }
    }
}

@Composable
private fun PageIndicators(
    totalPages: Int,
    currentPage: Int,
    onPageClick: (Int) -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(totalPages) { index ->
            val isSelected = index == currentPage
            val scale by animateFloatAsState(
                targetValue = if (isSelected) 1.2f else 1f,
                animationSpec = spring(),
                label = "indicator_scale",
            )

            Box(
                modifier =
                Modifier
                    .size(if (isSelected) 12.dp else 8.dp)
                    .scale(scale)
                    .background(
                        color =
                        if (isSelected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        },
                        shape = RoundedCornerShape(50),
                    ),
            )
        }
    }
}

@Composable
private fun WelcomeActions(
    currentPage: Int,
    totalPages: Int,
    onNext: () -> Unit,
    onGetStarted: () -> Unit,
    onSkip: () -> Unit,
) {
    val isLastPage = currentPage == totalPages - 1

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Skip button
        if (!isLastPage) {
            TextButton(
                onClick = onSkip,
                modifier = Modifier.weight(1f),
            ) {
                Text(stringResource(id = R.string.action_skip))
            }
        }

        // Main action button
        Button(
            onClick = if (isLastPage) onGetStarted else onNext,
            modifier = Modifier.weight(2f),
            shape = RoundedCornerShape(12.dp),
        ) {
            Text(
                text =
                if (isLastPage) {
                    stringResource(
                        id = R.string.action_get_started,
                    )
                } else {
                    stringResource(id = R.string.action_next)
                },
                modifier = Modifier.padding(vertical = 4.dp),
            )
        }
    }
}

// Welcome page data
data class WelcomePage(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val iconBackground: androidx.compose.ui.graphics.Color,
    val iconTint: androidx.compose.ui.graphics.Color,
)

@Composable
private fun createWelcomePages(): List<WelcomePage> {
    return listOf(
        WelcomePage(
            title = stringResource(id = R.string.welcome_page1_title),
            description = stringResource(id = R.string.welcome_page1_desc),
            icon = Icons.Default.Home,
            iconBackground = MaterialTheme.colorScheme.primaryContainer,
            iconTint = MaterialTheme.colorScheme.onPrimaryContainer,
        ),
        WelcomePage(
            title = stringResource(id = R.string.welcome_page2_title),
            description = stringResource(id = R.string.welcome_page2_desc),
            icon = Icons.Default.Check,
            iconBackground = MaterialTheme.colorScheme.secondaryContainer,
            iconTint = MaterialTheme.colorScheme.onSecondaryContainer,
        ),
        WelcomePage(
            title = stringResource(id = R.string.welcome_page3_title),
            description = stringResource(id = R.string.welcome_page3_desc),
            icon = Icons.Default.Settings,
            iconBackground = MaterialTheme.colorScheme.tertiaryContainer,
            iconTint = MaterialTheme.colorScheme.onTertiaryContainer,
        ),
        WelcomePage(
            title = stringResource(id = R.string.welcome_page4_title),
            description = stringResource(id = R.string.welcome_page4_desc),
            icon = Icons.Default.Info,
            iconBackground = MaterialTheme.colorScheme.primaryContainer,
            iconTint = MaterialTheme.colorScheme.onPrimaryContainer,
        ),
    )
}
