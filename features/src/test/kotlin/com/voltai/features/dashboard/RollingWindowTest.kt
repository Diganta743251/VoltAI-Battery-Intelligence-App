package com.voltai.features.dashboard

import com.voltai.domain.DomainConstants
import com.voltai.domain.model.BatteryLog
import com.voltai.domain.repository.IBatteryMonitor
import com.voltai.domain.repository.ICurrentRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import org.junit.Rule
import com.voltai.testing.MainDispatcherRule
import org.junit.Assert.assertEquals
import org.junit.Test

private class FakeBatteryMonitor(private val flow: Flow<BatteryLog>) : IBatteryMonitor {
    override fun observeBatteryLogs(): Flow<BatteryLog> = flow
}

private class FakeCurrentRepository : ICurrentRepository {
    private val store = MutableStateFlow<List<BatteryLog>>(emptyList())
    override suspend fun save(log: BatteryLog) {
        store.value = store.value + log
    }
    override fun getRecentLogs(limit: Int) = store.map { it.takeLast(limit) }
}

@OptIn(ExperimentalCoroutinesApi::class)
class RollingWindowTest {
    @get:Rule
    val mainRule = MainDispatcherRule(StandardTestDispatcher())

    @Test
    fun rolling_window_retains_last_N() = runTest {
        val logs = (1..60).map { i ->
            BatteryLog(
                id = 0,
                timestamp = i.toLong(),
                batteryLevel = 50,
                voltage = 3.9f,
                temperature = 30f,
                chargingStatus = if (i % 2 == 0) "Charging" else "Discharging",
                chargingType = "NONE",
                current = i.toFloat()
            )
        }
        val fakeMonitor = FakeBatteryMonitor(emptyFlow())
        val fakeRepo = FakeCurrentRepository()
        val vm = DashboardViewModel(fakeMonitor, fakeRepo)

        // push into repo manually to simulate persistence
        logs.forEach { log -> fakeRepo.save(log) }
        advanceUntilIdle()

        val expected = logs.takeLast(DomainConstants.CURRENT_WINDOW_SIZE).map { it.current }
        assertEquals(expected, vm.currentData.value)
    }
}