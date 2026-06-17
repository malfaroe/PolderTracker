package com.mae.poldertracker.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [GroundingSession::class],
    version = 1,
    exportSchema = false
)
abstract class GroundingDatabase : RoomDatabase() {
    abstract fun sessionDao(): GroundingSessionDao
}
