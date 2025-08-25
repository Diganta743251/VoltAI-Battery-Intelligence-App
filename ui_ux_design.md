## 🎨 **VoltAI – UI/UX Design Plan (v2.1)**  
*Modern. Animated. Dedicated. Professional.*

> A complete, screen-by-screen UI/UX overhaul transforming VoltAI into a premium battery intelligence platform.

---

## 🔧 **Design Philosophy**

| Principle | Implementation |
|---------|----------------|
| **One Feature, One Screen** | Each battery metric has its own deep-dive screen |
| **No Default Icons** | All icons are custom `Canvas`-based SVG-style (`AppIcons.kt`) |
| **Animated First** | Every screen has purposeful motion (entry, data, feedback) |
| **Responsive Layouts** | Adapts to phone/tablet, portrait/landscape |
| **Accessibility First** | WCAG AA, screen reader, high contrast, reduced motion |
| **Real Data Only** | No mock values — all from `BatteryManager`, sensors, `UsageStatsManager` |

---

## 🏗️ **Project Structure (Fixed & Final)**

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

> ✅ **Delete all `com.example.*` or duplicate files.**  
> ✅ **Never use `Icons.Default.*`. Always use `AppIcons.kt`.**

---

## 📱 **Screen-by-Screen UI/UX Specification**

---

### 1. 🔋 **DashboardScreen**  
`features/dashboard/DashboardScreen.kt`

| Aspect | Specification |
|------|---------------|
| **Purpose** | Central hub with key metrics and navigation |
| **Layout** | `LazyVerticalStaggeredGrid(2)` (phone), `LazyVerticalGrid(3)` (tablet) |
| **Cards** | 6 main cards: Battery, Health, Current, Temp, Voltage, Tools |
| **Entry Animation** | Staggered `slideIn + fadeIn` with 100ms delay per card |
| **Header** | Parallax scrolling with animated gradient |
| **Navigation** | Bottom Nav: Dashboard, Health, Current, History, Tools |
| **Data Source** | `BatteryMonitor.kt` (real-time) |
| **Custom Icons** | `BatteryIcon`, `HealthIcon`, `ThermometerIcon`, `FlashIcon` |
| **Animations** | Shimmer loading, animated battery ring, pulse on update |

---

### 2. 🩺 **HealthScreen**  
`features/health/HealthScreen.kt`

| Aspect | Specification |
|------|---------------|
| **Purpose** | Track long-term battery degradation |
| **Layout** | `LazyColumn` with cards |
| **Key Metrics** | - Health % (e.g., 95%)<br>- Cycle Count (e.g., 127)<br>- Design vs Current Capacity (3800mAh / 4000mAh)<br>- Degradation Rate (0.05%/cycle) |
| **Visuals** | - Animated progress ring (0–100%)<br>- Bar chart of last 30 days<br>- Color-coded: Green (>80%), Yellow (60–80%), Red (<60%) |
| **Animations** | Spring animation for health %, bar chart entry |
| **Custom Icon** | `HealthIcon` |
| **Navigation** | Back to Dashboard, or via Bottom Nav |

---

### 3. 🌡️ **TemperatureScreen**  
`features/temperature/TemperatureScreen.kt`

| Aspect | Specification |
|------|---------------|
| **Purpose** | Monitor thermal health |
| **Layout** | `LazyColumn` |
| **Key Metrics** | - Current Temp (°C/°F)<br>- Min/Max Today<br>- Average Temp<br>- Thermal State (Normal/Warning/Danger) |
| **Visuals** | - Animated thermometer gauge<br>- Color gradient: Blue → Yellow → Red<br>- Historical line graph (last 60 mins) |
| **Alerts** | Vibration + notification if >40°C |
| **Animations** | Smooth gauge update, pulsing warning |
| **Custom Icon** | `ThermometerIcon` |

---

### 4. 🔌 **VoltageScreen**  
`features/voltage/VoltageScreen.kt`

