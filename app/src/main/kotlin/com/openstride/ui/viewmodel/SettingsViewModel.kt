package com.openstride.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.openstride.ServiceLocator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * ViewModel for the Settings Screen.
 */
class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ServiceLocator.provideSettingsRepository(application)

    private val _isMetric = MutableStateFlow(repository.isMetric())
    val isMetric: StateFlow<Boolean> = _isMetric.asStateFlow()

    private val _trackingInterval = MutableStateFlow(repository.getTrackingInterval())
    val trackingInterval: StateFlow<Long> = _trackingInterval.asStateFlow()

    fun toggleUnits() {
        val newVal = !_isMetric.value
        repository.setMetric(newVal)
        _isMetric.value = newVal
    }

    fun updateInterval(intervalMs: Long) {
        repository.setTrackingInterval(intervalMs)
        _trackingInterval.value = intervalMs
    }
}
