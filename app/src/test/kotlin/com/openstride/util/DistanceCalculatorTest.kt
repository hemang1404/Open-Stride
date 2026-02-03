package com.openstride.util

import com.openstride.data.model.TrackPoint
import org.junit.Assert.assertEquals
import org.junit.Test

class DistanceCalculatorTest {

    @Test
    fun testHaversineDistance() {
        // Test Haversine (legacy formula, kept for compatibility)
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
        
        assertEquals(0.0, DistanceCalculator.calculateHaversineDistance(
            p1.latitude, p1.longitude, p2.latitude, p2.longitude
        ), 0.01)
    }

    @Test
    fun testVincentyDistance() {
        // Test Vincenty (production formula, more accurate)
        val p1 = TrackPoint(latitude = 51.5007, longitude = -0.1246, accuracy = 1f, timestamp = 0)
        val p2 = TrackPoint(latitude = 51.5007, longitude = -0.1250, accuracy = 1f, timestamp = 0)
        
        val distance = DistanceCalculator.calculateVincentyDistance(
            p1.latitude, p1.longitude,
            p2.latitude, p2.longitude
        )
        
        // Should be slightly different from Haversine (more accurate)
        // Approx 27.8 meters with better precision
        assertEquals(27.8, distance, 0.5)
    }

    @Test
    fun testVincentyZeroDistance() {
        val p1 = TrackPoint(latitude = 0.0, longitude = 0.0, accuracy = 1f, timestamp = 0)
        val p2 = TrackPoint(latitude = 0.0, longitude = 0.0, accuracy = 1f, timestamp = 0)
        
        assertEquals(0.0, DistanceCalculator.calculateVincentyDistance(
            p1.latitude, p1.longitude, p2.latitude, p2.longitude
        ), 0.01)
    }

    @Test
    fun testBearingCalculation() {
        // North
        val bearing1 = DistanceCalculator.calculateBearing(0.0, 0.0, 1.0, 0.0)
        assertEquals(0.0, bearing1, 0.1)
        
        // East
        val bearing2 = DistanceCalculator.calculateBearing(0.0, 0.0, 0.0, 1.0)
        assertEquals(90.0, bearing2, 0.1)
    }

    @Test
    fun testCornerDetection() {
        // 90-degree turn (corner)
        val change1 = DistanceCalculator.calculateBearingChange(
            0.0, 0.0,  // Start
            0.01, 0.0, // Go north
            0.01, 0.01 // Turn east (90 degrees)
        )
        assertEquals(90.0, change1, 1.0)
        
        // Straight line (no corner)
        val change2 = DistanceCalculator.calculateBearingChange(
            0.0, 0.0,
            0.01, 0.0,
            0.02, 0.0
        )
        assertEquals(0.0, change2, 1.0)
    }
}
