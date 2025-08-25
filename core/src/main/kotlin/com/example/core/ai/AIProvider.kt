package com.voltai.core.ai

enum class AIProvider(val displayName: String, val id: String) {
    TENSORFLOW_LITE("TensorFlow Lite (Offline)", "tensorflow_lite"),
    OPENAI("OpenAI GPT", "openai"),
    GEMINI("Google Gemini", "gemini"),
    CLAUDE("Anthropic Claude", "claude"),
    CUSTOM("Custom API", "custom"),
    RULE_BASED("Rule-based (No API)", "rule_based")
}

data class AIConfig(
    val provider: AIProvider,
    val apiKey: String?,
    val customUrl: String? = null,
    val model: String? = null
)

data class BatteryAnalysisRequest(
    val batteryPercentage: Int,
    val temperature: Float,
    val voltage: Float,
    val chargingStatus: String,
    val screenOnTime: Long,
    val screenOffTime: Long,
    val appUsageData: List<AppUsage>,
    val historicalData: List<BatteryDataPoint>
)

data class AppUsage(
    val packageName: String,
    val appName: String,
    val batteryUsage: Double,
    val screenTime: Long
)

data class BatteryDataPoint(
    val timestamp: Long,
    val batteryPercentage: Int,
    val temperature: Float,
    val chargingStatus: String
)

data class AIAnalysisResult(
    val recommendations: List<String>,
    val insights: List<String>,
    val healthScore: Int, // 0-100
    val predictedBatteryLife: String,
    val optimizationTips: List<String>,
    val riskFactors: List<String>
)
