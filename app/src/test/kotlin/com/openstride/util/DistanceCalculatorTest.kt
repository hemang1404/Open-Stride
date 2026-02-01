package com.openstride.util

import com.openstride.data.model.TrackPoint
import org.junit.Assert.assertEquals
import org.junit.Test

class DistanceCalculatorTest {

    @Test
    fun testHaversineDistance() {
        // Distance between two points in London
        val p1 = TrackPoint(latitude = 51.5007, longitude = -0.1246, accuracy = 1f, timestamp = 0)
        val p2 = TrackPoint(latitude = 51.5007, longitude = -0.1250, accuracy = 1f, timestamp = 0)
        
        val distance = DistanceCalculator.calculateHaversineDistance(
            p1.latitude, p1.longitude,
            p2.latitude, p2.longitude
        )
        
        // Approx 27.8 meters
        assertEquals(27.8, distance, 0.5)
    }

    @Test
    fun testZeroDistance() {
        val p1 = TrackPoint(latitude = 0.0, longitude = 0.0, accuracy = 1f, timestamp = 0)
        val p2 = TrackPoint(latitude = 0.0, longitude = 0.0, accuracy = 1f, timestamp = 0)
        
        assertEquals(0.0, DistanceCalculator.calculateDistance(p1, p2), 0.01)
    }
}
