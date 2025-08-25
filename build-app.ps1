# VoltAI Build Script
Write-Host "🔋 Building VoltAI Smart Battery Guardian..." -ForegroundColor Green

# Stop all Gradle daemons
Write-Host "Stopping Gradle daemons..." -ForegroundColor Yellow
& .\gradlew --stop

# Wait a moment
Start-Sleep -Seconds 2

# Kill any remaining Java processes that might be holding files
Write-Host "Cleaning up processes..." -ForegroundColor Yellow
Get-Process -Name "java" -ErrorAction SilentlyContinue | Where-Object { $_.ProcessName -eq "java" } | Stop-Process -Force -ErrorAction SilentlyContinue

# Clean build directories
Write-Host "Cleaning build directories..." -ForegroundColor Yellow
Remove-Item -Recurse -Force "build", "app\build", "ui\build", "core\build", "data\build", "domain\build", "features\build", "service\build", "widget\build", "backup\build" -ErrorAction SilentlyContinue

# Wait a moment
Start-Sleep -Seconds 3

# Build the app
Write-Host "Building VoltAI app..." -ForegroundColor Green
& .\gradlew assembleDebug --no-daemon --parallel

if ($LASTEXITCODE -eq 0) {
    Write-Host "✅ Build successful! APK location:" -ForegroundColor Green
    Write-Host "app\build\outputs\apk\debug\app-debug.apk" -ForegroundColor Cyan
    
    # Check if APK exists
    if (Test-Path "app\build\outputs\apk\debug\app-debug.apk") {
        $apkSize = (Get-Item "app\build\outputs\apk\debug\app-debug.apk").Length / 1MB
        Write-Host "APK Size: $([math]::Round($apkSize, 2)) MB" -ForegroundColor Cyan
    }
} else {
    Write-Host "❌ Build failed. Check the output above for errors." -ForegroundColor Red
}

Write-Host "🔋 VoltAI build process completed!" -ForegroundColor Green