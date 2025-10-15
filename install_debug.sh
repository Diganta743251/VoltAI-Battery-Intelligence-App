#!/bin/bash

# VoltAI SmartBatteryGuardian Debug Installation Script
# This script builds and installs the debug version of the app

echo "=== VoltAI SmartBatteryGuardian Debug Build & Install ==="
echo ""

# Check if ADB is available
if ! command -v adb &> /dev/null; then
    echo "❌ ADB is not installed or not in PATH"
    echo "Please install Android SDK Platform Tools and ensure 'adb' is accessible"
    exit 1
fi

# Check if device is connected
echo "🔍 Checking for connected devices..."
DEVICE_COUNT=$(adb devices | grep -v "List of devices" | grep -c "device$")

if [ $DEVICE_COUNT -eq 0 ]; then
    echo "❌ No Android devices/emulators connected"
    echo "Please connect your device or start an emulator"
    exit 1
fi

echo "✅ Found $DEVICE_COUNT connected device(s)"
echo ""

# Build the debug APK
echo "🏗️  Building debug APK..."
./gradlew assembleDebug

if [ $? -ne 0 ]; then
    echo "❌ Build failed"
    exit 1
fi

echo "✅ Build successful"
echo ""

# Install the APK
echo "📱 Installing VoltAI SmartBatteryGuardian on device..."
APK_PATH="app/build/outputs/apk/debug/app-debug.apk"

if [ ! -f "$APK_PATH" ]; then
    echo "❌ APK file not found at $APK_PATH"
    exit 1
fi

adb install -r "$APK_PATH"

if [ $? -eq 0 ]; then
    echo "✅ Installation successful!"
    echo ""
    echo "🚀 Debug Features Available:"
    echo "   • Tap the Info (i) icon in the top bar to access Battery Data Debug"
    echo "   • The debug screen shows comprehensive battery information"
    echo "   • Data quality indicators help verify real vs. static data"
    echo "   • Refresh button updates data in real-time"
    echo ""
    echo "📋 To test data collection:"
    echo "   1. Open the app on your device"
    echo "   2. Tap the info icon to open debug screen"
    echo "   3. Check 'Data Quality Check' section for real data indicators"
    echo "   4. Use the refresh button to see live updates"
    echo "   5. Connect/disconnect charger to see dynamic changes"
    echo ""
else
    echo "❌ Installation failed"
    exit 1
fi
