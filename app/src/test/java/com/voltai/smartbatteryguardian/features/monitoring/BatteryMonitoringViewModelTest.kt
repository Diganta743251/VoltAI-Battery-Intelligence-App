package com.voltai.smartbatteryguardian.features.monitoring

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.voltai.smartbatteryguardian.core.Result
import com.voltai.smartbatteryguardian.data.local.entities.BatteryReadingEntity
import com.voltai.smartbatteryguardian.data.repository.BatteryRepository
import com.voltai.smartbatteryguardian.presentation.viewmodels.BatteryMonitoringViewModel
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class BatteryMonitoringViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var batteryRepository: BatteryRepository
    private lateinit var viewModel: BatteryMonitoringViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        batteryRepository = mockk()
        viewModel = BatteryMonitoringViewModel(batteryRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        clearAllMocks()
    }

    @Test
    fun `initial state should be loading`() = runTest {
        viewModel.batteryState.test {
            val initialState = awaitItem()
            assertThat(initialState.isLoading).isTrue()
            assertThat(initialState.batteryLevel).isEqualTo(0)
            assertThat(initialState.error).isNull()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `loadBatteryData should update state with battery information`() = runTest {
        val mockBatteryInfo = BatteryInfo(
            level = 85,
            status = "Charging",
            health = "Good",
            temperature = 35.0f,
            voltage = 4.2f,
            isCharging = true,
            chargingMethod = "USB",
            timeRemaining = 3600000L,
            cycleCount = 150
        )

        every { batteryRepository.getCurrentBatteryInfo() } returns flowOf(Result.Success(mockBatteryInfo))

        viewModel.loadBatteryData()

        viewModel.batteryState.test {
            val state = awaitItem()
            assertThat(state.isLoading).isFalse()
            assertThat(state.batteryLevel).isEqualTo(85)
            assertThat(state.status).isEqualTo("Charging")
            assertThat(state.health).isEqualTo("Good")
            assertThat(state.temperature).isEqualTo(35.0f)
            assertThat(state.isCharging).isTrue()
            assertThat(state.error).isNull()
        }
    }

    @Test
    fun `loadBatteryData should handle error state`() = runTest {
        val exception = RuntimeException("Failed to load battery data")
        every { batteryRepository.getCurrentBatteryInfo() } returns flowOf(Result.Error(exception))

        viewModel.loadBatteryData()

        viewModel.batteryState.test {
            val state = awaitItem()
            assertThat(state.isLoading).isFalse()
            assertThat(state.error).isEqualTo("Failed to load battery data")
        }
    }

    @Test
    fun `loadBatteryHistory should update history state`() = runTest {
        val mockHistory = listOf(
            BatteryReadingEntity(
                id = 1,
                timestamp = System.currentTimeMillis() - 3600000,
                batteryLevel = 90,
                temperature = 32.5f,
                voltage = 4.1f,
                status = "Discharging",
                health = "Good",
                isCharging = false,
                chargingMethod = null
            ),
            BatteryReadingEntity(
                id = 2,
                timestamp = System.currentTimeMillis(),
                batteryLevel = 85,
                temperature = 35.0f,
                voltage = 4.2f,
                status = "Charging",
                health = "Good",
                isCharging = true,
                chargingMethod = "USB"
            )
        )

        every { batteryRepository.getBatteryHistory(any(), any()) } returns flowOf(Result.Success(mockHistory))

        viewModel.loadBatteryHistory(24) // 24 hours

        viewModel.historyState.test {
            val state = awaitItem()
            assertThat(state.isLoading).isFalse()
            assertThat(state.history).hasSize(2)
            assertThat(state.history?.first()?.batteryLevel).isEqualTo(90)
            assertThat(state.error).isNull()
        }
    }

    @Test
    fun `loadBatteryHistory should handle empty history`() = runTest {
        every { batteryRepository.getBatteryHistory(any(), any()) } returns flowOf(Result.Empty)

        viewModel.loadBatteryHistory(24)

        viewModel.historyState.test {
            val state = awaitItem()
            assertThat(state.isLoading).isFalse()
            assertThat(state.history).isEmpty()
            assertThat(state.error).isNull()
        }
    }

    @Test
    fun `loadBatteryHealth should update health state`() = runTest {
        val mockHealthData = BatteryHealthData(
            currentCapacity = 3500,
            designCapacity = 4000,
            cycleCount = 150,
            healthPercentage = 87.5f,
            degradationRate = 0.5f
        )

        every { batteryRepository.calculateBatteryHealth() } returns flowOf(Result.Success(mockHealthData))

        viewModel.loadBatteryHealth()

        viewModel.healthState.test {
            val state = awaitItem()
            assertThat(state.isLoading).isFalse()
            assertThat(state.healthPercentage).isEqualTo(87.5f)
            assertThat(state.cycleCount).isEqualTo(150)
            assertThat(state.currentCapacity).isEqualTo(3500)
            assertThat(state.error).isNull()
        }
    }

    @Test
    fun `predictChargingTime should update prediction state`() = runTest {
        val mockPrediction = ChargingPrediction(
            timeToFull = 7200000L,
            timeToTargetLevel = 3600000L,
            targetLevel = 90,
            chargingRate = 15.0f,
            estimatedCompletion = System.currentTimeMillis() + 7200000L
        )

        every { batteryRepository.predictChargingTime(any()) } returns flowOf(Result.Success(mockPrediction))

        viewModel.predictChargingTime(90)

        viewModel.chargingPredictionState.test {
            val state = awaitItem()
            assertThat(state.isLoading).isFalse()
            assertThat(state.timeToFull).isEqualTo(7200000L)
            assertThat(state.timeToTargetLevel).isEqualTo(3600000L)
            assertThat(state.targetLevel).isEqualTo(90)
            assertThat(state.error).isNull()
        }
    }

    @Test
    fun `loadOptimizationSuggestions should update suggestions state`() = runTest {
        val mockSuggestions = listOf(
            OptimizationSuggestion(
                id = "reduce_brightness",
                title = "Reduce Screen Brightness",
                description = "Lowering screen brightness can extend battery life",
                category = "Display",
                impact = "Medium",
                estimatedSavings = 15
            ),
            OptimizationSuggestion(
                id = "disable_location",
                title = "Disable Location Services",
                description = "Turn off GPS when not needed",
                category = "Location",
                impact = "High",
                estimatedSavings = 25
            )
        )

        every { batteryRepository.getOptimizationSuggestions() } returns flowOf(Result.Success(mockSuggestions))

        viewModel.loadOptimizationSuggestions()

        viewModel.optimizationState.test {
            val state = awaitItem()
            assertThat(state.isLoading).isFalse()
            assertThat(state.suggestions).hasSize(2)
            assertThat(state.suggestions?.first()?.estimatedSavings).isEqualTo(15)
            assertThat(state.error).isNull()
        }
    }

    @Test
    fun `startBatteryMonitoring should begin continuous monitoring`() = runTest {
        val mockBatteryReading = BatteryReadingEntity(
            timestamp = System.currentTimeMillis(),
            batteryLevel = 85,
            temperature = 35.0f,
            voltage = 4.2f,
            status = "Charging",
            health = "Good",
            isCharging = true,
            chargingMethod = "USB"
        )

        every { batteryRepository.monitorBatteryChanges() } returns flowOf(Result.Success(mockBatteryReading))

        viewModel.startBatteryMonitoring()

        viewModel.batteryState.test {
            val state = awaitItem()
            assertThat(state.batteryLevel).isEqualTo(85)
            assertThat(state.isCharging).isTrue()
            assertThat(state.status).isEqualTo("Charging")
        }
    }

    @Test
    fun `stopBatteryMonitoring should stop monitoring`() = runTest {
        every { batteryRepository.monitorBatteryChanges() } returns flowOf(Result.Success(mockk()))

        viewModel.startBatteryMonitoring()
        viewModel.stopBatteryMonitoring()

        // Verify monitoring is stopped
        viewModel.isMonitoring.test {
            assertThat(awaitItem()).isFalse()
        }
    }

    @Test
    fun `refreshData should reload all battery information`() = runTest {
        val mockBatteryInfo = BatteryInfo(
            level = 75,
            status = "Discharging",
            health = "Good",
            temperature = 32.0f,
            voltage = 4.0f,
            isCharging = false,
            chargingMethod = null,
            timeRemaining = 18000000L,
            cycleCount = 160
        )

        val mockHistory = listOf(
            BatteryReadingEntity(
                timestamp = System.currentTimeMillis(),
                batteryLevel = 75,
                temperature = 32.0f,
                voltage = 4.0f,
                status = "Discharging",
                health = "Good",
                isCharging = false,
                chargingMethod = null
            )
        )

        every { batteryRepository.getCurrentBatteryInfo() } returns flowOf(Result.Success(mockBatteryInfo))
        every { batteryRepository.getBatteryHistory(any(), any()) } returns flowOf(Result.Success(mockHistory))

        viewModel.refreshData()

        // Verify both battery state and history are updated
        viewModel.batteryState.test {
            val state = awaitItem()
            assertThat(state.batteryLevel).isEqualTo(75)
            assertThat(state.status).isEqualTo("Discharging")
        }

        viewModel.historyState.test {
            val state = awaitItem()
            assertThat(state.history).hasSize(1)
            assertThat(state.history?.first()?.batteryLevel).isEqualTo(75)
        }
    }

    @Test
    fun `clearError should clear error state`() = runTest {
        // First set an error
        val exception = RuntimeException("Test error")
        every { batteryRepository.getCurrentBatteryInfo() } returns flowOf(Result.Error(exception))

        viewModel.loadBatteryData()

        // Then clear the error
        viewModel.clearError()

        viewModel.batteryState.test {
            val state = awaitItem()
            assertThat(state.error).isNull()
        }
    }

    @Test
    fun `setBatteryAlerts should update alert settings`() = runTest {
        val alertSettings = BatteryAlertSettings(
            lowBatteryThreshold = 20,
            highTemperatureThreshold = 40.0f,
            overchargeProtection = true,
            chargingCompleteNotification = true
        )

        viewModel.setBatteryAlerts(alertSettings)

        viewModel.alertSettings.test {
            val settings = awaitItem()
            assertThat(settings.lowBatteryThreshold).isEqualTo(20)
            assertThat(settings.highTemperatureThreshold).isEqualTo(40.0f)
            assertThat(settings.overchargeProtection).isTrue()
        }
    }

    @Test
    fun `updateMonitoringInterval should change monitoring frequency`() = runTest {
        val newInterval = 30000L // 30 seconds

        viewModel.updateMonitoringInterval(newInterval)

        viewModel.monitoringInterval.test {
            assertThat(awaitItem()).isEqualTo(newInterval)
        }
    }

    @Test
    fun `exportBatteryData should handle export operation`() = runTest {
        val mockHistory = listOf(
            BatteryReadingEntity(
                timestamp = System.currentTimeMillis(),
                batteryLevel = 85,
                temperature = 35.0f,
                voltage = 4.2f,
                status = "Charging",
                health = "Good",
                isCharging = true,
                chargingMethod = "USB"
            )
        )

        every { batteryRepository.exportBatteryData(any(), any()) } returns flowOf(Result.Success("export_path"))

        viewModel.exportBatteryData(System.currentTimeMillis() - 86400000, System.currentTimeMillis())

        viewModel.exportState.test {
            val state = awaitItem()
            assertThat(state.isLoading).isFalse()
            assertThat(state.exportPath).isEqualTo("export_path")
            assertThat(state.error).isNull()
        }
    }

    @Test
    fun `multiple concurrent operations should be handled correctly`() = runTest {
        val mockBatteryInfo = BatteryInfo(
            level = 80,
            status = "Charging",
            health = "Good",
            temperature = 34.0f,
            voltage = 4.15f,
            isCharging = true,
            chargingMethod = "AC",
            timeRemaining = 4500000L,
            cycleCount = 145
        )

        val mockHistory = listOf(
            BatteryReadingEntity(
                timestamp = System.currentTimeMillis(),
                batteryLevel = 80,
                temperature = 34.0f,
                voltage = 4.15f,
                status = "Charging",
                health = "Good",
                isCharging = true,
                chargingMethod = "AC"
            )
        )

        every { batteryRepository.getCurrentBatteryInfo() } returns flowOf(Result.Success(mockBatteryInfo))
        every { batteryRepository.getBatteryHistory(any(), any()) } returns flowOf(Result.Success(mockHistory))

        // Launch multiple operations concurrently
        viewModel.loadBatteryData()
        viewModel.loadBatteryHistory(12)

        // Both operations should complete successfully
        viewModel.batteryState.test {
            val state = awaitItem()
            assertThat(state.batteryLevel).isEqualTo(80)
            assertThat(state.error).isNull()
        }

        viewModel.historyState.test {
            val state = awaitItem()
            assertThat(state.history).hasSize(1)
            assertThat(state.error).isNull()
        }
    }

    @Test
    fun `state persistence should work across configuration changes`() = runTest {
        val mockBatteryInfo = BatteryInfo(
            level = 70,
            status = "Discharging",
            health = "Good",
            temperature = 30.0f,
            voltage = 3.9f,
            isCharging = false,
            chargingMethod = null,
            timeRemaining = 21600000L,
            cycleCount = 155
        )

        every { batteryRepository.getCurrentBatteryInfo() } returns flowOf(Result.Success(mockBatteryInfo))

        viewModel.loadBatteryData()

        // Simulate configuration change by creating new ViewModel instance
        val newViewModel = BatteryMonitoringViewModel(batteryRepository)

        // State should be preserved or reloaded
        newViewModel.batteryState.test {
            val state = awaitItem()
            // Initially loading state for new instance
            assertThat(state.isLoading).isTrue()
        }
    }
}

// Mock data classes for testing
data class BatteryAlertSettings(
    val lowBatteryThreshold: Int,
    val highTemperatureThreshold: Float,
    val overchargeProtection: Boolean,
    val chargingCompleteNotification: Boolean
)
