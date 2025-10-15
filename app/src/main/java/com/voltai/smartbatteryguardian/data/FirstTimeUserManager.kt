package com.voltai.smartbatteryguardian.data

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages first-time user experience, onboarding, and data collection progress
 */
@Singleton
class FirstTimeUserManager
@Inject
constructor(
    @ApplicationContext private val context: Context,
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences(
            "voltai_first_time_user",
            Context.MODE_PRIVATE,
        )

    private val _firstTimeUserState = MutableStateFlow(createInitialState())
    val firstTimeUserState: StateFlow<FirstTimeUserState> = _firstTimeUserState.asStateFlow()

    companion object {
        private const val KEY_IS_FIRST_LAUNCH = "is_first_launch"
        private const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"
        private const val KEY_PERMISSIONS_REQUESTED = "permissions_requested"
        private const val KEY_DATA_COLLECTION_START_TIME = "data_collection_start_time"
        private const val KEY_INITIAL_DATA_COLLECTED = "initial_data_collected"
        private const val KEY_SUFFICIENT_DATA_AVAILABLE = "sufficient_data_available"

        // Minimum time required for meaningful battery analysis (in milliseconds)
        private const val MIN_DATA_COLLECTION_TIME = 30 * 60 * 1000L // 30 minutes
        private const val RECOMMENDED_DATA_TIME = 24 * 60 * 60 * 1000L // 24 hours
    }

    private fun createInitialState(): FirstTimeUserState {
        val isFirstLaunch = prefs.getBoolean(KEY_IS_FIRST_LAUNCH, true)
        val onboardingCompleted = prefs.getBoolean(KEY_ONBOARDING_COMPLETED, false)
        val permissionsRequested = prefs.getBoolean(KEY_PERMISSIONS_REQUESTED, false)
        val dataCollectionStartTime = prefs.getLong(KEY_DATA_COLLECTION_START_TIME, 0L)
        val initialDataCollected = prefs.getBoolean(KEY_INITIAL_DATA_COLLECTED, false)
        val sufficientDataAvailable = prefs.getBoolean(KEY_SUFFICIENT_DATA_AVAILABLE, false)

        val currentTime = System.currentTimeMillis()
        val dataCollectionDuration =
            if (dataCollectionStartTime > 0) {
                currentTime - dataCollectionStartTime
            } else {
                0L
            }

        return FirstTimeUserState(
            isFirstLaunch = isFirstLaunch,
            onboardingCompleted = onboardingCompleted,
            permissionsRequested = permissionsRequested,
            dataCollectionStartTime = dataCollectionStartTime,
            initialDataCollected = initialDataCollected,
            sufficientDataAvailable = sufficientDataAvailable,
            dataCollectionDuration = dataCollectionDuration,
            dataCollectionProgress = calculateDataCollectionProgress(dataCollectionDuration),
        )
    }

    /**
     * Mark that the app has been launched for the first time
     */
    fun markFirstLaunchCompleted() {
        prefs.edit()
            .putBoolean(KEY_IS_FIRST_LAUNCH, false)
            .apply()
        updateState()
    }

    /**
     * Mark onboarding as completed
     */
    fun markOnboardingCompleted() {
        prefs.edit()
            .putBoolean(KEY_ONBOARDING_COMPLETED, true)
            .apply()
        updateState()
    }

    /**
     * Mark that permissions have been requested
     */
    fun markPermissionsRequested() {
        prefs.edit()
            .putBoolean(KEY_PERMISSIONS_REQUESTED, true)
            .apply()
        updateState()
    }

    /**
     * Start data collection tracking
     */
    fun startDataCollection() {
        val currentTime = System.currentTimeMillis()
        prefs.edit()
            .putLong(KEY_DATA_COLLECTION_START_TIME, currentTime)
            .putBoolean(KEY_INITIAL_DATA_COLLECTED, true)
            .apply()
        updateState()
    }

    /**
     * Mark that sufficient data is now available for analysis
     */
    fun markSufficientDataAvailable() {
        prefs.edit()
            .putBoolean(KEY_SUFFICIENT_DATA_AVAILABLE, true)
            .apply()
        updateState()
    }

    /**
     * Reset all first-time user data (useful for testing)
     */
    fun resetFirstTimeUserData() {
        prefs.edit()
            .clear()
            .apply()
        updateState()
    }

    /**
     * Check if user should see onboarding
     */
    fun shouldShowOnboarding(): Boolean {
        val state = _firstTimeUserState.value
        return state.isFirstLaunch || !state.onboardingCompleted
    }

    /**
     * Check if app is in data collection phase
     */
    fun isInDataCollectionPhase(): Boolean {
        val state = _firstTimeUserState.value
        return state.initialDataCollected && !state.sufficientDataAvailable
    }

    /**
     * Get data collection status message
     */
    fun getDataCollectionStatusMessage(): String {
        val state = _firstTimeUserState.value

        return when {
            !state.initialDataCollected -> "Preparing to collect battery data..."
            state.sufficientDataAvailable -> "Battery analysis ready!"
            state.dataCollectionDuration < MIN_DATA_COLLECTION_TIME -> {
                val remainingMinutes = (MIN_DATA_COLLECTION_TIME - state.dataCollectionDuration) / (60 * 1000)
                "Collecting data... $remainingMinutes minutes for basic analysis"
            }
            state.dataCollectionDuration < RECOMMENDED_DATA_TIME -> {
                val remainingHours = (RECOMMENDED_DATA_TIME - state.dataCollectionDuration) / (60 * 60 * 1000)
                "Good progress! $remainingHours hours for detailed analysis"
            }
            else -> "Comprehensive battery data available!"
        }
    }

    /**
     * Get estimated time until sufficient data is available
     */
    fun getEstimatedTimeForSufficientData(): String {
        val state = _firstTimeUserState.value

        if (state.sufficientDataAvailable) return "Ready now!"

        val remainingTime = MIN_DATA_COLLECTION_TIME - state.dataCollectionDuration

        return when {
            remainingTime <= 0 -> "Ready soon!"
            remainingTime < 60 * 1000 -> "< 1 minute"
            remainingTime < 60 * 60 * 1000 -> "${remainingTime / (60 * 1000)} minutes"
            else -> "${remainingTime / (60 * 60 * 1000)} hours"
        }
    }

    private fun calculateDataCollectionProgress(duration: Long): Float {
        return when {
            duration <= 0 -> 0f
            duration >= RECOMMENDED_DATA_TIME -> 1f
            else -> (duration.toFloat() / RECOMMENDED_DATA_TIME).coerceIn(0f, 1f)
        }
    }

    private fun updateState() {
        _firstTimeUserState.value = createInitialState()

        // Auto-mark sufficient data after minimum time
        val state = _firstTimeUserState.value
        if (!state.sufficientDataAvailable &&
            state.dataCollectionDuration >= MIN_DATA_COLLECTION_TIME
        ) {
            markSufficientDataAvailable()
        }
    }

    /**
     * Update data collection progress (call this periodically)
     */
    fun updateDataCollectionProgress() {
        updateState()
    }
}

/**
 * Represents the first-time user experience state
 */
data class FirstTimeUserState(
    val isFirstLaunch: Boolean = true,
    val onboardingCompleted: Boolean = false,
    val permissionsRequested: Boolean = false,
    val dataCollectionStartTime: Long = 0L,
    val initialDataCollected: Boolean = false,
    val sufficientDataAvailable: Boolean = false,
    val dataCollectionDuration: Long = 0L,
    val dataCollectionProgress: Float = 0f,
) {
    val isDataCollectionPhase: Boolean
        get() = initialDataCollected && !sufficientDataAvailable

    val canShowDetailedAnalysis: Boolean
        get() = sufficientDataAvailable

    val canShowBasicAnalysis: Boolean
        get() = dataCollectionDuration >= 30 * 60 * 1000L // 30 minutes
}
