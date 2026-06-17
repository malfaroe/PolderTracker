package com.mae.poldertracker.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "grounding_sessions")
data class GroundingSession(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val startTimestamp: Long,
    val durationSeconds: Int,
    val feelingRating: Int,
    val notes: String? = null
)