| Aspect | Specification |
|------|---------------|
| **Purpose** | Track voltage stability |
| **Layout** | `LazyColumn` |
| **Key Metrics** | - Current Voltage (mV)<br>- Normal Range (3500–4200mV)<br>- Voltage Drop Events |
| **Visuals** | - Real-time line graph (1Hz updates)<br>- Grid background<br>- Color zones: Green (3.7–4.2V), Yellow (3.5–3.7V), Red (<3.5V) |
| **Animations** | Live graph redraw, color pulse on drop |
| **Custom Icon** | `FlashIcon` |

---

### 5. 🔀 **CurrentScreen**  
`features/current/CurrentScreen.kt`

| Aspect | Specification |
|------|---------------|
| **Purpose** | Monitor real-time power draw |
| **Layout** | `LazyColumn` |
| **Key Metrics** | - Current (mA): Charging (+) / Discharging (-)<br>- Peak Draw<br>- App-wise consumption ranking |
| **Visuals** | - Animated flow graph with gradient fill<br>- Ripple effect on data point update<br>- Grid lines for readability |
| **Animations** | Live graph update every second |
| **Custom Icon** | `FlashIcon` |

---

### 6. 📉 **UsageScreen**  
`features/usage/UsageScreen.kt`

| Aspect | Specification |
|------|---------------|
| **Purpose** | Track total mAh consumed |
| **Layout** | `LazyColumn` |
| **Key Metrics** | - Today’s Usage (e.g., 2140 mAh)<br>- Screen-on vs Standby usage<br>- Daily Average |
| **Visuals** | - Stacked bar chart<br>- mAh counter animation<br>- Export button (CSV) |
| **Animations** | Number counter, bar fill |
| **Custom Icon** | `GraphIcon` |

---

### 7. 📅 **ScreenTimeScreen**  
`features/screen_time/ScreenTimeScreen.kt`

| Aspect | Specification |
|------|---------------|
| **Purpose** | Track screen-on time |
| **Layout** | `LazyColumn` |
| **Key Metrics** | - Today’s Screen Time (e.g., 4h 22m)<br>- Daily Average<br>- Weekly Trend |
| **Visuals** | - Progress ring<br>- Daily bar chart<br>- Compare with battery drain |
| **Animations** | Ring fill, staggered chart entry |
| **Custom Icon** | `EyeIcon` (in `AppIcons.kt`) |

---

### 8. 🔁 **CycleCountScreen**  
`features/cycle_count/CycleCountScreen.kt`

| Aspect | Specification |
|------|---------------|
| **Purpose** | Track charge cycles |
| **Layout** | Simple `Column` |
| **Key Metrics** | - Estimated Cycles (e.g., 127)<br>- Cycle Limit Info (500–800 typical)<br>- “Battery wears with each full cycle” |
| **Visuals** | - Progress bar to 500 cycles<br>- Cycle counter animation |
| **Actions** | “Reset for Calibration” (updates `SharedPreferences`) |
| **Custom Icon** | `RefreshIcon` |

---

### 9. ⏳ **ForecastScreen**  
`features/forecast/ForecastScreen.kt`

| Aspect | Specification |
|------|---------------|
| **Purpose** | Predict battery life |
| **Layout** | Centered `Column` |
| **Key Metrics** | - Time Remaining (e.g., 3h 18m)<br>- Time to Full (e.g., 1h 04m)<br>- Forecast Model (adaptive based on usage) |
| **Visuals** | - Large digital clock style<br>- Small graph of last 10 mins<br>- Adaptive color (green → red as battery drops) |
| **Animations** | Smooth number transitions every 30s |
| **Custom Icon** | `ScheduleIcon` |

---

### 10. ⚡ **ChargingScreen**  
`features/charging/ChargingScreen.kt`

| Aspect | Specification |
|------|---------------|
| **Purpose** | Monitor charging behavior |
| **Layout** | Centered with Lottie |
| **Key Metrics** | - Charging Speed (W)<br>- Charging Stage (Trickle → Fast → Full)<br>- Voltage/Current during charge |
| **Visuals** | - `ChargingLottieAnimation` (sparkles, energy flow)<br>- Charging speed meter<br>- Time to full |
| **Animations** | Lottie loop, glow pulse |
| **Custom Icon** | `FlashIcon` |

---

### 11. 🛠️ **ToolsScreen**  
`features/tools/ToolsScreen.kt`

