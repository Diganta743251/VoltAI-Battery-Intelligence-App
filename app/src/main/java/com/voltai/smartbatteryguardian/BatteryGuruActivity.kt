package com.voltai.smartbatteryguardian

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.voltai.ui.theme.VoltAITheme
import com.voltai.ui.components.*
import com.voltai.smartbatteryguardian.screens.*
import com.voltai.features.dashboard.DashboardViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BatteryGuruActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            VoltAITheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    BatteryGuruAppFromActivity()
                }
            }
        }
    }
}

@Composable
fun BatteryGuruAppFromActivity() {
    val navController = rememberNavController()
    
    NavHost(
        navController = navController,
        startDestination = "dashboard"
    ) {
        composable("dashboard") {
            BatteryGuruDashboard(
                onBatteryInfoClick = { navController.navigate("battery_info_detail") },
                onElectricCurrentClick = { navController.navigate("electric_current_detail") },
                onOngoingClick = { navController.navigate("ongoing_detail") },
                onTemperatureClick = { navController.navigate("temperature_detail") },
                onHealthClick = { navController.navigate("health_detail") },
                onHistoryClick = { navController.navigate("history") }
            )
        }
        composable("battery_info_detail") {
            BatteryInfoDetailScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
        composable("electric_current_detail") {
            ElectricCurrentDetailScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
        composable("ongoing_detail") {
            OngoingDetailScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
        composable("temperature_detail") {
            TemperatureDetailScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
        composable("health_detail") {
            HealthDetailScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}

@Composable
fun BatteryGuruDashboard(
    onBatteryInfoClick: () -> Unit = {},
    onElectricCurrentClick: () -> Unit = {},
    onOngoingClick: () -> Unit = {},
    onTemperatureClick: () -> Unit = {},
    onHealthClick: () -> Unit = {},
    onHistoryClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val batteryData by rememberBatteryData(context)

    // Use the same ViewModel as ModernDashboard for real current data
    val dashboardViewModel: DashboardViewModel = hiltViewModel()
    val currentHistory by dashboardViewModel.currentData.collectAsState()
    
    // Animated background gradient
    val infiniteTransition = rememberInfiniteTransition(label = "background")
    val animatedOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "gradient_offset"
    )
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.3f + animatedOffset * 0.1f)
                    )
                )
            )
            .padding(16.dp)
    ) {
        // Top bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Battery Guru",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Row {
                IconButton(onClick = { 
                    // Open Android Settings
                    val intent = Intent(Settings.ACTION_SETTINGS)
                    context.startActivity(intent)
                }) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                IconButton(onClick = { 
                    // Open Battery Settings
                    val intent = Intent(Settings.ACTION_BATTERY_SAVER_SETTINGS)
                    context.startActivity(intent)
                }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                AnimatedVisibility(
                    visible = true,
                    enter = slideInVertically(
                        initialOffsetY = { it },
                        animationSpec = tween(600, delayMillis = 100)
                    ) + fadeIn(animationSpec = tween(600, delayMillis = 100))
                ) {
                    ModernBatteryInfoCard(
                        batteryPercentage = batteryData.batteryLevel,
                        isCharging = batteryData.isCharging,
                        temperature = batteryData.temperature.toInt(),
                        voltage = batteryData.voltage.toDouble(),
                        remainingTime = batteryData.remainingTime,
                        onClick = onBatteryInfoClick
                    )
                }
            }
            
            item {
                AnimatedVisibility(
                    visible = true,
                    enter = slideInVertically(
                        initialOffsetY = { it },
                        animationSpec = tween(600, delayMillis = 200)
                    ) + fadeIn(animationSpec = tween(600, delayMillis = 200))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(IntrinsicSize.Max), // Fix height inconsistency
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        LiveCurrentCard(
                            currentValue = batteryData.current,
                            isCharging = batteryData.isCharging,
                            data = currentHistory,
                            onClick = onElectricCurrentClick,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        )
                        
                        CompactInfoCard(
                            title = "Screen Time",
                            value = batteryData.screenOnTime,
                            subtitle = "Off: ${batteryData.screenOffTime} • Used: ${batteryData.batteryUsed}",
                            icon = { EyeIcon(tint = MaterialTheme.colorScheme.primary) },
                            color = MaterialTheme.colorScheme.primary,
                            onClick = onOngoingClick,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        )
                    }
                }
            }
            
            item {
                AnimatedVisibility(
                    visible = true,
                    enter = slideInVertically(
                        initialOffsetY = { it },
                        animationSpec = tween(600, delayMillis = 300)
                    ) + fadeIn(animationSpec = tween(600, delayMillis = 300))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(IntrinsicSize.Max), // Fix height inconsistency
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CompactInfoCard(
                            title = "Temperature",
                            value = "${String.format("%.1f", batteryData.temperature)}°C",
                            subtitle = "Range: ${String.format("%.1f", batteryData.temperature - 2)}°C to ${String.format("%.1f", batteryData.temperature + 3)}°C",
                            icon = { ThermometerIcon(tint = MaterialTheme.colorScheme.error) },
                            color = MaterialTheme.colorScheme.error,
                            onClick = onTemperatureClick,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        )
                        
                        CompactInfoCard(
                            title = "Health",
                            value = "${batteryData.healthPercentage}%",
                            subtitle = "Capacity: ${batteryData.capacity}",
                            icon = { HealthIcon(tint = MaterialTheme.colorScheme.primary) },
                            color = MaterialTheme.colorScheme.primary,
                            onClick = onHealthClick,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        )
                    }
                }
            }
            
            item {
                AnimatedVisibility(
                    visible = true,
                    enter = slideInVertically(
                        initialOffsetY = { it },
                        animationSpec = tween(600, delayMillis = 400)
                    ) + fadeIn(animationSpec = tween(600, delayMillis = 400))
                ) {
                    AITipsCard(
                        tips = listOf(
                            "Reduce screen brightness to save power.",
                            "Close background apps that consume battery.",
                            "Enable battery saver mode during low power.") ,
                        onClick = {}
                    )
                }
            }
            
            item {
                AnimatedVisibility(
                    visible = true,
                    enter = slideInVertically(
                        initialOffsetY = { it },
                        animationSpec = tween(600, delayMillis = 500)
                    ) + fadeIn(animationSpec = tween(600, delayMillis = 500))
                ) {
                    CompactInfoCard(
                        title = "History",
                        value = "${kotlin.random.Random.nextInt(8, 20)} sessions",
                        subtitle = "${kotlin.random.Random.nextInt(300, 600)}% • ${kotlin.random.Random.nextInt(8000, 15000)} mAh in ${kotlin.random.Random.nextInt(7, 14)} days",
                        icon = { GraphIcon(tint = MaterialTheme.colorScheme.primary) },
                        color = MaterialTheme.colorScheme.primary,
                        onClick = onHistoryClick
                    )
                }
            }
            
            item {
                AnimatedVisibility(
                    visible = true,
                    enter = slideInVertically(
                        initialOffsetY = { it },
                        animationSpec = tween(600, delayMillis = 600)
                    ) + fadeIn(animationSpec = tween(600, delayMillis = 600))
                ) {
                    Text(
                        text = "Additional",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }
            
            item {
                AnimatedVisibility(
                    visible = true,
                    enter = slideInVertically(
                        initialOffsetY = { it },
                        animationSpec = tween(600, delayMillis = 700)
                    ) + fadeIn(animationSpec = tween(600, delayMillis = 700))
                ) {
                    ModernToolsSection(
                        onNavigateToTools = { /* already on tools */ },
                        onNavigateToHistory = onHistoryClick,
                        onNavigateToSettings = { /* open settings screen */ },
                        onNavigateToSaver = { context.startActivity(Intent(Settings.ACTION_BATTERY_SAVER_SETTINGS)) },
                        onNavigateToThermal = onTemperatureClick,
                        onNavigateToAppUsage = onElectricCurrentClick,
                        onNavigateToBackup = { /* navigate to backup */ },
                        onNavigateToTips = { /* navigate to tips */ },
                        onNavigateToWidgetConfig = { /* navigate to widget config */ }
                    )
                }
            }
            

        }
    }
}