# Battery Guru Style Enhancements

Based on the provided screenshot, here are the key enhancements to implement:

## ✅ Completed
1. **Minimum Padding** - Reduced to 6dp in view mode, responsive (8/10/12dp) in edit mode
2. **Live Auto-Shift Preview** - Cards move aside while dragging and revert if not dropped
3. **Reliable Gestures** - Fixed tap and 3s long-press in view mode using `detectTapGestures`

## 🚧 In Progress - Battery Guru Charts

### 1. Battery Info Card Enhancement
**Current**: Basic battery level with capsule bar
**Target**: Add color-coded bar chart showing battery level history (like Battery Guru)
- Green bars for levels >= 70%
- Yellow/Amber for 40-69%
- Orange for 20-39%
- Red for < 20%
- Show last 30 readings in a horizontal bar chart
- Smooth 800ms EaseOutCubic animation

### 2. Electric Current Card (CURRENT_VOLTAGE)
**Current**: Basic current/voltage display  
**Target**: Add waveform chart showing current draw over time
- Line chart with 48 data points
- Show min/max current values
- Smooth animation with baseline
- Display "X.X W • XXX.X mV" below chart

### 3. Charging/Discharging Sessions Card (NEW)
**Status**: Need to add as new card type `CHARGING_SESSIONS`
- Bar chart showing charge and discharge cycles
- Green bars for charging sessions
- Red bars for discharging sessions
- Show duration and level change
- Last 10 sessions displayed

### 4. History Card Fix
**Current**: Shows "Remaining XX%" with basic bars
**Target**: Proper charge/discharge history with:
- Bar chart showing charging sessions over time
- Labels showing dates (e.g., "29", "21", "22", "24", "30")
- Distinguish charging vs screen-on time
- Error handling for no data: "No history data available"

### 5. Top Battery Apps Card (NEW)
**Status**: Need to add as new card type `TOP_BATTERY_APPS`
- List of top 3-5 battery-consuming apps
- Show app name, icon, and mAh consumption
- Get data from `AppUsageRepositoryImpl`
- Calculate mAh from foreground time
- Fallback: "No app usage data" if permission denied

## Implementation Files

### New Files Created
- ✅ `BatteryGuruCharts.kt` - Contains:
  - `BatteryLevelBarChart()` - Color-coded bar chart
  - `CurrentDrawWaveform()` - Current waveform chart
  - `ChargingSessionsChart()` - Sessions bar chart
  - `ChargingSession` data class

### Files to Modify
1. **CustomizableHomeState.kt**
   - Add `CHARGING_SESSIONS` card type
   - Add `TOP_BATTERY_APPS` card type

2. **MaterialCardContent.kt**
   - Update `BatteryLevelCard()` to include `BatteryLevelBarChart()`
   - Update `CurrentVoltageCard()` to include `CurrentDrawWaveform()`
   - Add `ChargingSessionsCard()` - NEW
   - Update `HistoryCard()` with proper data and error handling
   - Add `TopBatteryAppsCard()` - NEW

3. **BatteryReadingsViewModel.kt** (already exists)
   - Already has `levelSeries`, `currentSeries`, `last24hReadings`
   - Use these for chart data

4. **AppUsageRepositoryImpl.kt** (already exists)
   - Already fetches app usage stats
   - Use `estimatedBatteryDrain` for mAh calculation

## Data Flow

### Battery Level History
```kotlin
BatteryReadingsViewModel.levelSeries -> BatteryLevelBarChart()
```

### Current Draw
```kotlin
BatteryReadingsViewModel.currentSeries -> CurrentDrawWaveform()
```

### Charging Sessions
```kotlin
BatteryReadingsViewModel.last24hReadings -> 
  Process to extract sessions -> ChargingSessionsChart()
```

### Top Apps
```kotlin
AppUsageRepository.getAppUsageStats() ->
  Sort by estimatedBatteryDrain -> TopBatteryAppsCard()
```

## Color Scheme (Battery Guru Style)
- **Green** (#4CAF50): High battery (70-100%), Charging
- **Yellow/Amber** (#FFC107): Medium battery (40-69%)
- **Orange** (#FF9800): Low battery (20-39%)
- **Red** (#F44336): Critical battery (<20%), Discharging

## Animation Specs
- All charts: 800ms duration with `EaseOutCubic` easing
- Staggered bar reveal for bar charts
- Smooth line drawing for waveforms
- No infinite animations to save battery

## Error Handling
- Empty data: Show "No data yet" message
- Permission denied (app usage): Show "Grant usage access permission"
- Database errors: Graceful fallback to empty states
- Never show loading spinner indefinitely
