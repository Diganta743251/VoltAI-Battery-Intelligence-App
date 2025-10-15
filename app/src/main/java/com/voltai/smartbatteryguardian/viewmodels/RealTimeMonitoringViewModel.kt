package com.voltai.smartbatteryguardian.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.voltai.smartbatteryguardian.database.BatteryAlertDao
import com.voltai.smartbatteryguardian.database.BatteryLogDao
import com.voltai.smartbatteryguardian.screens.AlertData
import com.voltai.smartbatteryguardian.screens.AlertSeverity
import com.voltai.smartbatteryguardian.screens.AlertType
import com.voltai.smartbatteryguardian.screens.GraphDataPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * ViewModel for Real-Time Monitoring Screen
 * Provides real historical data from Room database
 */
@HiltViewModel
class RealTimeMonitoringViewModel
@Inject
constructor(
    private val batteryLogDao: BatteryLogDao,
    private val batteryAlertDao: BatteryAlertDao,
) : ViewModel() {
    /**
     * Battery level history (last 50 data points)
     * Maps BatteryLogEntity to GraphDataPoint for charting
     */
    val batteryHistory: StateFlow<List<GraphDataPoint>> =
        batteryLogDao.getRecentLogs(50)
            .map { logs ->
                logs.map { log ->
                    GraphDataPoint(
                        timestamp = log.timestamp,
                        value = log.batteryLevel.toFloat(),
                    )
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList(),
            )

    /**
     * Temperature history (last 50 data points)
     */
    val temperatureHistory: StateFlow<List<GraphDataPoint>> =
        batteryLogDao.getRecentLogs(50)
            .map { logs ->
                logs.map { log ->
                    GraphDataPoint(
                        timestamp = log.timestamp,
                        value = log.temperature,
                    )
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList(),
            )

    /**
     * Voltage history (last 50 data points)
     */
    val voltageHistory: StateFlow<List<GraphDataPoint>> =
        batteryLogDao.getRecentLogs(50)
            .map { logs ->
                logs.map { log ->
                    GraphDataPoint(
                        timestamp = log.timestamp,
                        value = log.voltage.toFloat(),
                    )
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList(),
            )

    /**
     * Active alerts from database
     * Converts BatteryAlertEntity to screen's AlertData model
     */
    val currentAlerts: StateFlow<List<AlertData>> =
        batteryAlertDao.getUnreadAlerts()
            .map { alerts ->
                alerts.map { alert ->
                    AlertData(
                        id = alert.id.toString(),
                        type = parseAlertType(alert.alertType),
                        severity = parseAlertSeverity(alert.severity),
                        title = alert.alertType.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() },
                        message = alert.message,
                        timestamp = alert.timestamp,
                        isActive = !alert.isRead,
                        icon = getAlertIcon(parseAlertType(alert.alertType)),
                    )
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList(),
            )

    /**
     * Parse alert type from database string
     */
    private fun parseAlertType(typeString: String): AlertType {
        return when (typeString.uppercase()) {
            "TEMPERATURE", "OVERHEAT" -> AlertType.TEMPERATURE
            "VOLTAGE", "OVER_VOLTAGE" -> AlertType.VOLTAGE
            "CHARGING", "FAST_CHARGING" -> AlertType.CHARGING
            "HEALTH", "HEALTH_DECLINE" -> AlertType.HEALTH
            else -> AlertType.PERFORMANCE
        }
    }

    /**
     * Parse alert severity from database int (1-5 scale to enum)
     */
    private fun parseAlertSeverity(severity: Int): AlertSeverity {
        return when {
            severity >= 4 -> AlertSeverity.CRITICAL
            severity >= 2 -> AlertSeverity.WARNING
            else -> AlertSeverity.INFO
        }
    }

    /**
     * Get icon emoji for alert type
     */
    private fun getAlertIcon(type: AlertType): String {
        return when (type) {
            AlertType.TEMPERATURE -> "🌡️"
            AlertType.VOLTAGE -> "⚡"
            AlertType.CHARGING -> "🔌"
            AlertType.HEALTH -> "❤️"
            AlertType.PERFORMANCE -> "⚙️"
        }
    }

    /**
     * Mark an alert as read
     */
    suspend fun markAlertAsRead(alertId: Long) {
        batteryAlertDao.markAlertAsRead(alertId)
    }

    /**
     * Mark all alerts as read
     */
    suspend fun markAllAlertsAsRead() {
        batteryAlertDao.markAllAlertsAsRead()
    }
}
