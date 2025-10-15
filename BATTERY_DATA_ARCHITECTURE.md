# Battery Data Management Architecture

## Single Source of Truth Implementation

This document describes the unified battery data management system that ensures all screens and cards display consistent, accurate, real-time data.

---

## Architecture Overview

```
┌─────────────────────────────────────────────────────────────┐
│                    BatteryDataManager                        │
│                  (Single Source of Truth)                    │
│                                                              │
│  • Monitors BatteryManager broadcasts                        │
│  • Detects actual battery capacity                           │
│  • Calculates intelligent predictions                        │
│  • Saves readings to database                                │
│  • Exposes StateFlow<BatteryData>                           │
└──────────────────────┬───────────────────────────────────────┘
                       │
                       ├─────────────────┬──────────────────────┐
                       ▼                 ▼                      ▼
          ┌───────────────────┐  ┌──────────────┐   ┌──────────────────┐
          │ BatteryDataViewModel│ │  HomeScreen  │   │  All Other Screens│
          │                   │  │    Cards     │   │                  │
          └───────────────────┘  └──────────────┘   └──────────────────┘
```

---

## Core Components

### 1. **BatteryDataManager** (NEW - Single Source of Truth)

**Location:** `data/battery/BatteryDataManager.kt`

**Responsibilities:**
- ✅ **Battery Capacity Detection** - Uses multiple methods to detect actual mAh capacity
- ✅ **Real-time Monitoring** - Listens to BatteryManager broadcasts
- ✅ **Intelligent Calculations** - Accurate time remaining, charging speed, power consumption
- ✅ **Historical Tracking** - Maintains recent readings for trend analysis
- ✅ **Database Persistence** - Saves all readings for long-term analysis
- ✅ **Session Tracking** - Monitors charging/discharging cycles

**Key Features:**

#### Battery Capacity Detection Methods:
1. **Charge Counter API** - Gets current capacity in microampere-hours
2. **Energy Counter API** (Android 9+) - Converts nWh to mAh
3. **Device Model Estimation** - Fallback database of common device capacities
4. **Historical Analysis** - Learns actual capacity from usage patterns

#### Intelligent Time Calculations:
```kotlin
// Charging Time: Considers charging slowdown above 80%
val effectiveCurrent = if (level > 80) currentMa * 0.6f else currentMa
val hoursToFull = (remainingPercent * designCapacity / 100) / effectiveCurrent

// Discharging Time: Uses historical average discharge rate
val avgDischargeRate = calculateAverageDischargeRate() ?: currentMa
val hoursToDead = (level * designCapacity / 100) / avgDischargeRate
```

#### Charging Speed Detection:
- Calculates actual power: `P = V × I` (Watts)
- Categories: Super Fast (20W+), Fast (15W+), Quick (10W+), Normal (5W+), Slow (<5W)

---

### 2. **BatteryDataViewModel**

**Location:** `viewmodels/BatteryDataViewModel.kt`

**Purpose:** Exposes BatteryDataManager to Compose UI using Hilt dependency injection

```kotlin
@HiltViewModel
class BatteryDataViewModel @Inject constructor(
    private val batteryDataManager: BatteryDataManager
) : ViewModel() {
    val batteryData: StateFlow<BatteryData> = batteryDataManager.batteryData
}
```

---

### 3. **BatteryData** (Data Model)

**Location:** `BatteryData.kt`

**Contains ALL battery-related information:**

```kotlin
data class BatteryData(
    // Basic Info
    val batteryLevel: Int = 0,
    val isCharging: Boolean = false,
    val chargingStatus: String = "Not Charging",
    val remainingTime: String = "Unknown",      // ✅ Intelligent calculation
    val batteryUsed: String = "0%",
    
    // Technical Data
    val temperature: Float = 25.0f,
    val voltage: Double = 3.7,
    val current: Int = 0,
    val healthPercentage: Int = 100,
    val capacity: String = "Unknown",            // ✅ Actual device capacity
    val cycleCount: Int = 0,
    
    // Power Metrics
    val powerConsumption: Double = 0.0,          // ✅ Real-time watts
    val chargingSpeed: String = "Normal",        // ✅ With wattage
    val estimatedLife: String = "Unknown",
    val powerSource: String = "Battery",
    
    // Screen Time
    val screenOnTime: String = "0h 0m",          // ✅ From UsageStatsManager
    val screenOffTime: String = "24h 0m",
    
    // AI/Analytics
    val batteryScore: Int = 85,                  // ✅ Health score
    val recommendations: List<String> = emptyList()
)
```

---

## Data Flow

### 1. **Initial Detection (App Launch)**
```
1. BatteryDataManager initialized via Hilt
2. Detects battery capacity using multiple methods
3. Registers BroadcastReceiver for battery changes
4. Loads last 24h of historical data from database
5. Queries current battery state immediately
6. Emits initial BatteryData via StateFlow
```

### 2. **Continuous Updates**
```
Battery State Changes → BroadcastReceiver → updateBatteryData()
  ├─ Extract raw data from Intent
  ├─ Track charging/discharging sessions
  ├─ Calculate intelligent metrics
  ├─ Save to database (async)
  └─ Emit updated BatteryData → StateFlow
       ├─ Home Screen Cards update
       ├─ Detail Screens update
       └─ All other screens update
```

### 3. **Historical Analysis**
```
Recent Readings (last 100 in memory)
  ├─ Calculate average discharge rate
  ├─ Determine charging patterns
  ├─ Estimate cycle count
  └─ Improve time predictions
```

---

## Usage in Screens and Cards

