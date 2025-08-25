package com.voltai.data.repository

import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.PackageManager
import com.voltai.domain.model.AppUsage
import com.voltai.domain.repository.AppUsageRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.Calendar
import javax.inject.Inject

class AppUsageRepositoryImpl @Inject constructor(
    private val context: Context,
    private val usageStatsManager: UsageStatsManager
) : AppUsageRepository {

    override fun getAppUsageStats(): Flow<List<AppUsage>> = flow {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -1) // Get stats for the last 24 hours
        val endTime = System.currentTimeMillis()
        val startTime = calendar.timeInMillis

        val usageStatsList = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            startTime,
            endTime
        )

        val appUsageList = mutableListOf<AppUsage>()
        val packageManager = context.packageManager

        for (usageStats in usageStatsList) {
            try {
                val appInfo = packageManager.getApplicationInfo(usageStats.packageName, 0)
                val appName = packageManager.getApplicationLabel(appInfo).toString()
                val totalTimeInForeground = usageStats.totalTimeInForeground

                // Simple approximation for battery drain (can be improved later)
                // This is a placeholder for more advanced battery drain estimation.
                // Real background drain analysis is complex and often requires root access or specific OEM APIs.
                val estimatedBatteryDrain = totalTimeInForeground / (60 * 60 * 1000).toDouble() * 5 // 5% battery drain per hour of foreground use

                appUsageList.add(
                    AppUsage(
                        packageName = usageStats.packageName,
                        appName = appName,
                        totalTimeInForeground = totalTimeInForeground,
                        estimatedBatteryDrain = estimatedBatteryDrain
                    )
                )
            } catch (e: PackageManager.NameNotFoundException) {
                // Ignore uninstalled apps
            }
        }
        emit(appUsageList.sortedByDescending { it.totalTimeInForeground })
    }
}
