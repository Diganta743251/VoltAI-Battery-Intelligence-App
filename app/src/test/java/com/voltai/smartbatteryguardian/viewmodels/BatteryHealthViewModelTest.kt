package com.voltai.smartbatteryguardian.viewmodels

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.voltai.smartbatteryguardian.data.database.dao.BatteryDao
import com.voltai.smartbatteryguardian.data.database.dao.DailyHealthTrend
import com.voltai.smartbatteryguardian.data.database.entities.BatteryHealthRecord
import com.voltai.smartbatteryguardian.data.database.entities.ChargingSession
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test

class BatteryHealthViewModelTest {
    @Test
    fun chargedSummaryLast7d_sumsPositiveChargeDeltas() =
        runTest {
            val now = System.currentTimeMillis()
            val sessions =
                listOf(
                    // +30%
                    ChargingSession(
                        id = 1,
                        startTime = now - 2 * 24 * 60 * 60 * 1000L,
                        endTime = now - 2 * 24 * 60 * 60 * 1000L + 60 * 60 * 1000L,
                        startLevel = 20,
                        endLevel = 50,
                        chargerType = "USB-C",
                        maxPower = 18.0,
                        avgPower = 10.0,
                        maxTemperature = 38.0,
                        avgTemperature = 34.0,
                        efficiency = 0.9,
                        energyConsumed = 500.0,
                        location = null,
                        duration = 60 * 60 * 1000L,
                        interruptionCount = 0,
                        peakChargingSpeed = 25.0,
                        thermalThrottling = false,
                    ),
                    // +40%
                    ChargingSession(
                        id = 2,
                        startTime = now - 1 * 24 * 60 * 60 * 1000L,
                        endTime = now - 1 * 24 * 60 * 60 * 1000L + 90 * 60 * 1000L,
                        startLevel = 10,
                        endLevel = 50,
                        chargerType = "USB-C",
                        maxPower = 18.0,
                        avgPower = 10.0,
                        maxTemperature = 40.0,
                        avgTemperature = 35.0,
                        efficiency = 0.88,
                        energyConsumed = 700.0,
                        location = null,
                        duration = 90 * 60 * 1000L,
                        interruptionCount = 0,
                        peakChargingSpeed = 27.0,
                        thermalThrottling = false,
                    ),
                )

            val dao = mockk<BatteryDao>()
            // Trends and latest record (not used directly in this test)
            coEvery { dao.getDailyHealthTrends(any()) } returns emptyList<DailyHealthTrend>()
            coEvery { dao.getLatestHealthRecord() } returns
                BatteryHealthRecord(
                    id = 1,
                    timestamp = now,
                    healthPercentage = 90.0,
                    capacityRetained = 85.0,
                    cycleCount = 400,
                    degradationRate = 0.2,
                    impedance = null,
                    voltageStability = null,
                    temperatureStress = null,
                    chargingStress = null,
                    ageInDays = 500,
                    predictedLifespan = null,
                    recommendationScore = 0.0,
                    calibrationNeeded = false,
                    criticalIssues = null,
                )
            every { dao.getRecentChargingSessions(any()) } returns flowOf(sessions)
            every { dao.getChargingSessionsSince(any()) } returns flowOf(sessions)

            val vm = BatteryHealthViewModel(dao)

            vm.chargedSummaryLast7d.test {
                awaitItem() // initial
                val next = awaitItem() // computed
                assertThat(next.totalPercent).isEqualTo(30 + 40)
                cancelAndIgnoreRemainingEvents()
            }
        }
}
