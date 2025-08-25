package com.voltai.backup

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.voltai.data.local.BatteryLogDao
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileWriter

@HiltWorker
class ExportLogsWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val batteryLogDao: BatteryLogDao
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val batteryLogs = batteryLogDao.getAllBatteryLogs().first()
            val csvFile = File(applicationContext.getExternalFilesDir(null), "battery_logs.csv")
            val writer = FileWriter(csvFile)

            // Write CSV header
            writer.append("Timestamp,BatteryPercentage,Voltage,Temperature,Status,ChargePlug\n")

            // Write battery log data
            batteryLogs.forEach {
                writer.append("${it.timestamp},${it.batteryPercentage},${it.voltage},${it.temperature},${it.status},${it.chargePlug}\n")
            }
            writer.flush()
            writer.close()
            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure()
        }
    }
}
