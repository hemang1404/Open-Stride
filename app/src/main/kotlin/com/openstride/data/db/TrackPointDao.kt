package com.openstride.data.db

import androidx.room.*
import com.openstride.data.model.TrackPoint
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) for the track_points table.
 */
@Dao
interface TrackPointDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrackPoint(point: TrackPoint)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrackPoints(points: List<TrackPoint>)

    @Query("SELECT * FROM track_points WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    fun getPointsForSession(sessionId: String): Flow<List<TrackPoint>>

    @Query("SELECT * FROM track_points WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    suspend fun getPointsForSessionOnce(sessionId: String): List<TrackPoint>

    @Query("DELETE FROM track_points WHERE sessionId = :sessionId")
    suspend fun deletePointsForSession(sessionId: String)

    @Query("SELECT COUNT(*) FROM track_points WHERE sessionId = :sessionId")
    suspend fun getPointCountForSession(sessionId: String): Int
}
