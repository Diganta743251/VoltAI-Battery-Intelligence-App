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
    onNavigateToSettings: () -> Unit
) {
    val batteryData by batteryDataProvider.batteryData.collectAsState()
    
    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            com.voltai.smartbatteryguardian.screens.HomeScreen(
                onNavigateToSettings = onNavigateToSettings
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
                onBackClick = { navController.popBackStack() }
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
                onBackClick = { navController.popBackStack() }
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
