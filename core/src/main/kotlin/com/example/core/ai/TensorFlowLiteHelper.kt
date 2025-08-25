package com.voltai.core.ai

import android.content.Context
import android.util.Log
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TensorFlowLiteHelper @Inject constructor(private val context: Context) {

    private var interpreter: Interpreter? = null
    private val TAG = "TensorFlowLiteHelper"
    
    fun loadModel(modelPath: String): Boolean {
        return try {
            // Check if model file exists
            val assetList = context.assets.list("") ?: emptyArray()
            if (!assetList.contains(modelPath)) {
                Log.w(TAG, "Model file not found: $modelPath")
                return false
            }
            
            val assetFileDescriptor = context.assets.openFd(modelPath)
            val inputStream = FileInputStream(assetFileDescriptor.fileDescriptor)
            val fileChannel = inputStream.channel
            val startOffset = assetFileDescriptor.startOffset
            val declaredLength = assetFileDescriptor.declaredLength
            val modelBuffer: MappedByteBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
            
            val options = Interpreter.Options().apply {
                setNumThreads(2) // Reduced threads for better stability
                setUseNNAPI(false) // Disable NNAPI for compatibility
            }
            
            interpreter = Interpreter(modelBuffer, options)
            Log.d(TAG, "Model loaded successfully: $modelPath")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load model: $modelPath", e)
            false
        }
    }

    fun runBatteryPrediction(batteryData: FloatArray): FloatArray? {
        return try {
            if (interpreter == null) {
                Log.w(TAG, "Interpreter not initialized")
                return null
            }
            
            // Input shape: [1, 6] - battery%, voltage, temp, current, time_since_last_charge, charging_status
            val input = Array(1) { batteryData }
            // Output shape: [1, 3] - predicted_drain_rate, health_score, time_to_empty
            val output = Array(1) { FloatArray(3) }
            
            interpreter?.run(input, output)
            Log.d(TAG, "Inference completed successfully")
            output[0]
        } catch (e: Exception) {
            Log.e(TAG, "Inference failed", e)
            null
        }
    }

    fun runHealthAnalysis(historicalData: Array<FloatArray>): FloatArray? {
        return try {
            if (interpreter == null) {
                Log.w(TAG, "Interpreter not initialized")
                return null
            }
            
            // Input shape: [1, 10, 6] - last 10 battery readings with 6 features each
            val input = Array(1) { historicalData }
            // Output shape: [1, 4] - health_score, degradation_rate, cycle_count_estimate, lifespan_months
            val output = Array(1) { FloatArray(4) }
            
            interpreter?.run(input, output)
            Log.d(TAG, "Health analysis completed successfully")
            output[0]
        } catch (e: Exception) {
            Log.e(TAG, "Health analysis failed", e)
            null
        }
    }

    fun isModelLoaded(): Boolean = interpreter != null

    fun close() {
        try {
            interpreter?.close()
            interpreter = null
            Log.d(TAG, "TensorFlow Lite interpreter closed")
        } catch (e: Exception) {
            Log.e(TAG, "Error closing interpreter", e)
        }
    }
}
