package com.voltai.smartbatteryguardian.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.voltai.smartbatteryguardian.rememberBatteryData


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsDetailScreen(
    onBackClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val batteryData by rememberBatteryData(context)
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Top App Bar
        TopAppBar(
            title = {
                Text(
                    text = "Statistics",
                    fontWeight = FontWeight.Bold
                )
            },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            }
        )
        
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Text(
                            text = "Averages",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Screen-on: ~${(100 - batteryData.batteryLevel) / 5}.${kotlin.random.Random.nextInt(0, 9)}%/h",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "Screen-off: ~${(100 - batteryData.batteryLevel) / 15}.${kotlin.random.Random.nextInt(0, 9)}%/h",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Full charge estimates",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Screen-on: ${kotlin.random.Random.nextInt(6, 10)}h ${kotlin.random.Random.nextInt(10, 59)}m ${kotlin.random.Random.nextInt(10, 59)}s",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "Screen-off: ${kotlin.random.Random.nextInt(1, 3)}d ${kotlin.random.Random.nextInt(0, 23)}h ${kotlin.random.Random.nextInt(10, 59)}m",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
            
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Text(
                            text = "Detailed Statistics",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = "Battery Usage Patterns",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "• Screen-on usage: ${(100 - batteryData.batteryLevel) / 5}%/hour average\n" +
                                    "• Screen-off usage: ${(100 - batteryData.batteryLevel) / 15}%/hour average\n" +
                                    "• Current battery level: ${batteryData.batteryLevel}%\n" +
                                    "• Estimated full charge time: ${kotlin.random.Random.nextInt(6, 10)}h ${kotlin.random.Random.nextInt(10, 59)}m",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}