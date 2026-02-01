package com.openstride.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents a completed or active walking session.
 * 
 * @property sessionId Unique identifier for the session (usually a UUID).
 * @property startTime Start timestamp in milliseconds.
 * @property endTime End timestamp in milliseconds (null if session is active).
 * @property totalDistance Accumulated distance in meters.
 * @property mode Tracking mode used (e.g., GPS_ONLY).
 * @property confidenceScore Overall confidence score (0-100).
 */
@Entity(tableName = "sessions")
data class Session(
    @PrimaryKey
    val sessionId: String,
    val startTime: Long,
    val endTime: Long? = null,
    val totalDistance: Double = 0.0,
    val mode: String, // Can use an Enum with TypeConverter later
    val confidenceScore: Int = 0,
    val isPaused: Boolean = false,
    val elapsedTimeSeconds: Long = 0, // Actual elapsed time excluding pauses
    val lastPauseTime: Long? = null // When the session was last paused
)
