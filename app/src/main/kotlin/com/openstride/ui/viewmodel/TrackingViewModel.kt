package com.openstride.ui.viewmodel

import android.Manifest
import android.app.Application
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase
import com.openstride.R
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
    private val analytics = Firebase.analytics

    // UI States
    private val _isTracking = MutableStateFlow(false)
    val isTracking: StateFlow<Boolean> = _isTracking.asStateFlow()

    private val _isPaused = MutableStateFlow(false)
    val isPaused: StateFlow<Boolean> = _isPaused.asStateFlow()

    private val _timerSeconds = MutableStateFlow(0L)
    val timerSeconds: StateFlow<Long> = _timerSeconds.asStateFlow()

    private val _currentDistance = MutableStateFlow(0.0)
    val currentDistance: StateFlow<Double> = _currentDistance.asStateFlow()

    private val _sessionPoints = MutableStateFlow<List<TrackPoint>>(emptyList())
    val sessionPoints: StateFlow<List<TrackPoint>> = _sessionPoints.asStateFlow()

    private val _hasLocationPermission = MutableStateFlow(false)
    val hasLocationPermission: StateFlow<Boolean> = _hasLocationPermission.asStateFlow()

    private val _needsBackgroundPermission = MutableStateFlow(false)
    val needsBackgroundPermission: StateFlow<Boolean> = _needsBackgroundPermission.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _showOnboarding = MutableStateFlow(false)
    val showOnboarding: StateFlow<Boolean> = _showOnboarding.asStateFlow()

    val allSessions: Flow<List<Session>> = repository.allSessions

    private var timerJob: Job? = null
    private var activeSessionId: String? = null

    init {
        // Check if onboarding is needed
        val prefs = getApplication<Application>().getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE)
        _showOnboarding.value = !prefs.getBoolean("onboarding_completed", false)
        
        // Check permission status
        checkPermission()
        
        // Observe the active session to resume UI state if app was killed
        viewModelScope.launch {
            val activeSession = repository.getActiveSession()
            if (activeSession != null) {
                resumeTrackingState(activeSession)
            }
        }
    }

    private fun checkPermission() {
        val hasFineLocation = ContextCompat.checkSelfPermission(
            getApplication(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        
        _hasLocationPermission.value = hasFineLocation
        
        // Check background location for Android 10+ (API 29+)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            val hasBackgroundLocation = ContextCompat.checkSelfPermission(
                getApplication(),
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
            _needsBackgroundPermission.value = hasFineLocation && !hasBackgroundLocation
        } else {
            _needsBackgroundPermission.value = false
        }
    }

    private fun resumeTrackingState(session: Session) {
        activeSessionId = session.sessionId
        _isTracking.value = true
        _isPaused.value = session.isPaused
        _currentDistance.value = session.totalDistance
        
        // Set timer to saved elapsed time
        _timerSeconds.value = session.elapsedTimeSeconds
        
        // Only start timer if not paused
        if (!session.isPaused) {
            startTimer()
        }

        // Observe points for this session
        viewModelScope.launch {
            repository.getPointsForSession(session.sessionId).collect { points ->
                _sessionPoints.value = points
                
                // Keep distance in sync with DB
                val currentSession = repository.getSessionById(session.sessionId)
                currentSession?.let { _currentDistance.value = it.totalDistance }
            }
        }
    }

    fun setHasPermission(hasPermission: Boolean) {
        _hasLocationPermission.value = hasPermission
        checkPermission() // Re-check for background permission
    }

    fun setBackgroundPermissionGranted() {
        _needsBackgroundPermission.value = false
    }

    fun completeOnboarding() {
        val prefs = getApplication<Application>().getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE)
        prefs.edit().putBoolean("onboarding_completed", true).apply()
        _showOnboarding.value = false
        analytics.logEvent("onboarding_completed", null)
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun toggleTracking() {
        if (_isTracking.value) {
            stopTracking()
        } else {
            startTracking()
        }
    }

    fun togglePause() {
        if (!_isTracking.value) return
        
        if (_isPaused.value) {
            resumeTracking()
        } else {
            pauseTracking()
        }
    }

    private fun startTracking() {
        // Check permission first
        if (!_hasLocationPermission.value) {
            _errorMessage.value = getApplication<Application>().getString(R.string.error_location_required)
            return
        }

        // Warn about background permission on Android 10+
        if (_needsBackgroundPermission.value && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            _errorMessage.value = getApplication<Application>().getString(R.string.error_background_recommended)
            // Continue anyway, but user is warned
        }

        _isTracking.value = true
        _isPaused.value = false
        _timerSeconds.value = 0
        _currentDistance.value = 0.0
        _sessionPoints.value = emptyList()
        
        startTimer()

        try {
            // Log analytics event
            analytics.logEvent("tracking_started") {
                param("has_background_permission", !_needsBackgroundPermission.value)
            }
            
            // Start the background service
            val intent = Intent(getApplication(), LocationService::class.java).apply {
                action = LocationService.ACTION_START
            }
            getApplication<Application>().startForegroundService(intent)

            // Wait a small bit for DB to create session, then observe it
            viewModelScope.launch {
                delay(500)
                repository.getActiveSession()?.let { resumeTrackingState(it) }
            }
        } catch (e: Exception) {
            _errorMessage.value = getApplication<Application>().getString(
                R.string.error_tracking_failed,
                e.message ?: "Unknown error"
            )
            _isTracking.value = false
            stopTimer()
        }
    }

    private fun stopTracking() {
        _isTracking.value = false
        _isPaused.value = false
        stopTimer()
        
        // Log analytics event
        analytics.logEvent("tracking_stopped") {
            param("distance_meters", _currentDistance.value)
            param("duration_seconds", _timerSeconds.value.toDouble())
        }
        
        // Save final elapsed time
        val finalElapsedTime = _timerSeconds.value
        activeSessionId = null

        // Stop the background service
        val intent = Intent(getApplication(), LocationService::class.java).apply {
            action = LocationService.ACTION_STOP
        }
        getApplication<Application>().startService(intent)
        
        // Update session with final time
        viewModelScope.launch {
            val sessions = repository.allSessions.first()
            sessions.firstOrNull { it.endTime == null }?.let { session ->
                repository.updateSession(session.copy(elapsedTimeSeconds = finalElapsedTime))
            }
        }
    }

    private fun pauseTracking() {
        _isPaused.value = true
        stopTimer()
        
        // Save current elapsed time and pause timestamp
        viewModelScope.launch {
            activeSessionId?.let { id ->
                val session = repository.getSessionById(id)
                session?.let {
                    repository.updateSession(it.copy(
                        isPaused = true,
                        elapsedTimeSeconds = _timerSeconds.value,
                        lastPauseTime = System.currentTimeMillis()
                    ))
                }
            }
        }
        
        val intent = Intent(getApplication(), LocationService::class.java).apply {
            action = LocationService.ACTION_PAUSE
        }
        getApplication<Application>().startService(intent)
    }

    private fun resumeTracking() {
        _isPaused.value = false
        startTimer()
        
        // Update database to mark as not paused
        viewModelScope.launch {
            activeSessionId?.let { id ->
                val session = repository.getSessionById(id)
                session?.let {
                    repository.updateSession(it.copy(
                        isPaused = false,
                        lastPauseTime = null
                    ))
                }
            }
        }
        
        val intent = Intent(getApplication(), LocationService::class.java).apply {
            action = LocationService.ACTION_RESUME
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

    // Helper methods for other screens
    suspend fun getSessionById(sessionId: String): Session? = repository.getSessionById(sessionId)
    
    fun getPointsForSession(sessionId: String): Flow<List<TrackPoint>> = repository.getPointsForSession(sessionId)

    override fun onCleared() {
        super.onCleared()
        stopTimer()
    }
}
