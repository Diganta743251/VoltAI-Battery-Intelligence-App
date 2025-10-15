# Quick fix for Composable errors - Replace MaterialTheme in DrawScope

$files = @(
    "OngoingDetailScreen.kt",
    "StatisticsDetailScreen.kt",
    "EnhancedBatteryHealthScreen.kt",
    "HealthDetailScreen.kt",
    "TemperatureDetailScreen.kt",
    "PowerManagementScreen.kt",
    "RealTimeMonitoringScreen.kt",
    "SmartChargingScreen.kt",
    "PremiumOnboardingScreen.kt"
)

$basePath = "c:\Users\Diganta1\AndroidStudioProjects\VoltAI-SmartBatteryGuardian\app\src\main\java\com\voltai\smartbatteryguardian\screens\"

Write-Host "Fixing Composable errors..." -ForegroundColor Cyan

foreach ($file in $files) {
    $path = $basePath + $file
    if (Test-Path $path) {
        $content = Get-Content -Raw -Encoding UTF8 $path
        
        # Fix: androidx.compose.ui.graphics.MaterialTheme -> comment out (needs manual fix)
        $content = $content -replace 'androidx\.compose\.ui\.graphics\.MaterialTheme', '/* MaterialTheme - FIX: pass color as parameter */'
        
        Set-Content -Path $path -Value $content -Encoding UTF8
        Write-Host "  Fixed: $file" -ForegroundColor Green
    }
}

Write-Host "`nDone! Manual review needed for DrawScope functions." -ForegroundColor Yellow
Write-Host "These functions need colors passed as parameters, not accessed from MaterialTheme." -ForegroundColor Yellow
