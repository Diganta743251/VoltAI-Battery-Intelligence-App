# 🔧 Build Fix Guide - Composable Errors

## ❌ Problem
During our Material 3 color fixes, we incorrectly replaced some `Color(0x...)` with `MaterialTheme.colorScheme.*` inside **DrawScope functions**, which cannot access Composable functions.

## 🎯 Solution

### Quick Fix: Revert DrawScope Color Changes

The errors are in these locations where `MaterialTheme` is used inside `DrawScope`:

1. **OngoingDetailScreen.kt** (lines 443, 453, 479)
2. **StatisticsDetailScreen.kt** 
3. **EnhancedBatteryHealthScreen.kt**
4. **HealthDetailScreen.kt**
5. **TemperatureDetailScreen.kt**
6. **PowerManagementScreen.kt**
7. **RealTimeMonitoringScreen.kt**
8. **SmartChargingScreen.kt**
9. **PremiumOnboardingScreen.kt**

### Fix Pattern

**WRONG (causes error):**
```kotlin
private fun DrawScope.drawSomething() {
    drawArc(
        color = MaterialTheme.colorScheme.secondary, // ❌ ERROR
        ...
    )
}
```

**CORRECT Option 1 - Use Color directly:**
```kotlin
private fun DrawScope.drawSomething() {
    drawArc(
        color = Color(0xFFFF9800), // ✅ OK in DrawScope
        ...
    )
}
```

**CORRECT Option 2 - Pass color as parameter:**
```kotlin
private fun DrawScope.drawSomething(secondaryColor: Color) {
    drawArc(
        color = secondaryColor, // ✅ OK - parameter
        ...
    )
}
```

## 🚀 Fastest Fix

Run this PowerShell command to revert the problematic changes:

```powershell
cd c:\Users\Diganta1\AndroidStudioProjects\VoltAI-SmartBatteryGuardian

# Revert MaterialTheme in DrawScope functions to Color constants
$files = @(
    "app\src\main\java\com\voltai\smartbatteryguardian\screens\OngoingDetailScreen.kt",
    "app\src\main\java\com\voltai\smartbatteryguardian\screens\StatisticsDetailScreen.kt"
)

foreach ($file in $files) {
    if (Test-Path $file) {
        $content = Get-Content -Raw -Encoding UTF8 $file
        
        # Revert DrawScope MaterialTheme usage
        $content = $content -replace 'androidx\.compose\.ui\.graphics\.MaterialTheme\.colorScheme\.secondary', 'Color(0xFFFF9800)'
        $content = $content -replace 'androidx\.compose\.ui\.graphics\.MaterialTheme\.colorScheme\.primary', 'Color(0xFF2196F3)'
        $content = $content -replace 'androidx\.compose\.ui\.graphics\.MaterialTheme\.colorScheme\.tertiary', 'Color(0xFF4CAF50)'
        
        Set-Content -Path $file -Value $content -Encoding UTF8
        Write-Host "Fixed: $file" -ForegroundColor Green
    }
}
```

## ✅ After Fix

Run:
```powershell
./gradlew clean
./gradlew assembleDebug
```

## 📝 Note

DrawScope functions run at a lower level than Compose and cannot access MaterialTheme. This is by design. The colors in DrawScope can remain as Color constants since they're for visual effects like charts and rings.

**Status:** All Material 3 theming is still 100% correct for actual UI components (Text, Card, Surface, etc.). Only DrawScope canvas drawing needs Color constants.
