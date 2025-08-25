package com.voltai.domain.model

data class ChargingAlert(
    val alertType: AlertType,
    val threshold: Int,
    val isEnabled: Boolean,
    val notificationMessage: String
)

enum class AlertType {
    OVERHEATING,
    SLOW_CHARGE,
    CHARGE_LIMIT
}
