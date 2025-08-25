package com.voltai.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Legacy Dashboard Screen - Kept for compatibility
 * 
 * NOTE: The actual dashboard implementation is now in:
 * com.voltai.features.dashboard.ModernDashboardScreen
 * 
 * This follows the UI/UX Design Plan v2.1 with dedicated feature modules.
 */
@Composable
fun DashboardScreen(
    onNavigateToHealth: () -> Unit = {},
    onNavigateToHistory: () -> Unit = {},
    onNavigateToSaver: () -> Unit = {},
    onNavigateToThermal: () -> Unit = {},
    onNavigateToAppUsage: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onNavigateToBackup: () -> Unit = {},
    onNavigateToTips: () -> Unit = {},
    onNavigateToWidgetConfig: () -> Unit = {}
) {
    // This is a legacy placeholder - the real implementation is in features/dashboard/ModernDashboardScreen.kt
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "VoltAI Dashboard",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Please use ModernDashboardScreen from features module",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}