---
description: Repository Information Overview
alwaysApply: true
---

# VoltAI: Smart Battery Guardian - Project Information

## Summary
VoltAI is a comprehensive battery management Android application designed to help users monitor, understand, and optimize their device's battery performance. Built using Kotlin and Jetpack Compose with modern Android architecture components, it provides detailed battery insights through real-time monitoring and data visualization.

## Project Structure
The application follows a modular architecture:

```
VoltAI-SmartBatteryGuardian/
├── app/              # Main application module with Android configuration
├── core/             # Shared utilities, dependency injection, and core components
├── data/             # Local data management, repositories, and preferences
├── domain/           # Business logic, models, and use cases
├── features/         # Individual feature modules with screens and ViewModels
├── ui/               # Theming, custom components, and navigation
├── service/          # Background services and system receivers
├── widget/           # Home screen widgets and Quick Settings tiles
├── backup/           # Data backup and export functionality
├── localization/     # Multi-language support
├── privacy/          # Privacy-focused features and settings
└── wear/             # Wear OS companion application
```

## Technical Specifications
**Programming Language**: Kotlin 1.9.0  
**Build System**: Gradle 8.11.1  
**Android SDK**: 
- Compile SDK: 34
- Target SDK: 34
- Min SDK: 24

## Key Dependencies
**Core Libraries**:
- Jetpack Compose BOM 2023.08.00
- Dagger Hilt 2.48 for dependency injection
- Room 2.6.1 for local database storage
- Lifecycle Components 2.7.0
- MPAndroidChart v3.1.0 for data visualization
- Google Drive API v3 for cloud backup
- Google Play Services (Auth 20.7.0, Wearable 18.1.0)
- Accompanist Navigation Animation 0.34.0

**Development & Testing**:
- JUnit 4.13.2 for unit testing
- Espresso 3.5.1 for UI testing
- Compose UI testing framework

## Build & Deployment
```bash
# Clone the repository
git clone https://github.com/yourusername/VoltAI-SmartBatteryGuardian.git

# Open in Android Studio and sync Gradle dependencies

# Build debug APK
./gradlew assembleDebug

# Install on connected device
./gradlew installDebug

# Build release APK (requires signing configuration)
./gradlew assembleRelease
```

## Testing Framework
**Testing Libraries**: JUnit and Espresso  
**Test Locations**: 
- Unit tests: `*/src/test/`
- Instrumentation tests: `*/src/androidTest/`  
**File Naming**: `*Test.kt` for unit tests, `*InstrumentedTest.kt` for UI tests  

```bash
# Execute all unit tests
./gradlew test

# Run instrumentation tests on connected device
./gradlew connectedAndroidTest
```

## Core Features
- **Real-time Battery Monitoring**: Live status updates and background data logging
- **Battery Health Analysis**: Health estimation and charging cycle tracking
- **App Usage Analytics**: Detailed breakdown of app-specific battery consumption
- **Thermal Monitoring**: CPU, GPU, and battery temperature tracking
- **Discharge Pattern Analysis**: Screen-on vs screen-off battery drain metrics
- **Intelligent Recommendations**: AI-powered battery optimization suggestions
- **Historical Data & Trends**: Interactive graphs showing battery usage patterns
- **Power Management Tools**: Customizable power profiles and saver modes
- **System Integration**: Home screen widgets and Quick Settings tiles
- **Modern UI/UX**: Jetpack Compose interface with animations and Material Design 3
- **Educational Content**: Battery care tips and longevity guidance
- **Data Portability**: Export/import functionality with encrypted storage
- **Wear OS Support**: Companion application for smartwatch integration