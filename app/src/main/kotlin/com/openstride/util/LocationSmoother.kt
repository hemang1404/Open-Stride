package com.openstride.util

import com.openstride.data.model.TrackPoint

/**
 * Utility for smoothing GPS points using a Weighted Moving Average.
 * This helps reduce the "zig-zag" effect of GPS jitter.
 */
object LocationSmoother {

    private const val WINDOW_SIZE = 3 // Number of points to average

    /**
     * Returns a smoothed version of the latest point by averaging it with previous points.
     */
    fun smooth(newPoint: TrackPoint, recentPoints: List<TrackPoint>): TrackPoint {
        if (recentPoints.isEmpty()) return newPoint

        // We take the last few points (up to WINDOW_SIZE - 1) + the new one
        val pointsToAverage = recentPoints.takeLast(WINDOW_SIZE - 1) + newPoint
        
        var avgLat = 0.0
        var avgLon = 0.0
        
        pointsToAverage.forEach {
            avgLat += it.latitude
            avgLon += it.longitude
        }
        
        avgLat /= pointsToAverage.size
        avgLon /= pointsToAverage.size

        // Return a copy of the new point but with the averaged coordinates
        return newPoint.copy(
            latitude = avgLat,
            longitude = avgLon
        )
    }
}
