---
description: Repository Information Overview
alwaysApply: true
---

# VoltAI: Smart Battery Guardian Information

## Summary
VoltAI is a full-featured battery management Android application designed to help users monitor and optimize their device's battery performance. Built using Kotlin and Jetpack Compose, it leverages modern Android architecture and AI-powered insights to provide a comprehensive battery management experience.

## Structure
- **app**: Main application module with Android application configuration
- **core**: Contains dependency injection, utility functions, and AI components
- **data**: Manages local storage, repositories, and user preferences
- **domain**: Defines models, use cases, and repository interfaces
- **features**: Comprises individual screens and their corresponding ViewModels
- **ui**: Contains theming, components, and navigation setup
- **service**: Implements background services and broadcast receivers
- **widget**: Manages home screen widgets and quick settings tiles
- **backup**: Handles encrypted backups and log exports
- **localization**: Supports multiple languages
- **privacy**: Manages privacy settings
- **wear**: Companion app for Wear OS (smartwatches)

## Language & Runtime
**Language**: Kotlin
**Version**: Kotlin 1.9.0
**Build System**: Gradle 8.11.1
**Package Manager**: Gradle
**Android SDK**: 
- Compile SDK: 34
- Target SDK: 34
- Min SDK: 24

## Dependencies
**Main Dependencies**:
- Jetpack Compose (BOM 2023.08.00)
- Dagger Hilt 2.48 (Dependency Injection)
- Room 2.6.1 (Local Database)
- Lifecycle Components 2.7.0
- MPAndroidChart v3.1.0 (Data Visualization)
- Google Drive API v3 (Cloud Backup)
- Google Play Services (Auth 20.7.0, Wearable 18.1.0)
- Accompanist Navigation Animation 0.34.0
- TensorFlow Lite (Optional, via local AAR files)

**Development Dependencies**:
- JUnit 4.13.2
- Espresso 3.5.1
- Compose UI Testing

## Build & Installation
```bash
# Clone the repository
git clone https://github.com/yourusername/VoltAI-SmartBatteryGuardian.git

# Open in Android Studio
# Optional: Add TensorFlow Lite AAR files to app/libs directory

# Build the application
./gradlew assembleDebug

# Install on connected device
./gradlew installDebug
```

## Testing
**Framework**: JUnit, Espresso
**Test Location**: 
- Unit tests: `*/src/test/`
- Instrumentation tests: `*/src/androidTest/`
**Naming Convention**: `*Test.kt` for unit tests, `*InstrumentedTest.kt` for UI tests
**Run Command**:
```bash
# Run unit tests
./gradlew test

# Run instrumentation tests
./gradlew connectedAndroidTest
```

## Features
- **Battery Monitoring**: Real-time status updates, background logging
- **Battery Health**: Estimation of health, charging cycles tracking
- **App Usage Tracking**: Analysis of app battery drain
- **Thermal Monitoring**: CPU, GPU, and battery temperature monitoring
- **Discharge Analytics**: Screen-on vs screen-off battery drain tracking
- **AI-Powered Recommendations**: Rule-based and on-device predictions
- **History & Trends**: Interactive graphs for battery usage
- **Power Saver Tools**: Customizable profiles, Ultra Battery Saver
- **Widgets and Tiles**: Home screen widgets, Quick Settings tiles
- **Modern UI/UX**: Compose-based interface with animations
- **Tips & Educational Content**: Battery care and longevity information
- **Export/Import**: Backup settings using encrypted storage
- **Wear OS Integration**: Companion app for smartwatches