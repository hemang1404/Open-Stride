package com.openstride.data.repository

import com.openstride.data.db.SessionDao
import com.openstride.data.db.TrackPointDao
import com.openstride.data.model.Session
import com.openstride.data.model.TrackPoint
import kotlinx.coroutines.flow.Flow

/**
 * Repository that abstracts access to the local database.
 * This class provides a clean API for the rest of the application to interact with data.
 */
class TrackingRepository(
    private val sessionDao: SessionDao,
    private val trackPointDao: TrackPointDao
) {
    // --- Session Operations ---

    val allSessions: Flow<List<Session>> = sessionDao.getAllSessions()

    suspend fun insertSession(session: Session) {
        sessionDao.insertSession(session)
    }

    suspend fun updateSession(session: Session) {
        sessionDao.updateSession(session)
    }

    suspend fun getSessionById(sessionId: String): Session? {
        return sessionDao.getSessionById(sessionId)
    }

    suspend fun getActiveSession(): Session? {
        return sessionDao.getActiveSession()
    }

    // --- TrackPoint Operations ---

    fun getPointsForSession(sessionId: String): Flow<List<TrackPoint>> {
        return trackPointDao.getPointsForSession(sessionId)
    }

    suspend fun insertTrackPoint(point: TrackPoint) {
        trackPointDao.insertTrackPoint(point)
    }

    suspend fun deleteSessionData(sessionId: String) {
        sessionDao.deleteSession(sessionId)
        trackPointDao.deletePointsForSession(sessionId)
    }
}
