package com.openstride.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents a single coordinate captured during a walking session.
 * 
 * @property id Unique identifier for the room database.
 * @property sessionId The ID of the session this point belongs to.
 * @property latitude GPS Latitude.
 * @property longitude GPS Longitude.
 * @property timestamp Time when the point was recorded (in milliseconds).
 * @property accuracy Estimated horizontal accuracy in meters.
 * @property speed Speed in meters/second at this point (can be null if unknown).
 */
@Entity(tableName = "track_points")
data class TrackPoint(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sessionId: String,
    val latitude: Double,
    val longitude: Double,
    val timestamp: Long,
    val accuracy: Float,
    val speed: Float?
)
