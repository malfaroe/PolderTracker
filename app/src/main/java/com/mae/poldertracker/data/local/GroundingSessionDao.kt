package com.mae.poldertracker.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface GroundingSessionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(session: GroundingSession): Long

    @Update
    suspend fun update(session: GroundingSession)

    @Delete
    suspend fun delete(session: GroundingSession)

    @Query("SELECT * FROM grounding_sessions ORDER BY startTimestamp DESC")
    fun getAllSessions(): Flow<List<GroundingSession>>

    @Query("SELECT * FROM grounding_sessions WHERE startTimestamp >= :from AND startTimestamp < :to ORDER BY startTimestamp DESC")
    fun getSessionsInRange(from: Long, to: Long): Flow<List<GroundingSession>>

    @Query("SELECT * FROM grounding_sessions WHERE id = :id")
    suspend fun getSessionById(id: Long): GroundingSession?

    @Query("SELECT * FROM grounding_sessions ORDER BY startTimestamp DESC LIMIT 1")
    fun getLastSession(): Flow<GroundingSession?>

    // Días distintos con al menos una sesión (como epoch day)
    @Query("""
        SELECT DISTINCT CAST(startTimestamp / 86400000 AS INTEGER) AS epochDay
        FROM grounding_sessions
        ORDER BY epochDay DESC
    """)
    suspend fun getDistinctSessionDays(): List<Long>

    // Suma de duración en segundos en un rango de fechas
    @Query("""
        SELECT COALESCE(SUM(durationSeconds), 0)
        FROM grounding_sessions
        WHERE startTimestamp >= :from AND startTimestamp < :to
    """)
    suspend fun getTotalDurationSecondsInRange(from: Long, to: Long): Long

    // Conteo de sesiones en un rango
    @Query("""
        SELECT COUNT(*) FROM grounding_sessions
        WHERE startTimestamp >= :from AND startTimestamp < :to
    """)
    suspend fun getSessionCountInRange(from: Long, to: Long): Int

    // Promedio de feelingRating en un rango
    @Query("""
        SELECT COALESCE(AVG(CAST(feelingRating AS REAL)), 0)
        FROM grounding_sessions
        WHERE startTimestamp >= :from AND startTimestamp < :to
    """)
    suspend fun getAvgRatingInRange(from: Long, to: Long): Float

    // Duración promedio por sesión en un rango (en segundos)
    @Query("""
        SELECT COALESCE(AVG(CAST(durationSeconds AS REAL)), 0)
        FROM grounding_sessions
        WHERE startTimestamp >= :from AND startTimestamp < :to
    """)
    suspend fun getAvgDurationSecondsInRange(from: Long, to: Long): Float

    // Datos diarios de duración en un rango (para gráficos)
    @Query("""
        SELECT CAST(startTimestamp / 86400000 AS INTEGER) AS epochDay,
               SUM(durationSeconds) AS totalSeconds
        FROM grounding_sessions
        WHERE startTimestamp >= :from AND startTimestamp < :to
        GROUP BY epochDay
        ORDER BY epochDay ASC
    """)
    suspend fun getDailyDurationsInRange(from: Long, to: Long): List<DayDuration>
}

data class DayDuration(
    val epochDay: Long,
    val totalSeconds: Long
)
