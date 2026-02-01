package com.openstride

import android.content.Context
import com.openstride.data.db.OpenStrideDatabase
import com.openstride.data.repository.TrackingRepository
import com.openstride.engine.GPSOnlyEngine
import com.openstride.engine.TrackingEngine

/**
 * A simple Service Locator to manage application-wide dependencies.
 */
object ServiceLocator {
    
    private var database: OpenStrideDatabase? = null
    private var repository: TrackingRepository? = null
    private var trackingEngine: TrackingEngine? = null

    fun provideRepository(context: Context): TrackingRepository {
        return repository ?: synchronized(this) {
            val database = database ?: OpenStrideDatabase.getDatabase(context)
            this.database = database
            val repo = TrackingRepository(database.sessionDao(), database.trackPointDao())
            repository = repo
            repo
        }
    }

    fun provideTrackingEngine(context: Context): TrackingEngine {
        return trackingEngine ?: synchronized(this) {
            val engine = GPSOnlyEngine(context)
            trackingEngine = engine
            engine
        }
    }
}
