package com.voltai.core.ai

import android.util.Log
import com.voltai.data.local.BatteryLogEntity
import com.voltai.data.preferences.DataStoreManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AIBatteryAnalyzer @Inject constructor(
    private val dataStoreManager: DataStoreManager,
    private val ruleBasedCoach: RuleBasedCoach,
    private val tensorFlowLiteHelper: TensorFlowLiteHelper
) {

    suspend fun analyzeBatteryData(batteryLogs: List<BatteryLogEntity>): AIAnalysisResult {
        return withContext(Dispatchers.IO) {
            val isAIEnabled = dataStoreManager.isAIEnabled()
            val provider = dataStoreManager.getAIProvider()
            
            // Try TensorFlow Lite first for offline AI
            if (isAIEnabled && provider == "tensorflow_lite") {
                val tfliteResult = getTensorFlowLiteAnalysis(batteryLogs)
                if (tfliteResult != null) {
                    return@withContext tfliteResult
                }
            }
            
            if (!isAIEnabled || provider == "rule_based") {
                return@withContext getRuleBasedAnalysis(batteryLogs)
            }
            
            val apiKey = dataStoreManager.getAPIKey(provider)
            if (apiKey.isNullOrEmpty()) {
                return@withContext getRuleBasedAnalysis(batteryLogs)
            }
            
            try {
                when (provider) {
                    "openai" -> getOpenAIAnalysis(batteryLogs, apiKey)
                    "gemini" -> getGeminiAnalysis(batteryLogs, apiKey)
                    "claude" -> getClaudeAnalysis(batteryLogs, apiKey)
                    "custom" -> getCustomAPIAnalysis(batteryLogs, apiKey)
                    else -> getRuleBasedAnalysis(batteryLogs)
                }
            } catch (e: Exception) {
                // Fallback to rule-based analysis if AI fails
                getRuleBasedAnalysis(batteryLogs)
            }
        }
    }

    private fun getRuleBasedAnalysis(batteryLogs: List<BatteryLogEntity>): AIAnalysisResult {
        val tips = RuleBasedCoach.getBatteryTips(batteryLogs)
        val healthScore = calculateHealthScore(batteryLogs)
        
        return AIAnalysisResult(
            recommendations = tips,
            insights = listOf(
                "Analysis based on battery usage patterns",
                "Enable AI for more detailed insights"
            ),
            healthScore = healthScore,
            predictedBatteryLife = getPredictedBatteryLife(batteryLogs),
            optimizationTips = getOptimizationTips(batteryLogs),
            riskFactors = getRiskFactors(batteryLogs)
        )
    }

    private suspend fun getOpenAIAnalysis(batteryLogs: List<BatteryLogEntity>, apiKey: String): AIAnalysisResult {
        val prompt = createBatteryAnalysisPrompt(batteryLogs)
        val response = callOpenAI(prompt, apiKey)
        return parseAIResponse(response)
    }

    private suspend fun getGeminiAnalysis(batteryLogs: List<BatteryLogEntity>, apiKey: String): AIAnalysisResult {
        val prompt = createBatteryAnalysisPrompt(batteryLogs)
        val response = callGemini(prompt, apiKey)
        return parseAIResponse(response)
    }

    private suspend fun getClaudeAnalysis(batteryLogs: List<BatteryLogEntity>, apiKey: String): AIAnalysisResult {
        val prompt = createBatteryAnalysisPrompt(batteryLogs)
        val response = callClaude(prompt, apiKey)
        return parseAIResponse(response)
    }

    private suspend fun getCustomAPIAnalysis(batteryLogs: List<BatteryLogEntity>, apiKey: String): AIAnalysisResult {
        val customUrl = dataStoreManager.getCustomAPIUrl()
        if (customUrl.isNullOrEmpty()) {
            return getRuleBasedAnalysis(batteryLogs)
        }
        
        val prompt = createBatteryAnalysisPrompt(batteryLogs)
        val response = callCustomAPI(prompt, apiKey, customUrl)
        return parseAIResponse(response)
    }

    private fun createBatteryAnalysisPrompt(batteryLogs: List<BatteryLogEntity>): String {
        val recentLogs = batteryLogs.take(10)
        val batteryData = recentLogs.joinToString("\n") { log ->
            "Time: ${log.timestamp}, Battery: ${log.batteryPercentage}%, Temp: ${log.temperature}°C, Status: ${log.status}"
        }
        
        return """
        Analyze this battery data and provide insights:
        
        Battery Data:
        $batteryData
        
        Please provide:
        1. Battery health recommendations (3-5 specific tips)
        2. Usage insights (2-3 observations)
        3. Health score (0-100)
        4. Predicted battery lifespan
        5. Optimization tips (3-4 actionable items)
        6. Risk factors (if any)
        
        Format as JSON with keys: recommendations, insights, healthScore, predictedBatteryLife, optimizationTips, riskFactors
        """.trimIndent()
    }

    private suspend fun callOpenAI(prompt: String, apiKey: String): String {
        val url = URL("https://api.openai.com/v1/chat/completions")
        val connection = url.openConnection() as HttpURLConnection
        
        connection.requestMethod = "POST"
        connection.setRequestProperty("Authorization", "Bearer $apiKey")
        connection.setRequestProperty("Content-Type", "application/json")
        connection.doOutput = true
        
        val requestBody = JSONObject().apply {
            put("model", "gpt-3.5-turbo")
            put("messages", org.json.JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "user")
                    put("content", prompt)
                })
            })
            put("max_tokens", 500)
        }
        
        connection.outputStream.use { it.write(requestBody.toString().toByteArray()) }
        
        return connection.inputStream.bufferedReader().use { it.readText() }
    }

    private suspend fun callGemini(prompt: String, apiKey: String): String {
        val url = URL("https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent?key=$apiKey")
        val connection = url.openConnection() as HttpURLConnection
        
        connection.requestMethod = "POST"
        connection.setRequestProperty("Content-Type", "application/json")
        connection.doOutput = true
        
        val requestBody = JSONObject().apply {
            put("contents", org.json.JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", org.json.JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", prompt)
                        })
                    })
                })
            })
        }
        
        connection.outputStream.use { it.write(requestBody.toString().toByteArray()) }
        
        return connection.inputStream.bufferedReader().use { it.readText() }
    }

    private suspend fun callClaude(prompt: String, apiKey: String): String {
        val url = URL("https://api.anthropic.com/v1/messages")
        val connection = url.openConnection() as HttpURLConnection
        
        connection.requestMethod = "POST"
        connection.setRequestProperty("x-api-key", apiKey)
        connection.setRequestProperty("Content-Type", "application/json")
        connection.setRequestProperty("anthropic-version", "2023-06-01")
        connection.doOutput = true
        
        val requestBody = JSONObject().apply {
            put("model", "claude-3-sonnet-20240229")
            put("max_tokens", 500)
            put("messages", org.json.JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "user")
                    put("content", prompt)
                })
            })
        }
        
        connection.outputStream.use { it.write(requestBody.toString().toByteArray()) }
        
        return connection.inputStream.bufferedReader().use { it.readText() }
    }

    private suspend fun callCustomAPI(prompt: String, apiKey: String, customUrl: String): String {
        val url = URL(customUrl)
        val connection = url.openConnection() as HttpURLConnection
        
        connection.requestMethod = "POST"
        connection.setRequestProperty("Authorization", "Bearer $apiKey")
        connection.setRequestProperty("Content-Type", "application/json")
        connection.doOutput = true
        
        val requestBody = JSONObject().apply {
            put("prompt", prompt)
            put("max_tokens", 500)
        }
        
        connection.outputStream.use { it.write(requestBody.toString().toByteArray()) }
        
        return connection.inputStream.bufferedReader().use { it.readText() }
    }

    private fun parseAIResponse(response: String): AIAnalysisResult {
        return try {
            val jsonResponse = JSONObject(response)
            // Parse based on provider response format
            // This is a simplified parser - you'd need to handle each provider's format
            AIAnalysisResult(
                recommendations = listOf("AI-powered recommendation"),
                insights = listOf("AI-generated insight"),
                healthScore = 85,
                predictedBatteryLife = "2-3 years",
                optimizationTips = listOf("AI optimization tip"),
                riskFactors = listOf()
            )
        } catch (e: Exception) {
            // Fallback if parsing fails
            AIAnalysisResult(
                recommendations = listOf("Enable proper AI configuration for detailed analysis"),
                insights = listOf("AI analysis temporarily unavailable"),
                healthScore = 75,
                predictedBatteryLife = "Unknown",
                optimizationTips = listOf("Check AI settings"),
                riskFactors = listOf()
            )
        }
    }

    private fun calculateHealthScore(batteryLogs: List<BatteryLogEntity>): Int {
        if (batteryLogs.isEmpty()) return 75
        
        val recentLogs = batteryLogs.take(5)
        var score = 100
        
        // Deduct points for high temperature
        val avgTemp = recentLogs.map { it.temperature }.average()
        if (avgTemp > 40) score -= 20
        else if (avgTemp > 35) score -= 10
        
        // Deduct points for rapid discharge
        if (recentLogs.size >= 2) {
            val firstLog = recentLogs.last()
            val lastLog = recentLogs.first()
            val timeDiffHours = (lastLog.timestamp - firstLog.timestamp) / (1000.0 * 60 * 60)
            val batteryDrop = firstLog.batteryPercentage - lastLog.batteryPercentage
            
            if (timeDiffHours > 0 && batteryDrop / timeDiffHours > 15) {
                score -= 15
            }
        }
        
        return maxOf(0, minOf(100, score))
    }

    private fun getPredictedBatteryLife(batteryLogs: List<BatteryLogEntity>): String {
        val healthScore = calculateHealthScore(batteryLogs)
        return when {
            healthScore >= 90 -> "3+ years"
            healthScore >= 75 -> "2-3 years"
            healthScore >= 60 -> "1-2 years"
            else -> "Less than 1 year"
        }
    }

    private fun getOptimizationTips(batteryLogs: List<BatteryLogEntity>): List<String> {
        return listOf(
            "Avoid charging to 100% regularly",
            "Keep device temperature below 35°C",
            "Use battery saver mode when needed",
            "Close unused background apps"
        )
    }

    private fun getRiskFactors(batteryLogs: List<BatteryLogEntity>): List<String> {
        val risks = mutableListOf<String>()
        
        if (batteryLogs.isNotEmpty()) {
            val avgTemp = batteryLogs.take(5).map { it.temperature }.average()
            if (avgTemp > 40) {
                risks.add("High operating temperature detected")
            }
        }
        
        return risks
    }

    private fun getTensorFlowLiteAnalysis(batteryLogs: List<BatteryLogEntity>): AIAnalysisResult? {
        return try {
            if (!tensorFlowLiteHelper.isModelLoaded()) {
                // Try to load the model
                val modelLoaded = tensorFlowLiteHelper.loadModel("battery_model.tflite")
                if (!modelLoaded) {
                    Log.w("AIBatteryAnalyzer", "Failed to load TensorFlow Lite model")
                    return null
                }
            }

            if (batteryLogs.isEmpty()) {
                return null
            }

            // Prepare data for TensorFlow Lite inference
            val recentLog = batteryLogs.first()
            val batteryData = floatArrayOf(
                recentLog.batteryPercentage.toFloat(),
                recentLog.voltage.toFloat(),
                recentLog.temperature.toFloat(),
                recentLog.currentNow.toFloat(),
                (System.currentTimeMillis() - recentLog.timestamp).toFloat() / (1000 * 60 * 60), // hours since last reading
                if (recentLog.status == android.os.BatteryManager.BATTERY_STATUS_CHARGING) 1f else 0f
            )

            // Run battery prediction
            val prediction = tensorFlowLiteHelper.runBatteryPrediction(batteryData)
            if (prediction != null && prediction.size >= 3) {
                val drainRate = prediction[0]
                val healthScore = (prediction[1] * 100).toInt().coerceIn(0, 100)
                val timeToEmpty = prediction[2]

                // Prepare historical data for health analysis
                val historicalData = batteryLogs.take(10).map { log ->
                    floatArrayOf(
                        log.batteryPercentage.toFloat(),
                        log.voltage.toFloat(),
                        log.temperature.toFloat(),
                        log.currentNow.toFloat(),
                        (System.currentTimeMillis() - log.timestamp).toFloat() / (1000 * 60 * 60),
                        if (log.status == android.os.BatteryManager.BATTERY_STATUS_CHARGING) 1f else 0f
                    )
                }.toTypedArray()

                val healthAnalysis = tensorFlowLiteHelper.runHealthAnalysis(historicalData)
                
                val recommendations = generateTFLiteRecommendations(drainRate, healthScore, timeToEmpty)
                val insights = generateTFLiteInsights(prediction, healthAnalysis)
                val optimizationTips = generateTFLiteOptimizationTips(drainRate, healthScore)
                val riskFactors = generateTFLiteRiskFactors(prediction, healthAnalysis)

                return AIAnalysisResult(
                    recommendations = recommendations,
                    insights = insights,
                    healthScore = healthScore,
                    predictedBatteryLife = formatPredictedLife(healthAnalysis?.get(3) ?: 24f),
                    optimizationTips = optimizationTips,
                    riskFactors = riskFactors
                )
            }

            null
        } catch (e: Exception) {
            Log.e("AIBatteryAnalyzer", "TensorFlow Lite analysis failed", e)
            null
        }
    }

    private fun generateTFLiteRecommendations(drainRate: Float, healthScore: Int, timeToEmpty: Float): List<String> {
        val recommendations = mutableListOf<String>()
        
        if (drainRate > 15f) {
            recommendations.add("High battery drain detected. Consider closing background apps.")
        }
        
        if (healthScore < 70) {
            recommendations.add("Battery health is declining. Avoid extreme temperatures and overcharging.")
        }
        
        if (timeToEmpty < 2f) {
            recommendations.add("Low battery predicted. Enable power saving mode.")
        }
        
        if (recommendations.isEmpty()) {
            recommendations.add("Battery performance is optimal. Continue current usage patterns.")
        }
        
        return recommendations
    }

    private fun generateTFLiteInsights(prediction: FloatArray, healthAnalysis: FloatArray?): List<String> {
        val insights = mutableListOf<String>()
        
        insights.add("AI-powered analysis using on-device machine learning")
        insights.add("Predicted drain rate: ${String.format("%.1f", prediction[0])}%/hour")
        
        healthAnalysis?.let { health ->
            if (health.size >= 2) {
                insights.add("Battery degradation rate: ${String.format("%.2f", health[1])}% per month")
            }
        }
        
        return insights
    }

    private fun generateTFLiteOptimizationTips(drainRate: Float, healthScore: Int): List<String> {
        val tips = mutableListOf<String>()
        
        if (drainRate > 10f) {
            tips.add("Reduce screen brightness to extend battery life")
            tips.add("Disable location services for unused apps")
        }
        
        if (healthScore < 80) {
            tips.add("Charge between 20-80% to preserve battery health")
            tips.add("Avoid fast charging when not needed")
        }
        
        tips.add("Use dark mode to reduce OLED display power consumption")
        
        return tips
    }

    private fun generateTFLiteRiskFactors(prediction: FloatArray, healthAnalysis: FloatArray?): List<String> {
        val risks = mutableListOf<String>()
        
        if (prediction[1] < 0.6f) { // Health score below 60%
            risks.add("Battery health is critically low")
        }
        
        healthAnalysis?.let { health ->
            if (health.size >= 4 && health[3] < 12f) { // Less than 12 months predicted life
                risks.add("Battery replacement may be needed soon")
            }
        }
        
        return risks
    }

    private fun formatPredictedLife(lifespanMonths: Float): String {
        return when {
            lifespanMonths >= 36 -> "3+ years"
            lifespanMonths >= 24 -> "2-3 years"
            lifespanMonths >= 12 -> "1-2 years"
            else -> "Less than 1 year"
        }
    }
}
