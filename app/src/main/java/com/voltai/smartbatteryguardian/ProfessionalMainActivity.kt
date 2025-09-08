package com.voltai.smartbatteryguardian

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.voltai.smartbatteryguardian.screens.*
import com.voltai.smartbatteryguardian.ui.theme.VoltAITheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProfessionalMainActivity : ComponentActivity() {
    
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        // Handle permission results
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        
        requestNecessaryPermissions()
        
        setContent {
            VoltAITheme {
                ProfessionalBatteryApp()
            }
        }
    }
    
    private fun requestNecessaryPermissions() {
        val permissions = mutableListOf<String>()
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) 
                != PackageManager.PERMISSION_GRANTED) {
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
    
    Scaffold(
        bottomBar = {
            NavigationBar {
                val items = listOf(
                    BottomNavItem("home", "Home", Icons.Default.Home),
                    BottomNavItem("health", "Health", Icons.Default.Favorite),
                    BottomNavItem("power", "Power", Icons.Default.Build),
                    BottomNavItem("stats", "Stats", Icons.Default.Info),
                    BottomNavItem("settings", "Settings", Icons.Default.Settings)
                )
                
                items.forEach { item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.title) },
                        label = { Text(item.title) },
                        selected = currentRoute == item.route,
                        onClick = {
                            if (currentRoute != item.route) {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.startDestinationId)
                                    launchSingleTop = true
                                }
                            }
                        }
                    )
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(paddingValues)
        ) {
            composable("home") { 
                HomeScreen(
                    onNavigateToCard = { route ->
                        navController.navigate(route)
                    },
                    onNavigateToPremiumOnboarding = {
                        navController.navigate("premium_onboarding")
                    }
                )
            }
            composable("health") { EnhancedBatteryHealthScreen() }
            composable("power") { AIPoweredPowerManagementScreen() }
            composable("stats") { AdvancedAppUsageScreen() }
            composable("settings") { PremiumSettingsScreen() }
            
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
                    }
                )
            }
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
                    }
                )
            }
            
            // Premium Paywall Screen
            composable("premium_paywall") { 
                PremiumPaywallScreen(
                    onBackClick = { navController.popBackStack() },
                    onDismiss = { navController.popBackStack() }
                )
            }
            
            // Data Export/Import Screen
            composable("data_export_import") {
                DataExportImportScreen(
                    onBackClick = { navController.popBackStack() },
                    onUpgradeClick = { /* Navigate to premium upgrade */ }
                )
            }
        }
    }
}

data class BottomNavItem(
    val route: String,
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)
