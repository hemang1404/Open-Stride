package com.openstride.engine

import com.openstride.data.model.TrackPoint
import kotlinx.coroutines.flow.SharedFlow

/**
 * Interface defining the requirements for a tracking engine.
 * This allows the app to switch between "GPS Only" and "Sensor Fusion" modes easily.
 */
interface TrackingEngine {
    
    /**
     * A stream of location updates captured by the engine.
     * SharedFlow is used so multiple parts of the app (Service, UI) can listen to the same stream.
     */
    val locationUpdates: SharedFlow<TrackPoint>

    /**
     * Starts the tracking process.
     */
    fun startTracking()

    /**
     * Stops the tracking process.
     */
    fun stopTracking()

    /**
     * Adjusts the sampling rate (how often we request a point).
     * @property intervalMillis Time between samples in milliseconds.
     */
    fun setUpdateInterval(intervalMillis: Long)
}
