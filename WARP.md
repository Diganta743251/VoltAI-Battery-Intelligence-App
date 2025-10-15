# WARP.md

This file provides guidance to WARP (warp.dev) when working with code in this repository.

## Common Development Commands

### Building and Running
```bash
# Build debug APK
./gradlew assembleDebug

# Install debug on connected device
./gradlew installDebug

# Build release (requires signing setup)
./gradlew assembleRelease

# Quick build script (Windows PowerShell)
./build-app.ps1

# Clean build
./gradlew clean build
```

### Testing
```bash
# Run all unit tests
./gradlew test

# Run instrumentation tests (requires device/emulator)
./gradlew connectedAndroidTest

# Test specific module
./gradlew :core:test
./gradlew :features:test

# Run tests with coverage
./gradlew jacocoTestReport
```

### Code Quality
```bash
# Lint check
./gradlew lint

# Kotlin lint
./gradlew ktlintCheck

# Detekt static analysis
./gradlew detekt

# Format code with ktlint
./gradlew ktlintFormat
```

### Module-Specific Commands
```bash
# Build specific modules
./gradlew :app:assembleDebug
./gradlew :core:build
./gradlew :features:build
./gradlew :ui:build
```

## Architecture Overview

### Modular Architecture
VoltAI uses a multi-module architecture with clear separation of concerns:

- **app/**: Main application module and entry points (MainActivity, BatteryGuruActivity, SimpleMainActivity)
- **core/**: Shared utilities, dependency injection (Hilt), analytics engine, AI integration
- **data/**: Repository implementations, Room database, local preferences
- **domain/**: Business logic, models, use cases, repository interfaces
- **features/**: Feature-specific screens and ViewModels (dashboard, health, temperature, etc.)
- **ui/**: Shared UI components, themes, custom icons, animations
- **service/**: Background services for battery monitoring
- **widget/**: Home screen widgets and Quick Settings tiles
- **backup/**: Data backup and cloud sync functionality

### Key Components

#### Battery Data Flow
1. **BatteryMonitor** (core/utils) - Real-time battery status collection using Android BatteryManager
2. **BatteryDataCollector** (service/) - Background data collection service
3. **BatteryRepository** (data/) - Data persistence and management
4. **Dashboard ViewModels** (features/) - UI state management

#### AI Integration
- **AIBatteryAnalyzer** (core/ai) - Multi-provider AI analysis (OpenAI, Gemini, Claude, TensorFlow Lite)
- **TensorFlowLiteHelper** - On-device machine learning for battery predictions
- **RuleBasedCoach** - Fallback analysis when AI is disabled

#### Real-time Monitoring
- Uses Android's `BatteryManager` and broadcast receivers
- Background service for continuous data collection
- Foreground service notifications for persistent monitoring
- 1-5 second update intervals for real-time UI

### Data Models
- **BatteryStatus** - Real-time battery information
- **BatteryLog** - Historical data points for persistence
- **BatteryAnalytics** - Aggregated insights and recommendations

## Development Guidelines

### Dependency Injection (Hilt)
- Use `@HiltViewModel` for ViewModels
- Inject repositories and use cases via constructor injection
- Service modules defined in core module
- Application class annotated with `@HiltAndroidApp`

### Compose UI Patterns
- Custom Canvas-based icons (no Material defaults) in `ui/components/AppIcons.kt`
- Animated components using Compose animation APIs
- VoltAI theme with Electric Blue color scheme
- Responsive layouts for phone/tablet using `LazyVerticalStaggeredGrid`

### Battery Data Convention
- **Current values**: Negative = charging, Positive = discharging
- **Temperature**: Celsius (°C)
- **Voltage**: Volts (V)
- **Percentage**: 0-100 integer

### Background Processing
- Use `BatteryMonitorService` for persistent monitoring
- WorkManager for scheduled tasks
- Foreground service with notification for data collection
- Respect battery optimization settings

### Testing Strategy
- Unit tests in `*/src/test/` directories
- Instrumentation tests in `*/src/androidTest/`
- Use JUnit 4.13.2, Espresso 3.5.1, and Compose testing
- Mock battery data for consistent testing

### Build Configuration
- Gradle 8.11.1 with Kotlin 1.9.0
- Compose BOM 2023.08.00
- Min SDK 24, Target/Compile SDK 34
- Multi-module build with dependency management in root build.gradle

### Signing and Release
- Release keystore at `keystore/voltai.jks`
- Environment variables for signing: `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`
- ProGuard rules for TensorFlow Lite and Hilt
- Release builds require proper signing configuration

### Performance Considerations
- Battery monitoring frequency: Balance accuracy with battery impact
- Use coroutines with appropriate dispatchers (IO for database, Default for computation)
- Implement proper lifecycle management for ViewModels
- Cache expensive calculations in analytics engine

### Privacy and Permissions
- `PACKAGE_USAGE_STATS` permission for app usage data
- Local data encryption using Android's EncryptedSharedPreferences
- Optional cloud backup (user-initiated)
- Privacy-first approach with opt-in telemetry

### Module Dependencies Flow
```
app -> features, ui, service, widget, backup
features -> domain, ui, core
ui -> core
data -> domain, core
service -> data, domain, core
core -> domain (utilities only)
domain -> (no dependencies - pure business logic)
```

## Important Notes

### Custom Components
- All icons are Canvas-based implementations in `AppIcons.kt`
- Animated battery ring with charging glow effects
- Live current graph with real-time data visualization
- Theme toggle with smooth dark/light mode transitions

### AI Features
- Multiple AI providers supported with fallback to rule-based analysis
- TensorFlow Lite for offline AI predictions
- Configurable AI settings through repository pattern
- Battery health scoring algorithm based on temperature, voltage, and cycles

### Data Export/Import
- CSV export functionality
- Google Drive integration for cloud backup
- Encrypted local storage
- Historical data persistence with Room database

When working with this codebase, maintain the modular architecture, follow the established patterns for battery data handling, and ensure proper testing coverage for new features.

<citations>
<document>
    <document_type>WARP_DOCUMENTATION</document_type>
    <document_id>getting-started/quickstart-guide/coding-in-warp</document_id>
</document>
</citations>
