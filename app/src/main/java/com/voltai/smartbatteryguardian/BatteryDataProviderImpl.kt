package com.voltai.smartbatteryguardian

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import com.voltai.smartbatteryguardian.data.battery.BatteryDataManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Real implementation of BatteryDataProvider that delegates to BatteryDataManager (single source of truth)
 */
@Singleton
class BatteryDataProviderImpl
@Inject
constructor(
    @ApplicationContext private val context: Context,
    private val batteryDataManager: BatteryDataManager,
) : BatteryDataProvider {
    override val batteryData: StateFlow<BatteryData> = batteryDataManager.batteryData

    override fun refreshData() {
        // Handled automatically by BatteryDataManager
    }
}

/**
 * Composable function to get battery data from the singleton BatteryDataManager
 */
@Composable
fun rememberBatteryData(context: Context): State<BatteryData> {
    // Get the singleton instance from Hilt
    val viewModel: com.voltai.smartbatteryguardian.viewmodels.BatteryDataViewModel =
        androidx.hilt.navigation.compose.hiltViewModel()
    return viewModel.batteryData.collectAsState()
}
