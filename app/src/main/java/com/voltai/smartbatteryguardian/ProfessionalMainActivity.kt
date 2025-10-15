package com.voltai.smartbatteryguardian

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.focusable
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.voltai.smartbatteryguardian.screens.*
import com.voltai.smartbatteryguardian.ui.theme.VoltAITheme
import com.voltai.smartbatteryguardian.viewmodels.ThemeViewModel
import com.voltai.ui.theme.LocalReducedMotion
import com.voltai.ui.theme.VoltAIPremiumTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProfessionalMainActivity : ComponentActivity() {
    private val permissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions(),
        ) { permissions ->
            // Handle permission results
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        requestNecessaryPermissions()

        setContent {
            VoltAITheme {
                val themeVm: ThemeViewModel = hiltViewModel()
                val selectedTheme by themeVm.selectedTheme.collectAsState()
                VoltAIPremiumTheme(selectedTheme = selectedTheme) {
                    ProfessionalBatteryApp()
                }
            }
        }
    }

    private fun requestNecessaryPermissions() {
        val permissions = mutableListOf<String>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                permissions.add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        if (permissions.isNotEmpty()) {
            permissionLauncher.launch(permissions.toTypedArray())
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfessionalBatteryApp() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val reducedMotion = LocalReducedMotion.current
    val items =
        listOf(
            BottomNavItem("home", "Home", Icons.Default.Home),
            BottomNavItem("health", "Health", Icons.Default.Favorite),
            BottomNavItem("power", "Power", Icons.Default.Build),
            BottomNavItem("stats", "Stats", Icons.Default.Info),
            BottomNavItem("settings", "Settings", Icons.Default.Settings),
        )

    fun navigateTo(route: String) {
        if (currentRoute != route) {
            navController.navigate(route) {
                popUpTo(navController.graph.startDestinationId) { saveState = true }
                launchSingleTop = true
                restoreState = true
            }
        }
    }

    Scaffold(
        modifier =
        Modifier
            .onPreviewKeyEvent { event ->
                if (event.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false
                val index = items.indexOfFirst { it.route == currentRoute }.let { if (it < 0) 0 else it }
                when (event.key) {
                    Key.DirectionRight, Key.Tab -> {
                        val next = (index + 1) % items.size
                        navigateTo(items[next].route)
                        true
                    }
                    Key.DirectionLeft -> {
                        val prev = if (index - 1 < 0) items.lastIndex else index - 1
                        navigateTo(items[prev].route)
                        true
                    }
                    Key.Enter, Key.NumPadEnter, Key.Spacebar -> {
                        // Activate current selection
                        navigateTo(items[index].route)
                        true
                    }
                    Key.Escape, Key.Backspace -> {
                        navController.popBackStack()
                        true
                    }
                    else -> false
                }
            }
            .focusable(),
        bottomBar = {
            NavigationBar(
                modifier = Modifier.height(48.dp), // Smaller bottom bar height
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
                windowInsets = WindowInsets(0, 0, 0, 0), // Remove extra system insets padding
            ) {
                items.forEach { item ->
                    val selected = currentRoute == item.route
                    val scale by animateFloatAsState(
                        targetValue =
                        if (selected) {
                            if (reducedMotion) {
                                1f
                            } else {
                                1.1f
                            }
                        } else {
                            1f
                        },
                        animationSpec = if (reducedMotion) tween(0) else tween(150),
                        label = "navIconScale",
                    )
                    NavigationBarItem(
                        icon = {
                            Surface(
                                shape = CircleShape,
                                color = if (selected) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent,
                                tonalElevation = 0.dp,
                                shadowElevation = 0.dp,
                            ) {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.title,
                                    modifier =
                                    Modifier
                                        .size(22.dp)
                                        .padding(4.dp)
                                        .scale(scale),
                                    tint = if (selected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        },
                        selected = selected,
                        onClick = {
                            navigateTo(item.route)
                        },
                        colors =
                        NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            selectedTextColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            indicatorColor = Color.Transparent,
                        ),
                    )
                }
            }
        },
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(paddingValues),
        ) {
            composable("home") {
                HomeScreen(
                    onNavigateToSettings = { navController.navigate("settings") },
                    onNavigateToCard = { route ->
                        navController.navigate(route)
                    },
                    onNavigateToPremiumOnboarding = {
                        navController.navigate("premium_onboarding")
                    },
                )
            }
            composable("battery_info") {
                BatteryInfoDetailScreen(onBackClick = { navController.popBackStack() })
            }
            composable("health") {
                FocusedBatteryHealthScreen(
                    onNavigateToHealthTips = { navController.navigate("health_tips") },
                )
            }
            composable("power") { PowerManagementSimpleScreen() }
            composable("stats") { SimplifiedStatsScreen() }
            composable("settings") {
                SettingsCategoriesScreen(
                    onNavigateTo = { route -> navController.navigate(route) },
                )
            }

            // Settings categories
            composable("settings/ai") { SettingsAiOptimizationScreen(onBack = { navController.popBackStack() }) }
            composable(
                "settings/notifications",
            ) { SettingsNotificationsScreen(onBack = { navController.popBackStack() }) }
            composable("settings/appearance") { SettingsAppearanceScreen(onBack = { navController.popBackStack() }) }
            composable("settings/advanced") { SettingsAdvancedScreen(onBack = { navController.popBackStack() }) }
            composable("settings/premium") { SettingsPremiumScreen(onBack = { navController.popBackStack() }) }
            composable("settings/about") { SettingsAboutScreen(onBack = { navController.popBackStack() }) }

            // Additional detailed screens accessible from home cards
            composable("history") { HistoryScreen() }
            composable("overlays") { OverlaysScreen() }
            composable("temperature_detail") { TemperatureDetailScreen() }
            composable("current_detail") { ElectricCurrentDetailScreen() }
            composable("health_detail") { HealthDetailScreen() }
            composable("ongoing_detail") { OngoingDetailScreen() }

            // Advanced Premium Screens
            composable("charging_analytics") { ChargingAnalyticsScreen() }
            composable("chemistry_lab") { BatteryChemistryLabScreen() }
            composable("system_optimization") { SystemOptimizationCenterScreen() }
            composable("performance_benchmarks") { PerformanceBenchmarksScreen() }

            // New Premium Analytics Screens
            composable("battery_health_analytics") {
                BatteryHealthAnalyticsScreen(
                    onNavigateToOnboarding = {
                        navController.navigate("premium_onboarding")
                    },
                    onNavigateToPaywall = {
                        navController.navigate("premium_paywall")
                    },
                )
            }
            composable("health_tips") { HealthTipsScreen(onBack = { navController.popBackStack() }) }
            composable("smart_charging") { SmartChargingScreen() }
            composable("battery_diagnostics") { BatteryDiagnosticsScreen() }
            composable("real_time_monitoring") { RealTimeMonitoringScreen() }
            composable("battery_optimization") { BatteryOptimizationScreen() }

            // Premium Onboarding Screen
            composable("premium_onboarding") {
                PremiumOnboardingScreen(
                    onBackClick = { navController.popBackStack() },
                    onSkipClick = { navController.popBackStack() },
                    onUpgradeClick = {
                        // Navigate to paywall for actual purchase
                        navController.navigate("premium_paywall")
                    },
                )
            }

            // Premium Paywall Screen
            composable("premium_paywall") {
                PremiumPaywallScreen(
                    onBackClick = { navController.popBackStack() },
                    onDismiss = { navController.popBackStack() },
                )
            }

            // Data Export/Import Screen
            composable("data_export_import") {
                DataExportImportScreen(
                    onBackClick = { navController.popBackStack() },
                    onUpgradeClick = { /* Navigate to premium upgrade */ },
                )
            }
        }
    }
}

data class BottomNavItem(
    val route: String,
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
)
