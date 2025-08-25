package com.voltai.features.onboarding

import android.Manifest
import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.voltai.ui.components.*
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermissionScreen(
    onNavigateToComplete: () -> Unit = {},
    onNavigateBack: () -> Unit = {}
) {
    val context = LocalContext.current
    var isVisible by remember { mutableStateOf(false) }
    var hasUsageStatsPermission by remember { mutableStateOf(false) }
    var showPermissionExplanation by remember { mutableStateOf(false) }
    
    // Check initial permission status
    LaunchedEffect(Unit) {
        hasUsageStatsPermission = checkUsageStatsPermission(context)
        delay(300)
        isVisible = true
        delay(1000)
        showPermissionExplanation = true
    }
    
    // Usage Stats permission launcher
    val usageStatsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        hasUsageStatsPermission = checkUsageStatsPermission(context)
        if (hasUsageStatsPermission) {
            onNavigateToComplete()
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
                    )
                )
            )
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top App Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                BackArrowIcon(
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "Step 1 of 2",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Permission Icon
        AnimatedVisibility(
            visible = isVisible,
            enter = scaleIn(
                initialScale = 0.3f,
                animationSpec = tween(800, easing = EaseOutBack)
            ) + fadeIn(animationSpec = tween(800))
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                if (hasUsageStatsPermission) Color(0xFF4CAF50).copy(alpha = 0.3f)
                                else MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                                Color.Transparent
                            )
                        ),
                        shape = RoundedCornerShape(60.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (hasUsageStatsPermission) {
                    CheckIcon(
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(60.dp)
                    )
                } else {
                    ShieldIcon(
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(60.dp)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Permission Title and Description
        AnimatedVisibility(
            visible = isVisible,
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = tween(800, delayMillis = 400, easing = EaseOutCubic)
            ) + fadeIn(animationSpec = tween(800, delayMillis = 400))
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = if (hasUsageStatsPermission) "Permission Granted!" else "Usage Access Required",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
                
                Text(
                    text = if (hasUsageStatsPermission) {
                        "VoltAI can now access usage statistics to provide accurate battery analytics and optimization recommendations."
                    } else {
                        "VoltAI needs access to usage statistics to analyze which apps consume the most battery and provide personalized optimization tips."
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                    lineHeight = 24.sp
                )
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Permission Explanation Cards
        AnimatedVisibility(
            visible = showPermissionExplanation,
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = tween(800, delayMillis = 200, easing = EaseOutCubic)
            ) + fadeIn(animationSpec = tween(800, delayMillis = 200))
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                PermissionExplanationCard(
                    icon = { AnalyticsIcon(tint = Color(0xFF2196F3)) },
                    title = "App Usage Analysis",
                    description = "Identify battery-draining apps and get optimization suggestions"
                )
                PermissionExplanationCard(
                    icon = { GraphIcon(tint = Color(0xFF9C27B0)) },
                    title = "Usage Patterns",
                    description = "Track screen time and usage patterns for better battery management"
                )
                PermissionExplanationCard(
                    icon = { ShieldIcon(tint = Color(0xFF4CAF50)) },
                    title = "Privacy Protected",
                    description = "Data stays on your device - no personal information is collected"
                )
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Action Buttons
        AnimatedVisibility(
            visible = isVisible,
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = tween(800, delayMillis = 600, easing = EaseOutCubic)
            ) + fadeIn(animationSpec = tween(800, delayMillis = 600))
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (hasUsageStatsPermission) {
                    Button(
                        onClick = onNavigateToComplete,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4CAF50)
                        )
                    ) {
                        Text(
                            text = "Continue",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        FlashIcon(
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                } else {
                    Button(
                        onClick = {
                            val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
                            usageStatsLauncher.launch(intent)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(
                            text = "Grant Permission",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        ShieldIcon(
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    
                    OutlinedButton(
                        onClick = onNavigateToComplete,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = "Skip for Now",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PermissionExplanationCard(
    icon: @Composable () -> Unit,
    title: String,
    description: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Box(modifier = Modifier.size(24.dp)) {
                    icon()
                }
            }
            
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }
    }
}

private fun checkUsageStatsPermission(context: Context): Boolean {
    val appOpsManager = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
    val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        appOpsManager.unsafeCheckOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            android.os.Process.myUid(),
            context.packageName
        )
    } else {
        @Suppress("DEPRECATION")
        appOpsManager.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            android.os.Process.myUid(),
            context.packageName
        )
    }
    return mode == AppOpsManager.MODE_ALLOWED
}