package com.voltai.core.ai

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.common.TensorProcessor
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.roundToInt

@Singleton
class BatteryAIPredictor @Inject constructor(
    private val context: Context
) {
    
    companion object {
        private const val TAG = "BatteryAIPredictor"
        private const val MODEL_FILE = "battery_predictor_model.tflite"
        private const val INPUT_SIZE = 10 // Number of input features
        private const val OUTPUT_SIZE = 3 // Predictions: health%, cycles_remaining, optimal_charge_limit
        
        // Input feature indices
        private const val FEATURE_CURRENT_CAPACITY = 0
        private const val FEATURE_DESIGN_CAPACITY = 1
        private const val FEATURE_CYCLE_COUNT = 2
        private const val FEATURE_AVG_TEMPERATURE = 3
        private const val FEATURE_AVG_VOLTAGE = 4
        private const val FEATURE_CHARGING_FREQUENCY = 5
        private const val FEATURE_FAST_CHARGE_PERCENTAGE = 6
        private const val FEATURE_DEEP_DISCHARGE_COUNT = 7
        private const val FEATURE_TIME_SINCE_MANUFACTURE = 8
        private const val FEATURE_USAGE_PATTERN = 9
    }
    
    private var interpreter: Interpreter? = null
    private val inputBuffer = ByteBuffer.allocateDirect(INPUT_SIZE * 4).apply {
        order(ByteOrder.nativeOrder())
    }
    private val outputBuffer = ByteBuffer.allocateDirect(OUTPUT_SIZE * 4).apply {
        order(ByteOrder.nativeOrder())
    }
    
    data class BatteryPrediction(
        val healthPercentage: Float,
        val remainingCycles: Int,
        val optimalChargeLimit: Int,
        val degradationRate: Float,
        val estimatedLifespanDays: Int,
        val chargingRecommendations: List<ChargingRecommendation>
    )
    
    data class ChargingRecommendation(
        val title: String,
        val description: String,
        val priority: Priority,
        val potentialBenefit: String
    )
    
    enum class Priority {
        HIGH, MEDIUM, LOW
    }
    
    data class BatteryFeatures(
        val currentCapacity: Float,
        val designCapacity: Float,
        val cycleCount: Int,
        val averageTemperature: Float,
        val averageVoltage: Float,
        val chargingFrequency: Float, // charges per day
        val fastChargePercentage: Float, // % of charges that were fast charges
        val deepDischargeCount: Int, // times battery went below 10%
        val timeSinceManufacture: Int, // days
        val usagePattern: Float // 0-1, where 0 is light usage, 1 is heavy usage
    )
    
    suspend fun initialize() = withContext(Dispatchers.IO) {
        try {
            val modelFile = loadModelFile()
            if (modelFile != null) {
                interpreter = Interpreter(modelFile)
                Log.d(TAG, "AI model loaded successfully")
            } else {
                // Use fallback rule-based prediction
                Log.w(TAG, "AI model not found, using rule-based prediction")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize AI model", e)
        }
    }
    
    private fun loadModelFile(): MappedByteBuffer? {
        return try {
            // Check if model exists in assets
            if (context.assets.list("")?.contains(MODEL_FILE) == true) {
                FileUtil.loadMappedFile(context, MODEL_FILE)
            } else {
                // Model not included, will use rule-based fallback
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading model file", e)
            null
        }
    }
    
    suspend fun predictBatteryHealth(features: BatteryFeatures): BatteryPrediction = withContext(Dispatchers.IO) {
        if (interpreter != null) {
            // Use TensorFlow Lite model
            predictWithModel(features)
        } else {
            // Fallback to rule-based prediction
            predictWithRules(features)
        }
    }
    
    private fun predictWithModel(features: BatteryFeatures): BatteryPrediction {
        // Prepare input
        inputBuffer.clear()
        inputBuffer.putFloat(normalizeCapacity(features.currentCapacity))
        inputBuffer.putFloat(normalizeCapacity(features.designCapacity))
        inputBuffer.putFloat(normalizeCycles(features.cycleCount.toFloat()))
        inputBuffer.putFloat(normalizeTemperature(features.averageTemperature))
        inputBuffer.putFloat(normalizeVoltage(features.averageVoltage))
        inputBuffer.putFloat(features.chargingFrequency / 10f) // normalize to 0-1
        inputBuffer.putFloat(features.fastChargePercentage / 100f)
        inputBuffer.putFloat(features.deepDischargeCount / 100f)
        inputBuffer.putFloat(features.timeSinceManufacture / 1095f) // normalize by 3 years
        inputBuffer.putFloat(features.usagePattern)
        inputBuffer.rewind()
        
        // Run inference
        outputBuffer.clear()
        interpreter?.run(inputBuffer, outputBuffer)
        outputBuffer.rewind()
        
        // Parse output
        val healthPercentage = outputBuffer.float * 100
        val remainingCycles = (outputBuffer.float * 1000).roundToInt()
        val optimalChargeLimit = (outputBuffer.float * 100).roundToInt()
        
        // Calculate additional metrics
        val degradationRate = calculateDegradationRate(features, healthPercentage)
        val estimatedLifespanDays = calculateLifespan(remainingCycles, features.chargingFrequency)
        val recommendations = generateRecommendations(features, healthPercentage, optimalChargeLimit)
        
        return BatteryPrediction(
            healthPercentage = healthPercentage,
            remainingCycles = remainingCycles,
            optimalChargeLimit = optimalChargeLimit,
            degradationRate = degradationRate,
            estimatedLifespanDays = estimatedLifespanDays,
            chargingRecommendations = recommendations
        )
    }
    
    private fun predictWithRules(features: BatteryFeatures): BatteryPrediction {
        // Rule-based fallback prediction
        val capacityRatio = features.currentCapacity / features.designCapacity
        val cycleImpact = features.cycleCount * 0.05f // 0.05% degradation per cycle
        val temperatureImpact = if (features.averageTemperature > 35) {
            (features.averageTemperature - 35) * 0.5f
        } else 0f
        val deepDischargeImpact = features.deepDischargeCount * 0.1f
        val fastChargeImpact = features.fastChargePercentage * 0.02f
        
        val healthPercentage = ((capacityRatio * 100) - cycleImpact - temperatureImpact - 
                                deepDischargeImpact - fastChargeImpact).coerceIn(0f, 100f)
        
        val remainingCycles = ((2000 - features.cycleCount) * (healthPercentage / 100)).roundToInt()
        
        val optimalChargeLimit = when {
            features.fastChargePercentage > 70 -> 80
            features.fastChargePercentage > 40 -> 85
            else -> 90
        }
        
        val degradationRate = cycleImpact / features.timeSinceManufacture.coerceAtLeast(1)
        val estimatedLifespanDays = calculateLifespan(remainingCycles, features.chargingFrequency)
        val recommendations = generateRecommendations(features, healthPercentage, optimalChargeLimit)
        
        return BatteryPrediction(
            healthPercentage = healthPercentage,
            remainingCycles = remainingCycles,
            optimalChargeLimit = optimalChargeLimit,
            degradationRate = degradationRate,
            estimatedLifespanDays = estimatedLifespanDays,
            chargingRecommendations = recommendations
        )
    }
    
    private fun generateRecommendations(
        features: BatteryFeatures, 
        healthPercentage: Float,
        optimalChargeLimit: Int
    ): List<ChargingRecommendation> {
        val recommendations = mutableListOf<ChargingRecommendation>()
        
        // Temperature recommendations
        if (features.averageTemperature > 35) {
            recommendations.add(
                ChargingRecommendation(
                    title = "Reduce Temperature Exposure",
                    description = "Your battery frequently operates above 35°C. Avoid charging in hot environments and remove phone case while charging.",
                    priority = Priority.HIGH,
                    potentialBenefit = "Can extend battery life by up to 20%"
                )
            )
        }
        
        // Fast charging recommendations
        if (features.fastChargePercentage > 60) {
            recommendations.add(
                ChargingRecommendation(
                    title = "Use Regular Charging More Often",
                    description = "You use fast charging ${features.fastChargePercentage.roundToInt()}% of the time. Use regular charging overnight when possible.",
                    priority = Priority.MEDIUM,
                    potentialBenefit = "Can reduce degradation rate by 15%"
                )
            )
        }
        
        // Charge limit recommendations
        if (optimalChargeLimit < 100) {
            recommendations.add(
                ChargingRecommendation(
                    title = "Set Charge Limit to $optimalChargeLimit%",
                    description = "Limiting maximum charge can significantly extend battery lifespan without major impact on daily usage.",
                    priority = Priority.HIGH,
                    potentialBenefit = "Can double battery lifespan"
                )
            )
        }
        
        // Deep discharge recommendations
        if (features.deepDischargeCount > 30) {
            recommendations.add(
                ChargingRecommendation(
                    title = "Avoid Deep Discharges",
                    description = "Your battery has been deeply discharged ${features.deepDischargeCount} times. Try to charge before reaching 20%.",
                    priority = Priority.MEDIUM,
                    potentialBenefit = "Can prevent capacity loss"
                )
            )
        }
        
        // Charging frequency recommendations
        if (features.chargingFrequency > 2) {
            recommendations.add(
                ChargingRecommendation(
                    title = "Reduce Charging Frequency",
                    description = "You charge ${features.chargingFrequency} times per day. Consider using battery saver modes to extend time between charges.",
                    priority = Priority.LOW,
                    potentialBenefit = "Can reduce wear from charge cycles"
                )
            )
        }
        
        return recommendations.sortedBy { it.priority.ordinal }
    }
    
    private fun calculateDegradationRate(features: BatteryFeatures, currentHealth: Float): Float {
        val initialHealth = 100f
        val healthLoss = initialHealth - currentHealth
        val days = features.timeSinceManufacture.coerceAtLeast(1)
        return healthLoss / days // % per day
    }
    
    private fun calculateLifespan(remainingCycles: Int, chargingFrequency: Float): Int {
        return if (chargingFrequency > 0) {
            (remainingCycles / chargingFrequency).roundToInt()
        } else {
            remainingCycles * 365 // Assume 1 charge per day if no data
        }
    }
    
    // Normalization functions for model input
    private fun normalizeCapacity(capacity: Float): Float = capacity / 5000f
    private fun normalizeCycles(cycles: Float): Float = cycles / 2000f
    private fun normalizeTemperature(temp: Float): Float = (temp - 20f) / 30f
    private fun normalizeVoltage(voltage: Float): Float = (voltage - 3.7f) / 0.5f
    
    fun generateSmartChargingSchedule(
        userSchedule: UserSchedule,
        currentBatteryLevel: Int,
        targetLevel: Int = 80
    ): ChargingSchedule {
        val schedule = ChargingSchedule()
        
        // Analyze user's daily pattern
        val wakeUpTime = userSchedule.typicalWakeTime
        val sleepTime = userSchedule.typicalSleepTime
        
        // Calculate optimal charging windows
        if (currentBatteryLevel < 30) {
            // Need immediate charging
            schedule.addWindow(
                ChargingWindow(
                    startTime = System.currentTimeMillis(),
                    endTime = System.currentTimeMillis() + 3600000, // 1 hour
                    targetPercentage = 60,
                    chargingSpeed = ChargingSpeed.NORMAL,
                    reason = "Emergency charge to safe level"
                )
            )
        }
        
        // Overnight charging schedule
        schedule.addWindow(
            ChargingWindow(
                startTime = sleepTime + 3600000, // 1 hour after sleep
                endTime = wakeUpTime - 1800000, // 30 min before wake
                targetPercentage = targetLevel,
                chargingSpeed = ChargingSpeed.SLOW,
                reason = "Optimized overnight charging"
            )
        )
        
        return schedule
    }
    
    data class UserSchedule(
        val typicalWakeTime: Long,
        val typicalSleepTime: Long,
        val workStartTime: Long,
        val workEndTime: Long,
        val chargingLocations: List<ChargingLocation>
    )
    
    data class ChargingLocation(
        val name: String,
        val latitude: Double,
        val longitude: Double,
        val availableFrom: Long,
        val availableTo: Long,
        val chargingSpeed: ChargingSpeed
    )
    
    data class ChargingSchedule(
        val windows: MutableList<ChargingWindow> = mutableListOf()
    ) {
        fun addWindow(window: ChargingWindow) {
            windows.add(window)
        }
    }
    
    data class ChargingWindow(
        val startTime: Long,
        val endTime: Long,
        val targetPercentage: Int,
        val chargingSpeed: ChargingSpeed,
        val reason: String
    )
    
    enum class ChargingSpeed {
        SLOW, // < 5W
        NORMAL, // 5-15W
        FAST, // 15-30W
        SUPER_FAST // > 30W
    }
    
    fun release() {
        interpreter?.close()
        interpreter = null
    }
}
