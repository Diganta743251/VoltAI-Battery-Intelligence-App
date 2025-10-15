# Home Screen Card Redesign Plan

## Design Principles
1. **Compact spacing** - No Arrangement.SpaceBetween (causes whitespace)
2. **Chevron indicators** - Every card shows ChevronRight icon for clickability  
3. **Visual hierarchy** - Header → Main content → Secondary info
4. **Icon/Emoji prominence** - Large, visible icons for quick recognition
5. **180dp height utilization** - Use space efficiently with proper spacing

## Cards to Fix:

### ✅ Already Fixed:
- BatteryLevelCard
- ChargingStatusCard  
- TemperatureCard
- HistoryCard

### 🔧 Need Fixing:
1. HealthCard - Remove SpaceBetween, add chevron
2. CurrentVoltageCard - Remove SpaceBetween, add chevron  
3. ScreenTimeCard - Remove SpaceBetween, add chevron
4. AppUsageCard - Remove SpaceBetween, add chevron
5. AIInsightsCard - Remove SpaceBetween, add chevron
6. PowerModesCard - Remove SpaceBetween, add chevron
7. QuickActionsCard - Remove SpaceBetween, add chevron
8. OverlaysCard - Remove SpaceBetween, add chevron
9. PremiumCard - Add chevron, better layout
10. DefaultCard - Better empty state

## Standard Card Pattern:
```kotlin
Column(
    modifier = Modifier.fillMaxSize(),
    verticalArrangement = Arrangement.spacedBy(8-10.dp)  // NOT SpaceBetween!
) {
    // Header with chevron
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        CardHeader("Card Title")
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = "View details",
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.size(20.dp)
        )
    }
    
    // Main content with icon/data
    Row(...) {
        Icon/Emoji (large, 32-40dp)
        Column { Main data }
    }
    
    // Secondary info/metrics
    Row { MetricPills or additional data }
    
    // Bottom hint/status (optional)
    Text(small, hint)
}
```
