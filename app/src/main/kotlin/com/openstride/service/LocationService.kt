package com.openstride.service

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.openstride.MainActivity
import com.openstride.R
import com.openstride.ServiceLocator
import com.openstride.data.model.Session
import com.openstride.data.repository.TrackingRepository
import com.openstride.engine.TrackingEngine
import kotlinx.coroutines.*
import java.util.*

/**
 * Foreground Service that handles location tracking even when the app is in background.
 */
class LocationService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private lateinit var trackingEngine: TrackingEngine
    private lateinit var repository: TrackingRepository
    private lateinit var settingsRepository: com.openstride.data.repository.SettingsRepository
    
    private var currentSessionId: String? = null
    private var isPaused = false
    private var trackingJob: Job? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        repository = ServiceLocator.provideRepository(this)
        settingsRepository = ServiceLocator.provideSettingsRepository(this)
        trackingEngine = ServiceLocator.provideTrackingEngine(this)
        
        // Apply interval from settings
        trackingEngine.setUpdateInterval(settingsRepository.getTrackingInterval())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> startTracking()
            ACTION_STOP -> stopTracking()
            ACTION_PAUSE -> pauseTracking()
            ACTION_RESUME -> resumeTracking()
        }
        return START_STICKY
    }

    private fun startTracking() {
        val sessionId = UUID.randomUUID().toString()
        currentSessionId = sessionId
        isPaused = false

        trackingJob = serviceScope.launch {
            val session = Session(
                sessionId = sessionId,
                startTime = System.currentTimeMillis(),
                mode = "GPS_ONLY"
            )
            repository.insertSession(session)

            startForeground(NOTIFICATION_ID, createNotification(getString(R.string.notification_starting)))
            
            // CRITICAL FIX: Start the hardware engine!
            trackingEngine.startTracking()

            // 4. Collect and save every GPS point
            var lastSavedPoint: com.openstride.data.model.TrackPoint? = null
            var totalDistanceMeters = 0.0
            val recentPoints = mutableListOf<com.openstride.data.model.TrackPoint>()
            
            trackingEngine.locationUpdates.collect { point ->
                if (isPaused) return@collect // Skip points if paused

                if (com.openstride.util.LocationFilter.shouldAcceptPoint(point, lastSavedPoint)) {
                    val smoothedPoint = com.openstride.util.LocationSmoother.smooth(point, recentPoints)
                    val pointWithSession = smoothedPoint.copy(sessionId = sessionId)
                    
                    // Accumulate distance
                    lastSavedPoint?.let { last ->
                        totalDistanceMeters += com.openstride.util.DistanceCalculator.calculateVincentyDistance(
                            last.latitude, last.longitude,
                            smoothedPoint.latitude, smoothedPoint.longitude
                        )
                    }
                    
                    repository.insertTrackPoint(pointWithSession)
                    lastSavedPoint = pointWithSession
                    recentPoints.add(pointWithSession)
                    if (recentPoints.size > 5) recentPoints.removeAt(0)

                    // Update session confidence and total distance
                    val currentSession = repository.getSessionById(sessionId)
                    currentSession?.let { session ->
                        val newConfidence = com.openstride.util.ConfidenceScoring.calculateSessionConfidence(recentPoints)
                        repository.updateSession(session.copy(
                            confidenceScore = newConfidence,
                            totalDistance = totalDistanceMeters
                        ))
                    }
                    
                    val kmDisplay = String.format(Locale.US, "%.2f km", totalDistanceMeters / 1000.0)
                    updateNotification(getString(R.string.notification_tracking, kmDisplay))
                }
            }
        }
    }

    private fun stopTracking() {
        trackingJob?.cancel()
        trackingEngine.stopTracking()
        
        serviceScope.launch {
            currentSessionId?.let { id ->
                val session = repository.getSessionById(id)
                session?.let {
                    repository.updateSession(it.copy(endTime = System.currentTimeMillis()))
                }
            }
            
            withContext(Dispatchers.Main) {
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
    }

    private fun pauseTracking() {
        isPaused = true
        updateNotification(getString(R.string.notification_paused))
        serviceScope.launch {
            currentSessionId?.let { id ->
                val session = repository.getSessionById(id)
                session?.let {
                    repository.updateSession(it.copy(isPaused = true))
                }
            }
        }
    }

    private fun resumeTracking() {
        isPaused = false
        updateNotification(getString(R.string.notification_resuming))
        serviceScope.launch {
            currentSessionId?.let { id ->
                val session = repository.getSessionById(id)
                session?.let {
                    repository.updateSession(it.copy(isPaused = false))
                }
            }
        }
    }

    private fun createNotification(content: String): Notification {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        return NotificationCompat.Builder(this, "tracking_channel")
            .setContentTitle(getString(R.string.notification_title))
            .setContentText(content)
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    private fun updateNotification(content: String) {
        val notification = createNotification(content)
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        trackingJob?.cancel()
        trackingEngine.stopTracking()
        serviceScope.cancel()
    }

    companion object {
        const val ACTION_START = "ACTION_START"
        const val ACTION_STOP = "ACTION_STOP"
        const val ACTION_PAUSE = "ACTION_PAUSE"
        const val ACTION_RESUME = "ACTION_RESUME"
        const val NOTIFICATION_ID = 1
    }
}
