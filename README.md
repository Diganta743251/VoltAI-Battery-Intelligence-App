# 🔋 VoltAI – Battery Intelligence App

![VoltAI Banner](https://via.placeholder.com/1200x400/1E1E1E/FFFFFF?text=VoltAI+-+Smart+Battery+Monitoring)  
*Modern. Accurate. Animated.*

VoltAI is a next-generation battery monitoring application that transforms raw device data into a **beautiful, animated, and intelligent dashboard**. Built with **Jetpack Compose**, Material 3, and real-time system integration, VoltAI delivers accurate battery insights with a premium user experience.

---

## 🚀 Features

✅ **Real-Time Battery Monitoring**  
✅ **Animated Circular Battery Ring with Charging Glow**  
✅ **Live Electric Current Graph (1Hz updates)**  
✅ **Battery Health Estimation Algorithm**  
✅ **Dark/Light Mode Toggle with Smooth Animation**  
✅ **Lottie Animations for Charging & Onboarding**  
✅ **Responsive Staggered Grid Layout**  
✅ **Shimmer Loading States (Skeleton Screens)**  
✅ **Accessibility-First Design (WCAG AA Compliant)**  
✅ **System Sensor Integration (BatteryManager, UsageStatsManager)**  
✅ **Custom SVG-Style Icons in Compose**  
✅ **Haptic Feedback & Micro-Interactions**

---

## 📱 Screens Overview (Dedicated Screen Per Feature)

VoltAI follows a **Battery Guru-style architecture** with **dedicated screens for each battery metric**:

| Screen | Description | Key Components |
|-------|-------------|---------------|
| **Dashboard** | Real-time battery stats overview | Animated ring, staggered cards, parallax header |
| **Battery Level** | Deep dive into battery percentage | Animated ring, time estimates, calibration |
| **Health** | Track long-term battery degradation | Health %, cycle count, capacity comparison |
| **Temperature** | Monitor thermal health | Animated gauge, color zones, history |
| **Voltage** | Track voltage stability | Real-time graph, range indicators |
| **Current** | Monitor power draw | Live flow graph, app-wise consumption |
| **Usage** | Track mAh consumed | Daily usage, screen-on vs standby |
| **Screen Time** | Track screen-on duration | Progress ring, daily trends |
| **Cycle Count** | Track charge cycles | Progress bar, reset calibration |
| **Forecast** | Predict battery life | Time remaining, adaptive forecasting |
| **Charging** | Monitor charging behavior | Lottie animation, charging speed |
| **History** | View trends over time | 7/30-day views, export data |
| **Tools** | Quick actions & diagnostics | Battery saver, optimization tools |
| **Settings** | Customize app behavior | Theme toggle, haptics, export |
| **Onboarding** | Lottie-powered setup | Permission requests, guidance |
| **About** | App info & credits | Version, license, feedback |

---

## 🎨 Design & UI

- **Material Design 3** with dynamic colors (Android 12+)
- **Neumorphic-inspired cards** with 20dp radius and elevation
- **Animated gradients** and **pulse effects** for charging
- **Parallax header** with depth
- **Staggered entry animations** for all cards
- **Responsive layout** for phone & tablet

### Color Palette
| Role | Color |
|------|-------|
| Primary | `#0066CC` (Electric Blue) |
| Secondary | `#6200EE` (Deep Purple) |
| Background | Light: `#F8F9FA`, Dark: `#121212` |
| Accent (Charging) | `#4CAF50` (Green Glow) |

---

## 🧩 Key Components

### 1. **Animated Battery Ring**
Smooth circular progress with charging glow animation.

```kotlin
// Uses custom Canvas implementation (no default icons)
AnimatedBatteryRing(
    batteryLevel = 78f,
    isCharging = true
)
```

### 2. **Live Current Graph**
Real-time visualization of power draw with custom animations.

```kotlin
// Uses custom Canvas implementation
LiveCurrentGraph(data = listOf(1.2f, 2.1f, 1.8f, 3.0f))
```

### 3. **Custom Compose Icons**
Lightweight, scalable SVG-style icons (all implemented in `AppIcons.kt`):

```kotlin
// All icons are Canvas-based - NO default Material icons
BatteryIcon(tint = MaterialTheme.colorScheme.primary)
HealthIcon()
ThermometerIcon()
FlashIcon()
GraphIcon()
SunIcon()
MoonIcon()
```

### 4. **Animated Theme Toggle**
Smooth dark/light mode switch with size & color animation (pure Compose implementation):

```kotlin
// No XML drawables - fully implemented in Compose
ThemeToggle(
    isDarkTheme = isDarkMode,
    onToggle = { isDarkMode = !isDarkMode }
)
```

### 5. **Lottie Animations**
Premium visuals for charging and onboarding:

```kotlin
// Lottie integration for premium animations
ChargingLottieAnimation(isCharging = true)
```

### 6. **Shimmer Loading**
Skeleton screens during data fetch:

```kotlin
// Custom shimmer effect with gradient animation
ShimmerEffect()
```

---

## ⚙️ Real-Time Data Integration

### Sensors Used
| Data | Source | Update Rate |
|------|--------|-------------|
| Battery Level | `BatteryManager.EXTRA_LEVEL` | 1s |
| Temperature | `EXTRA_TEMPERATURE` | 1s |
| Voltage | `EXTRA_VOLTAGE` | 1s |
| Charging Status | `BATTERY_STATUS_CHARGING` | 1s |
| Current Draw | `CURRENT_NOW` (if supported) | 1s |
| Screen On Time | `UsageStatsManager` | 5s |
| Battery Health | Custom algorithm (cycles, temp, voltage) | 10s |

### Battery Health Algorithm
```kotlin
// Realistic degradation model
health = (currentCapacity / designCapacity) * 100
    - (cycleCount * 0.05)  // ~0.05% per cycle
    - (temp > 35°C ? (temp - 35) * 0.02 : 0)  // temp impact
    - (lowVoltage ? 3–5%)  // voltage degradation
```

---

## 🌐 Responsive Layouts

| Device | Layout |
|-------|--------|
| Phone (Portrait) | `LazyVerticalStaggeredGrid(2)` |
| Phone (Landscape) | `LazyVerticalGrid(3)` |
| Tablet (Portrait) | `LazyVerticalGrid(3)` |
| Tablet (Landscape) | `LazyHorizontalGrid(2)` + side nav |

---

## ♿ Accessibility

- ✅ **Screen Reader Support** (Full semantics with custom descriptions)
- ✅ **High Contrast Mode** (Enhanced for all screens)
- ✅ **Large Text Scaling** (Respects system preferences)
- ✅ **Keyboard Navigation** (Full focus management)
- ✅ **Live Regions** for dynamic content updates
- ✅ **Reduced Motion** support (`prefersReducedMotion` respected)

---

## 📦 Installation

### 1. Clone the Repo
```bash
git clone https://github.com/yourusername/voltai.git
cd voltai
```

### 2. Add Lottie Dependency
In `app/build.gradle`:
```gradle
implementation "com.airbnb.android:lottie-compose:6.3.0"
```

### 3. Add Permissions
In `AndroidManifest.xml`:
```xml
<uses-permission android:name="android.permission.PACKAGE_USAGE_STATS" />
```

> ⚠️ Users must enable Usage Access manually in Settings.

---

## 🛠️ Setup & Usage

### 1. Onboarding Flow
1. Welcome Screen (Lottie animation)
2. Request Usage Access (with explanation)
3. Setup Complete (Confetti Lottie)

### 2. Navigation System
- **Bottom Navigation**: Dashboard, Health, Current, History, Tools
- **Dashboard Cards**: Tap to deep dive into specific metrics
- **Navigation Drawer**: Full list of all screens (for power users)

### 3. Settings
- Toggle Dark/Light Mode (with smooth animation)
- Enable/Disable Haptics
- Data Export (CSV format)
- Reset cycle count (for calibration)

---

## 🧪 Testing

| Test | Command |
|------|--------|
| Unit Tests | `./gradlew test` |
| Instrumentation | `./gradlew connectedAndroidTest` |
| Performance | Profile with **Android Studio Profiler** |
| Accessibility | Test with **TalkBack** and **Accessibility Scanner** |

---

## 📁 Project Structure

```
VoltAI-SmartBatteryGuardian/
├── app/
│   └── MainActivity.kt           # Entry point, uses VoltAITheme
├── core/                         # Shared utilities
│   ├── utils/
│   │   ├── BatteryStatus.kt      # Data class
│   │   └── Extensions.kt
├── data/                         # Data sources
│   ├── BatteryMonitor.kt         # Real-time sensor polling
│   ├── BatteryHealthCalculator.kt
│   └── UsageStatsHelper.kt
├── features/                     # Feature modules (one per screen)
│   ├── dashboard/
│   │   └── DashboardScreen.kt
│   ├── health/
│   │   └── HealthScreen.kt
│   ├── temperature/
│   │   └── TemperatureScreen.kt
│   ├── voltage/
│   │   └── VoltageScreen.kt
│   ├── current/
│   │   └── CurrentScreen.kt
│   ├── usage/
│   │   └── UsageScreen.kt
│   ├── screen_time/
│   │   └── ScreenTimeScreen.kt
│   ├── cycle_count/
│   │   └── CycleCountScreen.kt
│   ├── forecast/
│   │   └── ForecastScreen.kt
│   ├── charging/
│   │   └── ChargingScreen.kt
│   ├── tools/
│   │   └── ToolsScreen.kt
│   ├── settings/
│   │   └── SettingsScreen.kt
│   ├── history/
│   │   └── HistoryScreen.kt
│   └── onboarding/
│       ├── WelcomeScreen.kt
│       ├── PermissionScreen.kt
│       └── CompleteScreen.kt
├── ui/                           # Shared UI components
│   ├── components/
│   │   ├── AppIcons.kt                     # ✅ Your custom icons
│   │   ├── AnimatedBatteryRing.kt          # ✅ Your animated ring
│   │   ├── ThemeToggle.kt                  # ✅ Your animated toggle
│   │   ├── ShimmerEffect.kt                # ✅ Your shimmer
│   │   ├── ChargingLottieAnimation.kt      # ✅ Lottie integration
│   │   └── MetricCard.kt                   # Reusable card
│   └── theme/
│       └── VoltAITheme.kt                  # ✅ Your Electric Blue theme
└── build.gradle (project)                  # Correct module dependencies
```

> **Important**: All icons are implemented as Compose Canvas components in `AppIcons.kt` - **NO default Material icons** are used.

---

## 🤝 Contributing

We welcome contributions! Please follow these steps:

1. Fork the repo
2. Create your feature branch (`git checkout -b feature/awesome`)
3. Commit your changes (`git commit -m 'Add some feature'`)
4. Push to the branch (`git push origin feature/awesome`)
5. Open a Pull Request

**Critical Contribution Guidelines:**
- ✅ **Never** use `Icons.Default.*` - always use custom Canvas icons
- ✅ Respect the "dedicated screen per feature" architecture
- ✅ Maintain consistent animation patterns across all screens
- ✅ Follow the responsive layout strategy for all new components

---

## 📄 License

```
MIT License

Copyright (c) 2025 VoltAI Team

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files, to deal in the Software
without restriction, including without limitation the rights to use, copy,
modify, merge, publish, distribute, sublicense, and/or sell copies of the
Software, and to permit persons to whom the Software is furnished to do so,
subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

---

## 🎉 Screenshots

| Dashboard | Health Screen | Theme Toggle |
|-----------|---------------|--------------|
| ![Dashboard](https://via.placeholder.com/300x600/1E1E1E/FFFFFF?text=Dashboard+with+Animated+Ring) | ![Health](https://via.placeholder.com/300x600/1E1E1E/FFFFFF?text=Health+Screen+with+Degradation+Chart) | ![Theme](https://via.placeholder.com/300x600/1E1E1E/FFFFFF?text=Animated+Theme+Toggle) |

| Current Graph | Temperature Screen | Charging Animation |
|---------------|--------------------|--------------------|
| ![Current](https://via.placeholder.com/300x600/1E1E1E/FFFFFF?text=Live+Current+Graph) | ![Temp](https://via.placeholder.com/300x600/1E1E1E/FFFFFF?text=Temperature+Gauge) | ![Charging](https://via.placeholder.com/300x600/1E1E1E/FFFFFF?text=Lottie+Charging+Animation) |

---

**Built with ❤️ for Android developers and power users.**  
🔋 *Know your battery. Own your device.*