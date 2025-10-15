package com.voltai.smartbatteryguardian

import kotlinx.coroutines.flow.StateFlow

/**
 * Interface for providing battery data to the application
 */
interface BatteryDataProvider {
    /**
     * StateFlow of current battery data
     */
    val batteryData: StateFlow<BatteryData>

    /**
     * Manually refresh battery data
     */
    fun refreshData()
}
