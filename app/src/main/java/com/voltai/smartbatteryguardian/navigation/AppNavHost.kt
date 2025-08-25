package com.voltai.smartbatteryguardian.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
// Removed legacy imports; use fully qualified names or modern screens where needed

@Composable
fun PlaceholderScreen(screenName: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("$screenName Screen - Coming Soon!")
    }
}

@Composable
fun AppNavHost(navController: NavHostController) {
    NavHost(navController = navController, startDestination = "splash") {
        composable("splash") {
            com.voltai.features.splash.SplashScreen(
                onNavigateToMain = { 
                    navController.navigate("onboarding") {
                        popUpTo("splash") { inclusive = true }
                    }
                }
            )
        }
        
        composable("onboarding") {
            com.voltai.features.onboarding.OnboardingScreen(
                onComplete = {
                    navController.navigate("dashboard") {
                        popUpTo("onboarding") { inclusive = true }
                    }
                }
            )
        }
        
        composable("dashboard") { 
            // Use ModernDashboardScreen directly (following UI/UX Design Plan v2.1)
            com.voltai.features.dashboard.ModernDashboardScreen(
                // Dedicated feature screens
                onNavigateToHealth = { navController.navigate("health") },
                onNavigateToTemperature = { navController.navigate("temperature") },
                onNavigateToVoltage = { navController.navigate("voltage") },
                onNavigateToCurrent = { navController.navigate("current") },
                onNavigateToUsage = { navController.navigate("usage") },
                onNavigateToScreenTime = { navController.navigate("screen_time") },
                onNavigateToCycleCount = { navController.navigate("cycle_count") },
                onNavigateToForecast = { navController.navigate("forecast") },
                onNavigateToTools = { navController.navigate("tools") },
                onNavigateToHistory = { navController.navigate("history") },
                onNavigateToSettings = { navController.navigate("settings") },
                // Legacy navigation (keeping for compatibility)
                onNavigateToSaver = { navController.navigate("saver") },
                onNavigateToThermal = { navController.navigate("temperature") },
                onNavigateToAppUsage = { navController.navigate("usage") },
                onNavigateToBackup = { navController.navigate("backup") },
                onNavigateToTips = { navController.navigate("tips") },
                onNavigateToWidgetConfig = { navController.navigate("widget_config") }
            )
        }
        
        composable("charging") {
            com.voltai.features.charging.ChargingScreen(
                onNavigateBack = { navController.popBackStack() },
                onDismiss = { navController.popBackStack() }
            )
        }
        
        // Core Feature Screens (following UI/UX Design Plan v2.1)
        composable("health") { 
            com.voltai.features.health.ModernHealthScreen(
                onNavigateBack = { navController.popBackStack() }
            ) 
        }
        composable("temperature") { 
            com.voltai.features.temperature.TemperatureScreen(
                onNavigateBack = { navController.popBackStack() }
            ) 
        }
        composable("voltage") { 
            com.voltai.features.voltage.VoltageScreen(
                onNavigateBack = { navController.popBackStack() }
            ) 
        }
        composable("current") { 
            com.voltai.features.current.CurrentScreen(
                onNavigateBack = { navController.popBackStack() }
            ) 
        }
        composable("usage") { 
            com.voltai.features.usage.UsageScreen(
                onNavigateBack = { navController.popBackStack() }
            ) 
        }
        composable("screen_time") { 
            com.voltai.features.screen_time.ScreenTimeScreen(
                onNavigateBack = { navController.popBackStack() }
            ) 
        }
        composable("cycle_count") { 
            com.voltai.features.cycle_count.CycleCountScreen(
                onNavigateBack = { navController.popBackStack() }
            ) 
        }
        composable("forecast") { 
            com.voltai.features.forecast.ForecastScreen(
                onNavigateBack = { navController.popBackStack() }
            ) 
        }
        composable("tools") { 
            com.voltai.features.tools.ToolsScreen(
                onNavigateBack = { navController.popBackStack() }
            ) 
        }
        composable("history") { 
            com.voltai.features.history.HistoryScreen()
        }
        
        // Legacy screens (kept for route compatibility) replaced with placeholders where implementations are absent
        composable("saver") { PlaceholderScreen("Saver") }
        composable("appusage") { PlaceholderScreen("App Usage") }
        composable("thermal") { com.voltai.features.temperature.TemperatureScreen(onNavigateBack = { navController.popBackStack() }) }
        composable("backup") { PlaceholderScreen("Backup") }
        composable("tips") { PlaceholderScreen("Tips") }
        composable("widget_config") { PlaceholderScreen("Widget Config") }
        
        // Settings
        composable("settings") { 
            com.voltai.features.settings.ModernSettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToHelp = { navController.navigate("help") },
                onNavigateToFeedback = { navController.navigate("feedback") }
            ) 
        }
        composable("help") { PlaceholderScreen("Help") }
        composable("feedback") { PlaceholderScreen("Feedback") }
        
        // Onboarding Flow
        composable("welcome") {
            com.voltai.features.onboarding.WelcomeScreen(
                onNavigateToPermissions = { navController.navigate("permissions") }
            )
        }
        composable("permissions") {
            com.voltai.features.onboarding.PermissionScreen(
                onNavigateToComplete = { navController.navigate("complete") },
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable("complete") {
            com.voltai.features.onboarding.CompleteScreen(
                onNavigateToDashboard = { 
                    navController.navigate("dashboard") {
                        popUpTo("welcome") { inclusive = true }
                    }
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}