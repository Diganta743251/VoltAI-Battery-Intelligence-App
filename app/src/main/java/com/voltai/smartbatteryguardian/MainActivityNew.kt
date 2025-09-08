package com.voltai.smartbatteryguardian

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.voltai.smartbatteryguardian.navigation.AppNavHost
import com.voltai.smartbatteryguardian.service.BatteryMonitoringService
import com.voltai.smartbatteryguardian.ui.theme.VoltAITheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivityNew : ComponentActivity() {

    @Inject
    lateinit var batteryDataProvider: BatteryDataProvider

    private val batteryViewModel: BatteryViewModel by viewModels()

    // Permission launcher for notification permission (Android 13+)
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startBatteryMonitoringService()
        } else {
            // Show explanation dialog
            showPermissionExplanationDialog()
        }
    }

    // Usage stats permission launcher
    private val usageStatsLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (hasUsageStatsPermission()) {
            // Permission granted, start monitoring
            startBatteryMonitoringService()
        } else {
            // Show fallback message
            showUsageStatsExplanation()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Install splash screen
        installSplashScreen().apply {
            setKeepOnScreenCondition {
                batteryViewModel.isLoading.value ?: false
            }
        }

        super.onCreate(savedInstanceState)

        // Request necessary permissions
        requestPermissions()

        setContent {
            VoltAITheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    
                    AppNavHost(
                        navController = navController,
                        batteryDataProvider = batteryDataProvider,
                        onNavigateToSettings = ::navigateToSettings
                    )
                }
            }
        }
    }

    private fun requestPermissions() {
        // Request notification permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                return
            }
        }

        // Request usage stats permission
        if (!hasUsageStatsPermission()) {
            requestUsageStatsPermission()
            return
        }

        // All permissions granted, start service
        startBatteryMonitoringService()
    }

    private fun hasUsageStatsPermission(): Boolean {
        val appOpsManager = getSystemService(APP_OPS_SERVICE) as android.app.AppOpsManager
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOpsManager.unsafeCheckOpNoThrow(
                android.app.AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(),
                packageName
            )
        } else {
            @Suppress("DEPRECATION")
            appOpsManager.checkOpNoThrow(
                android.app.AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(),
                packageName
            )
        }
        return mode == android.app.AppOpsManager.MODE_ALLOWED
    }

    private fun requestUsageStatsPermission() {
        val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
            data = Uri.parse("package:$packageName")
        }
        usageStatsLauncher.launch(intent)
    }

    private fun startBatteryMonitoringService() {
        val serviceIntent = Intent(this, BatteryMonitoringService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }
    }

    private fun navigateToSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.parse("package:$packageName")
        }
        startActivity(intent)
    }

    private fun showPermissionExplanationDialog() {
        // This would typically show an AlertDialog
        // For now, we'll just start the service anyway for demo purposes
        startBatteryMonitoringService()
    }

    private fun showUsageStatsExplanation() {
        // Show explanation for usage stats permission
        // For now, continue without this permission
    }

    override fun onResume() {
        super.onResume()
        // Check if permissions were granted when returning to app
        if (hasUsageStatsPermission()) {
            startBatteryMonitoringService()
        }
    }
}

@Composable
private fun PermissionExplanationDialog(
    title: String,
    message: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Grant Permission")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Skip")
            }
        }
    )
}

@Composable
private fun LoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator()
            Text(
                text = "Initializing VoltAI...",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}
