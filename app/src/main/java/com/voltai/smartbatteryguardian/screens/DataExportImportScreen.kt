package com.voltai.smartbatteryguardian.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.voltai.smartbatteryguardian.ui.premium.PremiumGate
import com.voltai.smartbatteryguardian.viewmodels.DataExportImportViewModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * Comprehensive Data Export/Import screen with premium features
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataExportImportScreen(
    onBackClick: () -> Unit = {},
    onUpgradeClick: () -> Unit = {},
    viewModel: DataExportImportViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    
    val exportState by viewModel.exportState.collectAsState()
    val importState by viewModel.importState.collectAsState()
    val exportProgress by viewModel.exportProgress.collectAsState()
    val importProgress by viewModel.importProgress.collectAsState()
    val cloudBackups by viewModel.cloudBackups.collectAsState()
    
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Export", "Import", "Cloud Backup")
    
    // File picker launchers
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/*")
    ) { uri ->
        uri?.let { viewModel.exportToUri(it) }
    }
    
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { viewModel.importFromUri(it) }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFF8F9FA),
                        Color(0xFFE8F5E8)
                    )
                )
            )
    ) {
        // Header
        DataExportImportHeader(onBackClick = onBackClick)
        
        // Tab Row
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.Transparent,
            contentColor = Color(0xFF4CAF50)
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            text = title,
                            fontSize = 14.sp,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                )
            }
        }
        
        // Content based on selected tab
        when (selectedTab) {
            0 -> PremiumGate(
                featureId = "export_data",
                onUpgradeClick = onUpgradeClick
            ) {
                ExportTab(
                    exportState = exportState,
                    exportProgress = exportProgress,
                    onExportClick = { format, config ->
                        when (format) {
                            "CSV" -> {
                                exportLauncher.launch("voltai_battery_data.csv")
                            }
                            "JSON" -> {
                                exportLauncher.launch("voltai_battery_data.json")
                            }
                            "BACKUP" -> {
                                exportLauncher.launch("voltai_backup.json")
                            }
                        }
                    }
                )
            }
            1 -> PremiumGate(
                featureId = "export_data",
                onUpgradeClick = onUpgradeClick
            ) {
                ImportTab(
                    importState = importState,
                    importProgress = importProgress,
                    onImportClick = {
                        importLauncher.launch(arrayOf("application/json", "text/csv"))
                    }
                )
            }
            2 -> PremiumGate(
                featureId = "cloud_backup",
                onUpgradeClick = onUpgradeClick
            ) {
                CloudBackupTab(
                    cloudBackups = cloudBackups,
                    onCreateBackup = { viewModel.createCloudBackup() },
                    onRestoreBackup = { backupId -> viewModel.restoreCloudBackup(backupId) },
                    onDeleteBackup = { backupId -> viewModel.deleteCloudBackup(backupId) }
                )
            }
        }
    }
}

@Composable
private fun DataExportImportHeader(onBackClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFF4CAF50),
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Data Export & Import",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Backup and restore your battery data",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
            
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color.White.copy(alpha = 0.2f)
            ) {
                Text(
                    text = "💎 Premium",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
private fun ExportTab(
    exportState: DataExportImportViewModel.ExportState,
    exportProgress: Float,
    onExportClick: (String, String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Export Your Data",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2E7D32)
            )
            Text(
                text = "Export your battery data in various formats for analysis or backup purposes.",
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
        
        // Export formats
        item {
            ExportFormatSection(onExportClick = { format, _ -> onExportClick(format, "config") })
        }
        
        // Export options
        item {
            ExportOptionsSection()
        }
        
        // Export progress
        if (exportState != DataExportImportViewModel.ExportState.IDLE) {
            item {
                ExportProgressSection(
                    exportState = exportState,
                    progress = exportProgress
                )
            }
        }
        
        // Export statistics
        item {
            ExportStatisticsSection()
        }
    }
}

@Composable
private fun ExportFormatSection(
    onExportClick: (String, String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Export Formats",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2E7D32)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // CSV Export
            ExportFormatCard(
                icon = Icons.Default.List,
                title = "CSV Export",
                description = "Spreadsheet-compatible format for analysis in Excel or Google Sheets",
                fileSize = "~2-5 MB",
                onClick = {
                    onExportClick("CSV", "csv_config")
                }
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // JSON Export
            ExportFormatCard(
                icon = Icons.Default.Settings,
                title = "JSON Export",
                description = "Structured format for developers and advanced analysis",
                fileSize = "~1-3 MB",
                onClick = {
                    onExportClick("JSON", "json_config")
                }
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Complete Backup
            ExportFormatCard(
                icon = Icons.Default.Settings,
                title = "Complete Backup",
                description = "Full backup including settings, preferences, and all data",
                fileSize = "~3-8 MB",
                onClick = {
                    onExportClick("BACKUP", "backup_config")
                }
            )
        }
    }
}

@Composable
private fun ExportFormatCard(
    icon: ImageVector,
    title: String,
    description: String,
    fileSize: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF1F8E9)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = Color(0xFF4CAF50),
                modifier = Modifier.size(32.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2E7D32)
                )
                Text(
                    text = description,
                    fontSize = 12.sp,
                    color = Color.Gray,
                    lineHeight = 16.sp
                )
                Text(
                    text = "Size: $fileSize",
                    fontSize = 11.sp,
                    color = Color(0xFF4CAF50),
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = "Export",
                tint = Color(0xFF4CAF50),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun ExportOptionsSection() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Export Options",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2E7D32)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            var includeBatteryReadings by remember { mutableStateOf(true) }
            var includeChargingSessions by remember { mutableStateOf(true) }
            var includeAppUsage by remember { mutableStateOf(true) }
            var includeHealthRecords by remember { mutableStateOf(true) }
            var includePredictions by remember { mutableStateOf(true) }
            var includeSystemMetrics by remember { mutableStateOf(false) }
            
            ExportOptionCheckbox(
                checked = includeBatteryReadings,
                onCheckedChange = { includeBatteryReadings = it },
                title = "Battery Readings",
                description = "Real-time battery level, voltage, temperature data"
            )
            
            ExportOptionCheckbox(
                checked = includeChargingSessions,
                onCheckedChange = { includeChargingSessions = it },
                title = "Charging Sessions",
                description = "Complete charging session history and analytics"
            )
            
            ExportOptionCheckbox(
                checked = includeAppUsage,
                onCheckedChange = { includeAppUsage = it },
                title = "App Usage Stats",
                description = "Battery usage by app and usage patterns"
            )
            
            ExportOptionCheckbox(
                checked = includeHealthRecords,
                onCheckedChange = { includeHealthRecords = it },
                title = "Health Records",
                description = "Battery health trends and degradation data"
            )
            
            ExportOptionCheckbox(
                checked = includePredictions,
                onCheckedChange = { includePredictions = it },
                title = "AI Predictions",
                description = "Machine learning predictions and recommendations"
            )
            
            ExportOptionCheckbox(
                checked = includeSystemMetrics,
                onCheckedChange = { includeSystemMetrics = it },
                title = "System Metrics",
                description = "Detailed system performance data (large file size)"
            )
        }
    }
}

@Composable
private fun ExportOptionCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(
                checkedColor = Color(0xFF4CAF50)
            )
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF2E7D32)
            )
            Text(
                text = description,
                fontSize = 11.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
private fun ExportProgressSection(
    exportState: DataExportImportViewModel.ExportState,
    progress: Float
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                when (exportState) {
                    DataExportImportViewModel.ExportState.PREPARING -> {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color(0xFF4CAF50),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Preparing export...",
                            fontSize = 16.sp,
                            color = Color(0xFF2E7D32)
                        )
                    }
                    DataExportImportViewModel.ExportState.EXPORTING -> {
                        CircularProgressIndicator(
                            progress = progress,
                            modifier = Modifier.size(20.dp),
                            color = Color(0xFF4CAF50),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Exporting data... ${(progress * 100).toInt()}%",
                            fontSize = 16.sp,
                            color = Color(0xFF2E7D32)
                        )
                    }
                    DataExportImportViewModel.ExportState.COMPLETED -> {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Complete",
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Export completed successfully!",
                            fontSize = 16.sp,
                            color = Color(0xFF4CAF50),
                            fontWeight = FontWeight.Bold
                        )
                    }
                    DataExportImportViewModel.ExportState.ERROR -> {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Error",
                            tint = Color(0xFFD32F2F),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Export failed",
                            fontSize = 16.sp,
                            color = Color(0xFFD32F2F),
                            fontWeight = FontWeight.Bold
                        )
                    }
                    else -> {}
                }
            }
            
            if (exportState == DataExportImportViewModel.ExportState.EXPORTING) {
                Spacer(modifier = Modifier.height(12.dp))
                LinearProgressIndicator(
                    progress = progress,
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFF4CAF50)
                )
            }
        }
    }
}

@Composable
private fun ExportStatisticsSection() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Data Summary",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2E7D32)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatisticItem(
                    value = "12,843",
                    label = "Battery\nReadings"
                )
                StatisticItem(
                    value = "387",
                    label = "Charging\nSessions"
                )
                StatisticItem(
                    value = "2,156",
                    label = "App Usage\nRecords"
                )
                StatisticItem(
                    value = "94.2%",
                    label = "Health\nScore"
                )
            }
        }
    }
}

@Composable
private fun StatisticItem(
    value: String,
    label: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF4CAF50)
        )
        Text(
            text = label,
            fontSize = 11.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            lineHeight = 14.sp
        )
    }
}

@Composable
private fun ImportTab(
    importState: DataExportImportViewModel.ImportState,
    importProgress: Float,
    onImportClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Import Data",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2E7D32)
            )
            Text(
                text = "Import battery data from previous exports or other devices.",
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
        
        item {
            ImportOptionsSection(onImportClick = onImportClick)
        }
        
        if (importState != DataExportImportViewModel.ImportState.IDLE) {
            item {
                ImportProgressSection(
                    importState = importState,
                    progress = importProgress
                )
            }
        }
        
        item {
            ImportGuidelinesSection()
        }
    }
}

@Composable
private fun ImportOptionsSection(onImportClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Import Options",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2E7D32)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = onImportClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4CAF50)
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Import",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Select File to Import",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Supported formats:",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF2E7D32)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            SupportedFormatItem(
                format = "JSON Files (.json)",
                description = "VoltAI exported JSON data files"
            )
            SupportedFormatItem(
                format = "CSV Files (.csv)",
                description = "Spreadsheet-compatible data files"
            )
            SupportedFormatItem(
                format = "Backup Files (.json)",
                description = "Complete VoltAI backup files"
            )
        }
    }
}

@Composable
private fun SupportedFormatItem(
    format: String,
    description: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = "Supported",
            tint = Color(0xFF4CAF50),
            modifier = Modifier.size(16.dp)
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column {
            Text(
                text = format,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF2E7D32)
            )
            Text(
                text = description,
                fontSize = 11.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
private fun ImportProgressSection(
    importState: DataExportImportViewModel.ImportState,
    progress: Float
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                when (importState) {
                    DataExportImportViewModel.ImportState.VALIDATING -> {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color(0xFF4CAF50),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Validating import file...",
                            fontSize = 16.sp,
                            color = Color(0xFF2E7D32)
                        )
                    }
                    DataExportImportViewModel.ImportState.IMPORTING -> {
                        CircularProgressIndicator(
                            progress = progress,
                            modifier = Modifier.size(20.dp),
                            color = Color(0xFF4CAF50),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Importing data... ${(progress * 100).toInt()}%",
                            fontSize = 16.sp,
                            color = Color(0xFF2E7D32)
                        )
                    }
                    DataExportImportViewModel.ImportState.COMPLETED -> {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Complete",
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Import completed successfully!",
                            fontSize = 16.sp,
                            color = Color(0xFF4CAF50),
                            fontWeight = FontWeight.Bold
                        )
                    }
                    DataExportImportViewModel.ImportState.ERROR -> {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Error",
                            tint = Color(0xFFD32F2F),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Import failed",
                            fontSize = 16.sp,
                            color = Color(0xFFD32F2F),
                            fontWeight = FontWeight.Bold
                        )
                    }
                    else -> {}
                }
            }
            
            if (importState == DataExportImportViewModel.ImportState.IMPORTING) {
                Spacer(modifier = Modifier.height(12.dp))
                LinearProgressIndicator(
                    progress = progress,
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFF4CAF50)
                )
            }
        }
    }
}

@Composable
private fun ImportGuidelinesSection() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFF3E0)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Info",
                    tint = Color(0xFFFF9800),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Import Guidelines",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFE65100)
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "• Only import files exported from VoltAI Smart Battery Guardian\n" +
                      "• Large files may take several minutes to import\n" +
                      "• A backup will be created automatically before importing\n" +
                      "• Duplicate data will be handled according to your settings\n" +
                      "• Import process cannot be undone once completed",
                fontSize = 12.sp,
                color = Color(0xFFBF360C),
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
private fun CloudBackupTab(
    cloudBackups: List<DataExportImportViewModel.CloudBackupItem>,
    onCreateBackup: () -> Unit,
    onRestoreBackup: (String) -> Unit,
    onDeleteBackup: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Cloud Backup",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2E7D32)
            )
            Text(
                text = "Automatically backup your data to Google Drive and sync across devices.",
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
        
        item {
            CloudBackupActionsSection(onCreateBackup = onCreateBackup)
        }
        
        item {
            CloudBackupListSection(
                backups = cloudBackups,
                onRestoreBackup = onRestoreBackup,
                onDeleteBackup = onDeleteBackup
            )
        }
    }
}

@Composable
private fun CloudBackupActionsSection(onCreateBackup: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Backup Actions",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2E7D32)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = onCreateBackup,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4CAF50)
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Create Backup",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Create Cloud Backup",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "Creates an encrypted backup of all your battery data and settings in Google Drive.",
                fontSize = 12.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun CloudBackupListSection(
    backups: List<DataExportImportViewModel.CloudBackupItem>,
    onRestoreBackup: (String) -> Unit,
    onDeleteBackup: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Cloud Backups",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2E7D32)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (backups.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "No backups",
                        tint = Color.Gray,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No cloud backups found",
                        fontSize = 16.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Create your first backup to get started",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                backups.forEach { backup ->
                    CloudBackupItem(
                        backup = backup,
                        onRestore = { onRestoreBackup(backup.id) },
                        onDelete = { onDeleteBackup(backup.id) }
                    )
                    
                    if (backup != backups.last()) {
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun CloudBackupItem(
    backup: DataExportImportViewModel.CloudBackupItem,
    onRestore: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF1F8E9)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = backup.name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2E7D32)
                    )
                    Text(
                        text = SimpleDateFormat("MMM dd, yyyy 'at' HH:mm", Locale.getDefault())
                            .format(java.util.Date(backup.modifiedTime)),
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = "Size: ${formatFileSize(backup.size)}",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }
                
                Row {
                    IconButton(
                        onClick = onRestore,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = "Restore",
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = Color(0xFFD32F2F),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

private fun formatFileSize(bytes: Long): String {
    val kb = bytes / 1024.0
    val mb = kb / 1024.0
    return when {
        mb >= 1 -> String.format("%.1f MB", mb)
        kb >= 1 -> String.format("%.1f KB", kb)
        else -> "$bytes bytes"
    }
}
