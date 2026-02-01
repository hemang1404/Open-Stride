package com.openstride.util

import com.openstride.data.model.TrackPoint
import org.junit.Assert.assertEquals
import org.junit.Test

class LocationSmootherTest {

    @Test
    fun testSmoothingAveragesPoints() {
        val p1 = TrackPoint(latitude = 10.0, longitude = 10.0, accuracy = 1f, timestamp = 1)
        val p2 = TrackPoint(latitude = 11.0, longitude = 11.0, accuracy = 1f, timestamp = 2)
        val p3 = TrackPoint(latitude = 12.0, longitude = 12.0, accuracy = 1f, timestamp = 3)
        
        val smoothed = LocationSmoother.smooth(p3, listOf(p1, p2))
        
        // Average of (10, 11, 12) = 11
        assertEquals(11.0, smoothed.latitude, 0.01)
        assertEquals(11.0, smoothed.longitude, 0.01)
    }
}
