package com.voltai.smartbatteryguardian.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.voltai.smartbatteryguardian.BatteryDataProvider
import com.voltai.smartbatteryguardian.screens.HomeScreen
import com.voltai.smartbatteryguardian.screens.WelcomeScreen

@Composable
fun placeholderScreen(screenName: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("$screenName Screen - Coming Soon!")
    }
}

@Composable
fun AppNavHost(
    navController: NavHostController,
    batteryDataProvider: BatteryDataProvider,
    onNavigateToSettings: () -> Unit,
) {
    val batteryData by batteryDataProvider.batteryData.collectAsState()

    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            com.voltai.smartbatteryguardian.screens.HomeScreen(
                onNavigateToSettings = onNavigateToSettings,
                onNavigateToCard = { cardRoute ->
                    navController.navigate(cardRoute)
                },
                onNavigateToPremiumOnboarding = {
                    navController.navigate("premium_onboarding")
                },
                onShowWelcome = {
                    navController.navigate("welcome")
                },
            )
        }

        composable("welcome") {
            WelcomeScreen(
                onGetStarted = {
                    navController.navigate("home") {
                        popUpTo("welcome") { inclusive = true }
                    }
                },
                onSkip = {
                    navController.navigate("home") {
                        popUpTo("welcome") { inclusive = true }
                    }
                },
            )
        }

        composable("settings") {
            com.voltai.smartbatteryguardian.screens.SettingsScreen()
        }

        // Existing screens
        composable("health") {
            com.voltai.smartbatteryguardian.screens.HealthDetailScreen()
        }
        composable("temperature") {
            com.voltai.smartbatteryguardian.screens.TemperatureDetailScreen()
        }
        composable("battery_info") {
            com.voltai.smartbatteryguardian.screens.BatteryInfoDetailScreen(
                onBackClick = { navController.popBackStack() },
            )
        }
        composable("current") {
            com.voltai.smartbatteryguardian.screens.ElectricCurrentDetailScreen()
        }
        composable("ongoing") {
            com.voltai.smartbatteryguardian.screens.OngoingDetailScreen()
        }
        composable("statistics") {
            com.voltai.smartbatteryguardian.screens.StatisticsDetailScreen()
        }
        composable("saver") {
            com.voltai.smartbatteryguardian.screens.BatterySaverScreen()
        }
        composable("bluetooth") {
            com.voltai.smartbatteryguardian.screens.BluetoothDevicesScreen()
        }
        composable("device_log") {
            com.voltai.smartbatteryguardian.screens.DeviceLogScreen()
        }
        composable("idle_log") {
            com.voltai.smartbatteryguardian.screens.IdleLogScreen()
        }
        composable("overlays") {
            com.voltai.smartbatteryguardian.screens.OverlaysScreen()
        }
        composable("history") {
            com.voltai.smartbatteryguardian.screens.HistoryScreen()
        }
        composable("wakelocks") {
            com.voltai.smartbatteryguardian.screens.WakelocksScreen()
        }
        // Advanced/premium routes used by Home cards
        composable("charging_analytics") {
            com.voltai.smartbatteryguardian.screens.ChargingAnalyticsScreen()
        }
        composable("chemistry_lab") {
            com.voltai.smartbatteryguardian.screens.BatteryChemistryLabScreen()
        }
        composable("system_optimization") {
            com.voltai.smartbatteryguardian.screens.SystemOptimizationCenterScreen()
        }
        composable("performance_benchmarks") {
            com.voltai.smartbatteryguardian.screens.PerformanceBenchmarksScreen()
        }
        composable("data_export_import") {
            com.voltai.smartbatteryguardian.screens.DataExportImportScreen()
        }
        composable("battery_health_analytics") {
            com.voltai.smartbatteryguardian.screens.BatteryHealthAnalyticsScreen()
        }
        composable("smart_charging") {
            com.voltai.smartbatteryguardian.screens.SmartChargingScreen()
        }
        composable("battery_diagnostics") {
            com.voltai.smartbatteryguardian.screens.BatteryDiagnosticsScreen()
        }
        composable("real_time_monitoring") {
            com.voltai.smartbatteryguardian.screens.RealTimeMonitoringScreen()
        }
        composable("battery_optimization") {
            com.voltai.smartbatteryguardian.screens.BatteryOptimizationScreen()
        }
    }
}

@Composable
fun appNavHost(navController: NavHostController) {
    NavHost(navController = navController, startDestination = "dashboard") {
        // Main dashboard with available screens
        composable("dashboard") {
            placeholderScreen("Dashboard - Using our TestMainActivity for now")
        }

        // Available local screens from the screens folder
        composable("health") {
            com.voltai.smartbatteryguardian.screens.HealthDetailScreen()
        }
        composable("temperature") {
            com.voltai.smartbatteryguardian.screens.TemperatureDetailScreen()
        }
        composable("battery_info") {
            com.voltai.smartbatteryguardian.screens.BatteryInfoDetailScreen(
                onBackClick = { navController.popBackStack() },
            )
        }
        composable("current") {
            com.voltai.smartbatteryguardian.screens.ElectricCurrentDetailScreen()
        }
        composable("ongoing") {
            com.voltai.smartbatteryguardian.screens.OngoingDetailScreen()
        }
        composable("statistics") {
            com.voltai.smartbatteryguardian.screens.StatisticsDetailScreen()
        }

        // Placeholder screens for other features
        composable("saver") { placeholderScreen("Battery Saver") }
        composable("bluetooth") { placeholderScreen("Bluetooth Devices") }
        composable("device_log") { placeholderScreen("Device Log") }
        composable("idle_log") { placeholderScreen("Idle Log") }
        composable("overlays") { placeholderScreen("Overlays") }
        composable("wakelocks") { placeholderScreen("Wakelocks") }
        composable("settings") { placeholderScreen("Settings") }
        composable("help") { placeholderScreen("Help") }
    }
}