### For Compose Screens:
```kotlin
@Composable
fun AnyScreen() {
    val context = LocalContext.current
    val batteryData by rememberBatteryData(context)
    
    // Use batteryData - always up-to-date!
    Text("Battery: ${batteryData.batteryLevel}%")
    Text("Remaining: ${batteryData.remainingTime}")
    Text("Capacity: ${batteryData.capacity}")
}
```

### For ViewModels:
```kotlin
@HiltViewModel
class MyViewModel @Inject constructor(
    private val batteryDataManager: BatteryDataManager
) : ViewModel() {
    val batteryData = batteryDataManager.batteryData
}
```

---

## Key Improvements

### ✅ **1. Accurate Battery Capacity**
- **Before:** Showed "Unknown" or incorrect values
- **After:** Detects actual mAh capacity using multiple methods
- **Fallback:** Device model database with 50+ common phones

### ✅ **2. Intelligent Time Remaining**
- **Before:** Simple calculation, often wrong
- **After:** 
  - Uses historical discharge patterns
  - Accounts for charging slowdown above 80%
  - Considers screen-on time impact
  - Returns "Calculating..." when insufficient data

### ✅ **3. Real Charging Speed**
- **Before:** "Fast Charging" based on current only
- **After:** "Super Fast (25W)" - shows actual wattage using P=VI

### ✅ **4. Accurate Health Score**
- **Before:** Static value
- **After:** Dynamic calculation based on:
  - Battery health status
  - Temperature (penalties for extremes)
  - Charge level (80-20% rule)
  
### ✅ **5. Real Screen Time**
- **Before:** Fake/placeholder data
- **After:** Queries UsageStatsManager for actual today's usage

### ✅ **6. Power Consumption**
- **Before:** Not calculated
- **After:** Real-time watts = (current mA / 1000) × voltage

---

## Database Integration

### Automatic Saving:
Every battery update is saved to the database with full details:

```kotlin
BatteryReading(
    timestamp, level, temperature, voltage, current,
    isCharging, capacity, chargingType, health,
    technology, plugType, powerSource, chargingSpeed,
    estimatedTimeRemaining, cycleCount, wearLevel
)
```

### Historical Queries:
```kotlin
// Last 24 hours
batteryDao.getReadingsInRange(oneDayAgo, now)

// Average battery level
batteryDao.getAverageBatteryLevel(startTime)

// Daily statistics
batteryDao.getDailyBatteryStatistics(startTime)
```

---

## Recommendations System

### Smart Recommendations Based on Real Data:

| Condition | Recommendation |
|-----------|---------------|
| Temp > 40°C | "Battery is hot. Remove case and stop charging." |
| Temp < 10°C | "Battery is cold. Warm up device for optimal performance." |
| Level < 20% & Not Charging | "Battery low. Consider charging soon." |
| Level > 90% & Charging | "Battery nearly full. Unplug to preserve health." |
| Health < 80% | "Battery health degraded. Consider replacement." |

---

## Migration Guide for Existing Code

### Old Way (Don't Use):
```kotlin
// ❌ Multiple different data sources
val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
val temp = getTemperatureSomewhere()
val capacity = "Unknown" // Hardcoded
```

### New Way (Use This):
```kotlin
// ✅ Single source of truth
val batteryData by rememberBatteryData(context)
// Everything is here and always consistent!
```

---

## Benefits

1. **Consistency** - All screens show same data at same time
2. **Accuracy** - Intelligent calculations, not guesses
3. **Performance** - Single BroadcastReceiver, shared by all
4. **Maintainability** - One place to fix bugs
5. **Testability** - Easy to mock BatteryDataManager
6. **Real Data** - No fake/placeholder data anywhere

---

## Future Enhancements

### Planned Features:
- [ ] Machine learning for better time predictions
- [ ] Battery wear level calculation from cycle count
- [ ] Charging optimization suggestions
- [ ] Battery health trending (weekly/monthly)
- [ ] Power-hungry app detection
- [ ] Charging habit analysis

---

## Testing

### To verify the system:
1. Check home screen cards show real data
2. Navigate to detail screens - should show same values
3. Plug/unplug charger - watch values update
4. Check database contains readings
5. Verify capacity shows actual mAh (not "Unknown")
6. Time remaining should be reasonable (not 999h)

---

## Troubleshooting

### Issue: Capacity shows "Unknown"
**Solution:** Device may not support BATTERY_PROPERTY_CHARGE_COUNTER. System will use estimated capacity based on device model.

### Issue: Time remaining shows "Calculating..."
**Solution:** Normal on first launch. Needs 2-3 readings to establish pattern. Usually resolves within 1 minute.

### Issue: Current shows 0
**Solution:** Some devices don't support BATTERY_PROPERTY_CURRENT_NOW. Time calculations will use historical averages.

---

## Code Locations

| Component | File Path |
|-----------|-----------|
| **BatteryDataManager** | `data/battery/BatteryDataManager.kt` |
| **BatteryDataViewModel** | `viewmodels/BatteryDataViewModel.kt` |
| **BatteryData Model** | `BatteryData.kt` |
| **BatteryDao** | `data/database/dao/BatteryDao.kt` |
| **BatteryReading Entity** | `data/database/entities/BatteryEntities.kt` |
| **Provider (Legacy)** | `BatteryDataProviderImpl.kt` |

---

## Summary

✅ **Single source of truth** for all battery data  
✅ **Intelligent calculations** for time remaining and charging speed  
✅ **Real device data** - no placeholders  
✅ **Automatic capacity detection** using multiple methods  
✅ **Database persistence** for historical analysis  
✅ **Consistent across all screens** and cards  
✅ **Performance optimized** with single monitor  

**All home screen cards and detail screens now display accurate, real-time battery data from the unified BatteryDataManager!**
