package com.openstride.ui.viewmodel

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.openstride.ServiceLocator
import com.openstride.data.model.Session
import com.openstride.data.model.TrackPoint
import com.openstride.service.LocationService
import com.openstride.util.DistanceCalculator
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * ViewModel for the Main Tracking Screen.
 * It manages the connection between the UI, the Repository, and the Background Service.
 */
class TrackingViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ServiceLocator.provideRepository(application)
    private val trackingEngine = ServiceLocator.provideTrackingEngine(application)

    // UI States
    private val _isTracking = MutableStateFlow(false)
    val isTracking: StateFlow<Boolean> = _isTracking.asStateFlow()

    private val _timerSeconds = MutableStateFlow(0L)
    val timerSeconds: StateFlow<Long> = _timerSeconds.asStateFlow()

    private val _currentDistance = MutableStateFlow(0.0)
    val currentDistance: StateFlow<Double> = _currentDistance.asStateFlow()

    private val _sessionPoints = MutableStateFlow<List<TrackPoint>>(emptyList())
    val sessionPoints: StateFlow<List<TrackPoint>> = _sessionPoints.asStateFlow()

    private var timerJob: Job? = null
    private var lastPoint: TrackPoint? = null

    init {
        // Observe the current session dots to update the path on the map
        viewModelScope.launch {
            trackingEngine.locationUpdates.collect { point ->
                if (_isTracking.value) {
                    val currentList = _sessionPoints.value.toMutableList()
                    currentList.add(point)
                    _sessionPoints.value = currentList

                    // Calculate distance from previous point
                    lastPoint?.let { last ->
                        val distance = DistanceCalculator.calculateHaversineDistance(
                            last.latitude, last.longitude,
                            point.latitude, point.longitude
                        )
                        _currentDistance.value += distance
                    }
                    lastPoint = point
                }
            }
        }
    }

    fun toggleTracking() {
        if (_isTracking.value) {
            stopTracking()
        } else {
            startTracking()
        }
    }

    private fun startTracking() {
        _isTracking.value = true
        _timerSeconds.value = 0
        _currentDistance.value = 0.0
        _sessionPoints.value = emptyList()
        lastPoint = null
        
        startTimer()

        // Start the background service
        val intent = Intent(getApplication(), LocationService::class.java).apply {
            action = LocationService.ACTION_START
        }
        getApplication<Application>().startForegroundService(intent)
    }

    private fun stopTracking() {
        _isTracking.value = false
        stopTimer()

        // Stop the background service
        val intent = Intent(getApplication(), LocationService::class.java).apply {
            action = LocationService.ACTION_STOP
        }
        getApplication<Application>().startService(intent)
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                _timerSeconds.value++
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
    }

    override fun onCleared() {
        super.onCleared()
        stopTimer()
    }
}
