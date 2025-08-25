package com.voltai.data.repository

import com.voltai.core.utils.BatteryMonitor
import com.voltai.data.local.dao.CurrentSampleDao
import com.voltai.data.local.entity.CurrentSample
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CurrentRepository @Inject constructor(
    private val batteryMonitor: BatteryMonitor,
    private val currentSampleDao: CurrentSampleDao
) {
    private val repoScope = CoroutineScope(Dispatchers.IO)

    // Persist incoming samples and expose recent window
    val currentSamples: Flow<List<Float>> = flow {
        batteryMonitor.getBatteryStatusFlow().collect { status ->
            val now = System.currentTimeMillis()
            repoScope.launch {
                currentSampleDao.insert(
                    CurrentSample(
                        timestamp = now,
                        currentMa = status.currentNow / 1000f, // uA -> mA
                        isCharging = status.isCharging
                    )
                )
                // Prune older than 7 days
                val sevenDaysAgo = now - 7L * 24 * 60 * 60 * 1000
                currentSampleDao.prune(sevenDaysAgo)
            }
            // Emit is handled by separate DB flow below
        }
    }

    fun getRecentSamples(windowMillis: Long): Flow<List<Float>> {
        val since = System.currentTimeMillis() - windowMillis
        return currentSampleDao.getSamplesSince(since).map { list -> list.map { it.currentMa } }
    }
}