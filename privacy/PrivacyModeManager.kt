package com.voltai.smartbatteryguardian.privacy

import android.content.Context
import android.content.SharedPreferences

class PrivacyModeManager(private val context: Context) {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(PRIVACY_PREFS, Context.MODE_PRIVATE)

    fun isPrivacyModeEnabled(): Boolean {
        return sharedPreferences.getBoolean(PRIVACY_MODE_KEY, false)
    }

    fun setPrivacyMode(enabled: Boolean) {
        sharedPreferences.edit().putBoolean(PRIVACY_MODE_KEY, enabled).apply()
    }

    companion object {
        private const val PRIVACY_PREFS = "privacy_preferences"
        private const val PRIVACY_MODE_KEY = "privacy_mode_enabled"
    }
}
