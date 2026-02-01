package com.openstride.data.repository

import android.content.Context
import android.content.SharedPreferences

/**
 * Repository for managing user preferences and settings.
 */
class SettingsRepository(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        "openstride_settings", 
        Context.MODE_PRIVATE
    )

    companion object {
        const val KEY_USE_METRIC = "use_metric"
        const val KEY_TRACKING_INTERVAL = "tracking_interval_ms"
    }

    fun isMetric(): Boolean = prefs.getBoolean(KEY_USE_METRIC, true)

    fun setMetric(isMetric: Boolean) {
        prefs.edit().putBoolean(KEY_USE_METRIC, isMetric).apply()
    }

    fun getTrackingInterval(): Long = prefs.getLong(KEY_TRACKING_INTERVAL, 1000L)

    fun setTrackingInterval(intervalMs: Long) {
        prefs.edit().putLong(KEY_TRACKING_INTERVAL, intervalMs).apply()
    }
}
