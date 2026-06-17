package com.mae.poldertracker.data.repository

import com.mae.poldertracker.data.local.DayDuration
import com.mae.poldertracker.data.local.GroundingSession
import com.mae.poldertracker.data.local.GroundingSessionDao
import kotlinx.coroutines.flow.Flow
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GroundingRepository @Inject constructor(
    private val dao: GroundingSessionDao
) {
    val allSessions: Flow<List<GroundingSession>> = dao.getAllSessions()
    val lastSession: Flow<GroundingSession?> = dao.getLastSession()

    fun sessionsInRange(from: Long, to: Long): Flow<List<GroundingSession>> =
        dao.getSessionsInRange(from, to)

    suspend fun insert(session: GroundingSession): Long = dao.insert(session)

    suspend fun update(session: GroundingSession) = dao.update(session)

    suspend fun delete(session: GroundingSession) = dao.delete(session)

    suspend fun getById(id: Long): GroundingSession? = dao.getSessionById(id)

    suspend fun calculateStreak(): Int {
        val days = dao.getDistinctSessionDays()
        if (days.isEmpty()) return 0

        val zone = ZoneId.systemDefault()
        val today = LocalDate.now(zone)
        val todayEpochDay = today.toEpochDay()
        val yesterdayEpochDay = today.minusDays(1).toEpochDay()

        val daySet = days.toHashSet()

        // La racha puede empezar desde hoy o desde ayer
        val startDay = when {
            daySet.contains(todayEpochDay) -> todayEpochDay
            daySet.contains(yesterdayEpochDay) -> yesterdayEpochDay
            else -> return 0
        }

        var streak = 0
        var current = startDay
        while (daySet.contains(current)) {
            streak++
            current--
        }
        return streak
    }

    suspend fun getMaxStreak(): Int {
        val days = dao.getDistinctSessionDays().sortedDescending()
        if (days.isEmpty()) return 0
        var maxStreak = 1
        var currentStreak = 1
        for (i in 1 until days.size) {
            if (days[i - 1] - days[i] == 1L) {
                currentStreak++
                if (currentStreak > maxStreak) maxStreak = currentStreak
            } else {
                currentStreak = 1
            }
        }
        return maxStreak
    }

    fun currentWeekRange(): Pair<Long, Long> {
        val zone = ZoneId.systemDefault()
        val today = LocalDate.now(zone)
        val monday = today.with(DayOfWeek.MONDAY)
        val from = monday.atStartOfDay(zone).toInstant().toEpochMilli()
        val to = monday.plusDays(7).atStartOfDay(zone).toInstant().toEpochMilli()
        return from to to
    }

    fun currentMonthRange(): Pair<Long, Long> {
        val zone = ZoneId.systemDefault()
        val today = LocalDate.now(zone)
        val firstDay = today.withDayOfMonth(1)
        val from = firstDay.atStartOfDay(zone).toInstant().toEpochMilli()
        val to = firstDay.plusMonths(1).atStartOfDay(zone).toInstant().toEpochMilli()
        return from to to
    }

    fun monthRange(year: Int, month: Int): Pair<Long, Long> {
        val zone = ZoneId.systemDefault()
        val firstDay = LocalDate.of(year, month, 1)
        val from = firstDay.atStartOfDay(zone).toInstant().toEpochMilli()
        val to = firstDay.plusMonths(1).atStartOfDay(zone).toInstant().toEpochMilli()
        return from to to
    }

    suspend fun getSessionCountInRange(from: Long, to: Long): Int =
        dao.getSessionCountInRange(from, to)

    suspend fun getTotalDurationSecondsInRange(from: Long, to: Long): Long =
        dao.getTotalDurationSecondsInRange(from, to)

    suspend fun getAvgRatingInRange(from: Long, to: Long): Float =
        dao.getAvgRatingInRange(from, to)

    suspend fun getAvgDurationSecondsInRange(from: Long, to: Long): Float =
        dao.getAvgDurationSecondsInRange(from, to)

    suspend fun getDailyDurationsInRange(from: Long, to: Long): List<DayDuration> =
        dao.getDailyDurationsInRange(from, to)
}
