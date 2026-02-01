package com.openstride.util

import com.openstride.data.model.TrackPoint
import kotlin.math.max
import kotlin.math.min

/**
 * Logic for calculating the "Confidence Score" of an activity.
 * Based on GPS accuracy and signal consistency.
 */
object ConfidenceScoring {

    /**
     * Calculates a confidence score (0-100) for a given point.
     */
    fun calculatePointConfidence(point: TrackPoint): Int {
        // Higher accuracy (lower number) = higher score
        // 5m accuracy or better = 100 points
        // 30m accuracy or worse = 0 points
        val accuracyScore = when {
            point.accuracy <= 5f -> 100
            point.accuracy >= 30f -> 0
            else -> 100 - ((point.accuracy - 5) * 4).toInt()
        }

        return max(0, min(100, accuracyScore))
    }

    /**
     * Calculates the overall session confidence.
     */
    fun calculateSessionConfidence(points: List<TrackPoint>): Int {
        if (points.isEmpty()) return 100
        val averagePointConfidence = points.map { calculatePointConfidence(it) }.average().toInt()
        return averagePointConfidence
    }
}
