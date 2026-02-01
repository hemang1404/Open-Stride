package com.openstride.util

import com.openstride.data.model.TrackPoint
import kotlin.math.abs

/**
 * Utility for filtering and smoothing GPS data to ensure "solid" distance measurements.
 * It removes outliers (GPS jumps) and smooths jitter.
 */
object LocationFilter {

    private const val MIN_ACCURACY_THRESHOLD = 25.0f // Meters. Ignore points worse than this.
    private const val MAX_SPEED_WALKING = 6.0f // m/s (~21 km/h). Faster is likely a GPS jump if walking.
    private const val STALE_THRESHOLD_MS = 10000 // 10 seconds.
    
    /**
     * Determines if a new point should be accepted based on the previous point.
     */
    fun shouldAcceptPoint(newPoint: TrackPoint, lastPoint: TrackPoint?): Boolean {
        // 1. Basic Accuracy Check
        if (newPoint.accuracy > MIN_ACCURACY_THRESHOLD) return false

        if (lastPoint == null) return true // First point is always accepted if accurate enough

        // 2. Time delta check
        val timeDelta = newPoint.timestamp - lastPoint.timestamp
        if (timeDelta <= 0) return false // Ignore duplicate or out-of-order points

        // 3. Distance & Speed check (The "Jump" filter)
        val distance = DistanceCalculator.calculateHaversineDistance(
            lastPoint.latitude, lastPoint.longitude,
            newPoint.latitude, newPoint.longitude
        )
        
        val calculatedSpeed = distance / (timeDelta / 1000.0)
        
        // If speed is impossible for a human walk, it's likely a GPS jitter
        if (calculatedSpeed > MAX_SPEED_WALKING && timeDelta < STALE_THRESHOLD_MS) {
            return false
        }

        return true
    }

    /**
     * Basic smoothing: Weighted average of the last few points could be added here.
     * For now, we focus on outlier rejection.
     */
}
