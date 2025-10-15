# VoltAI SmartBatteryGuardian Debug Installation Script (PowerShell)
# This script builds and installs the debug version of the app

Write-Host "=== VoltAI SmartBatteryGuardian Debug Build & Install ===" -ForegroundColor Cyan
Write-Host ""

# Check if ADB is available
try {
    $null = Get-Command adb -ErrorAction Stop
    Write-Host "✅ ADB found" -ForegroundColor Green
} catch {
    Write-Host "❌ ADB is not installed or not in PATH" -ForegroundColor Red
    Write-Host "Please install Android SDK Platform Tools and ensure 'adb' is accessible" -ForegroundColor Yellow
    exit 1
}

# Check if device is connected
Write-Host "🔍 Checking for connected devices..." -ForegroundColor Yellow
$devices = & adb devices | Where-Object { $_ -match "device$" }
$deviceCount = $devices.Count

if ($deviceCount -eq 0) {
    Write-Host "❌ No Android devices/emulators connected" -ForegroundColor Red
    Write-Host "Please connect your device or start an emulator" -ForegroundColor Yellow
    exit 1
}

Write-Host "✅ Found $deviceCount connected device(s)" -ForegroundColor Green
Write-Host ""

# Build the debug APK
Write-Host "🏗️  Building debug APK..." -ForegroundColor Yellow
& .\gradlew.bat assembleDebug

if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Build failed" -ForegroundColor Red
    exit 1
}

Write-Host "✅ Build successful" -ForegroundColor Green
Write-Host ""

# Install the APK
Write-Host "📱 Installing VoltAI SmartBatteryGuardian on device..." -ForegroundColor Yellow
$apkPath = "app\build\outputs\apk\debug\app-debug.apk"

if (-Not (Test-Path $apkPath)) {
    Write-Host "❌ APK file not found at $apkPath" -ForegroundColor Red
    exit 1
}

& adb install -r $apkPath

if ($LASTEXITCODE -eq 0) {
    Write-Host "✅ Installation successful!" -ForegroundColor Green
    Write-Host ""
    Write-Host "🚀 Debug Features Available:" -ForegroundColor Cyan
    Write-Host "   • Tap the Info (i) icon in the top bar to access Battery Data Debug" -ForegroundColor White
    Write-Host "   • The debug screen shows comprehensive battery information" -ForegroundColor White
    Write-Host "   • Data quality indicators help verify real vs. static data" -ForegroundColor White
    Write-Host "   • Refresh button updates data in real-time" -ForegroundColor White
    Write-Host ""
    Write-Host "📋 To test data collection:" -ForegroundColor Cyan
    Write-Host "   1. Open the app on your device" -ForegroundColor White
    Write-Host "   2. Tap the info icon to open debug screen" -ForegroundColor White
    Write-Host "   3. Check 'Data Quality Check' section for real data indicators" -ForegroundColor White
    Write-Host "   4. Use the refresh button to see live updates" -ForegroundColor White
    Write-Host "   5. Connect/disconnect charger to see dynamic changes" -ForegroundColor White
    Write-Host ""
    Write-Host "✨ Additional Testing Tips:" -ForegroundColor Magenta
    Write-Host "   • Check temperature changes during charging" -ForegroundColor White
    Write-Host "   • Monitor voltage fluctuations" -ForegroundColor White
    Write-Host "   • Observe current values during different states" -ForegroundColor White
    Write-Host "   • Verify screen time data updates" -ForegroundColor White
} else {
    Write-Host "❌ Installation failed" -ForegroundColor Red
    exit 1
}