| Aspect | Specification |
|------|---------------|
| **Purpose** | Quick actions and diagnostics |
| **Layout** | `LazyVerticalGrid(2)` or `3` |
| **Tools** | - Battery Saver<br>- Optimize Apps<br>- Calibrate Battery<br>- Contact Support<br>- User Guide |
| **Visuals** | - Uniform 24dp icons (`AppIcons.kt`)<br>- Color-coded categories<br>- Ripple on tap |
| **Animations** | Scale + elevation lift on press |
| **Custom Icons** | `FlashIcon`, `LightbulbIcon`, `SupportIcon`, etc. |

---

### 12. ⚙️ **SettingsScreen**  
`features/settings/SettingsScreen.kt`

| Aspect | Specification |
|------|---------------|
| **Purpose** | App customization |
| **Layout** | `LazyColumn` with sections |
| **Sections** | - Theme: Auto / Light / Dark<br>- Haptics: On/Off<br>- Update Interval: 1s/5s/10s<br>- Data Export: CSV<br>- Accessibility: High Contrast<br>- About: Version, License |
| **Key Feature** | Animated `ThemeToggle` with size & color transition |
| **Custom Icons** | `SunIcon`, `MoonIcon`, `ExportIcon`, `AccessibilityIcon` |
| **Animations** | Toggle slide, section expand |

---

### 13. 📊 **HistoryScreen**  
`features/history/HistoryScreen.kt`

| Aspect | Specification |
|------|---------------|
| **Purpose** | Long-term trends |
| **Layout** | `LazyColumn` with tabs |
| **Views** | - 7-Day<br>- 30-Day |
| **Metrics** | - Daily Health %<br>- Avg Temp<br>- Usage (mAh)<br>- Charge Cycles |
| **Visuals** | - Line charts<br>- Export all data<br>- Share report |
| **Animations** | Tab swipe, chart fade-in |
| **Custom Icon** | `GraphIcon` |

---

### 14. 🧭 **Onboarding Flow**  
`features/onboarding/`

| Screen | Purpose |
|-------|--------|
| `WelcomeScreen` | Lottie intro, “Welcome to VoltAI” |
| `PermissionScreen` | Request `USAGE_STATS` access with explanation |
| `CompleteScreen` | Confetti Lottie, “Ready to go!” → Open Dashboard |

| **Animations** | Slide transitions, Lottie, button lifts |
| **Navigation** | Sequential, cannot skip |

---

## 🧭 **Navigation System**

| Method | Screens |
|------|--------|
| **Bottom Nav** | Dashboard, Health, Current, History, Tools |
| **Dashboard Cards** | Tap → Deep dive into Temp, Voltage, Cycles, etc. |
| **Navigation Drawer** | Full list of all 14+ screens (accessible via hamburger) |
| **Settings Links** | “View Temperature Trends” → opens `TemperatureScreen` |
| **Deep Linking** | `voltai://health`, `voltai://settings` |

---

## 🎨 **Design System**

| Element | Spec |
|-------|------|
| **Primary Color** | `#0066CC` (Electric Blue) |
| **Secondary** | `#6200EE` (Deep Purple) |
| **Background** | Light: `#F8F9FA`, Dark: `#121212` |
| **Cards** | 20dp rounded corners, 8dp elevation |
| **Spacing** | 12dp between cards, 8dp padding |
| **Typography** | `Inter` or `Poppins` (clean, modern) |
| **Icons** | Custom `Canvas`-based in `AppIcons.kt` |
| **Animations** | Spring, fade, slide, Lottie, shimmer |

---

## ✅ **Next Steps for Implementation**

1. **Fix `MainActivity.kt`** to use `VoltAITheme`
2. **Remove all `Icons.Default.*`** and use `AppIcons.kt`
3. **Implement `DashboardScreen.kt`** with staggered cards
4. **Create `HealthScreen.kt`**, `TemperatureScreen.kt`, etc. one by one
5. **Set up navigation** with `NavHost` and `BottomNavigation`
6. **Integrate `BatteryMonitor.kt`** for real data
7. **Add Lottie files** to `assets/lottie/`
8. **Test on device** with real battery data

---