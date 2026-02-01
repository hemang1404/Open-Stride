package com.openstride.service

import android.app.Notification
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
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
    
    private var currentSessionId: String? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        repository = ServiceLocator.provideRepository(this)
        trackingEngine = ServiceLocator.provideTrackingEngine(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> startTracking()
            ACTION_STOP -> stopTracking()
        }
        return START_STICKY
    }

    private fun startTracking() {
        val sessionId = UUID.randomUUID().toString()
        currentSessionId = sessionId

        serviceScope.launch {
            val session = Session(
                sessionId = sessionId,
                startTime = System.currentTimeMillis(),
                mode = "GPS_ONLY"
            )
            repository.insertSession(session)

            startForeground(NOTIFICATION_ID, createNotification("Starting walk tracking..."))
            
            // CRITICAL FIX: Start the hardware engine!
            trackingEngine.startTracking()

            // 4. Collect and save every GPS point
            var lastSavedPoint: com.openstride.data.model.TrackPoint? = null
            var totalDistanceMeters = 0.0
            val recentPoints = mutableListOf<com.openstride.data.model.TrackPoint>()
            
            trackingEngine.locationUpdates.collect { point ->
                if (com.openstride.util.LocationFilter.shouldAcceptPoint(point, lastSavedPoint)) {
                    val smoothedPoint = com.openstride.util.LocationSmoother.smooth(point, recentPoints)
                    val pointWithSession = smoothedPoint.copy(sessionId = sessionId)
                    
                    // Accumulate distance
                    lastSavedPoint?.let { last ->
                        totalDistanceMeters += com.openstride.util.DistanceCalculator.calculateHaversineDistance(
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
                    updateNotification("Tracking: $kmDisplay recorded")
                }
            }
        }
    }

    private fun stopTracking() {
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

    private fun createNotification(content: String): Notification {
        return NotificationCompat.Builder(this, "tracking_channel")
            .setContentTitle("OpenStride Active")
            .setContentText(content)
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
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
        serviceScope.cancel()
    }

    companion object {
        const val ACTION_START = "ACTION_START"
        const val ACTION_STOP = "ACTION_STOP"
        const val NOTIFICATION_ID = 1
    }
}
