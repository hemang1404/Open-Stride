package com.openstride.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.openstride.data.model.Session
import com.openstride.data.model.TrackPoint

/**
 * The Room Database for the app.
 * This class ties the Entities and DAOs together.
 */
@Database(
    entities = [Session::class, TrackPoint::class],
    version = 2,
    exportSchema = false
)
abstract class OpenStrideDatabase : RoomDatabase() {

    // These functions tell Room to provide the actual implementation of the DAOs
    abstract fun sessionDao(): SessionDao
    abstract fun trackPointDao(): TrackPointDao

    companion object {
        // @Volatile ensures that the value of INSTANCE is always up-to-date
        // and the same across all CPU cores.
        @Volatile
        private var INSTANCE: OpenStrideDatabase? = null

        /**
         * Returns the single instance of the database.
         * If it doesn't exist, it creates it using the "Singleton" pattern.
         */
        fun getDatabase(context: Context): OpenStrideDatabase {
            // If the INSTANCE is not null, then return it,
            // if it is, then create the database
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    OpenStrideDatabase::class.java,
                    "open_stride_database"
                )
                .fallbackToDestructiveMigration() // Useful during development: clears DB if schema changes
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
