package com.mae.poldertracker.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mae.poldertracker.data.local.DayDuration
import com.mae.poldertracker.data.repository.GroundingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

enum class StatsPeriod { WEEK, MONTH }

data class StatsUiState(
    val period: StatsPeriod = StatsPeriod.WEEK,
    val barData: List<BarEntry> = emptyList(),
    val sessionCount: Int = 0,
    val avgDurationMinutes: Float = 0f,
    val avgRating: Float = 0f,
    val maxStreak: Int = 0,
    val ratingTrend: Float = 0f  // positive = up, negative = down
)

data class BarEntry(val label: String, val minutes: Float)

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val repository: GroundingRepository
) : ViewModel() {

    private val _period = MutableStateFlow(StatsPeriod.WEEK)

    val uiState: StateFlow<StatsUiState> = _period.flatMapLatest { period ->
        flow {
            val zone = ZoneId.systemDefault()
            val today = LocalDate.now(zone)

            val (from, to) = if (period == StatsPeriod.WEEK) {
                repository.currentWeekRange()
            } else {
                repository.currentMonthRange()
            }

            val (prevFrom, prevTo) = if (period == StatsPeriod.WEEK) {
                val monday = today.with(DayOfWeek.MONDAY).minusWeeks(1)
                val f = monday.atStartOfDay(zone).toInstant().toEpochMilli()
                val t = monday.plusDays(7).atStartOfDay(zone).toInstant().toEpochMilli()
                f to t
            } else {
                val first = today.withDayOfMonth(1).minusMonths(1)
                val f = first.atStartOfDay(zone).toInstant().toEpochMilli()
                val t = first.plusMonths(1).atStartOfDay(zone).toInstant().toEpochMilli()
                f to t
            }

            val dailyDurations = repository.getDailyDurationsInRange(from, to)
            val count = repository.getSessionCountInRange(from, to)
            val avgDurSec = repository.getAvgDurationSecondsInRange(from, to)
            val avgRating = repository.getAvgRatingInRange(from, to)
            val maxStreak = repository.getMaxStreak()
            val prevAvgRating = repository.getAvgRatingInRange(prevFrom, prevTo)

            val barData = buildBarData(period, from, to, dailyDurations, zone)

            emit(
                StatsUiState(
                    period = period,
                    barData = barData,
                    sessionCount = count,
                    avgDurationMinutes = avgDurSec / 60f,
                    avgRating = avgRating,
                    maxStreak = maxStreak,
                    ratingTrend = avgRating - prevAvgRating
                )
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), StatsUiState())

    fun setPeriod(period: StatsPeriod) = _period.update { period }

    private fun buildBarData(
        period: StatsPeriod,
        from: Long,
        to: Long,
        durations: List<DayDuration>,
        zone: ZoneId
    ): List<BarEntry> {
        val durationMap = durations.associate { it.epochDay to it.totalSeconds }

        return if (period == StatsPeriod.WEEK) {
            val monday = java.time.Instant.ofEpochMilli(from).atZone(zone).toLocalDate()
            val labels = listOf("M", "D", "W", "D", "V", "Z", "Z") // Ma Di Wo Do Vr Za Zo
            (0..6).map { i ->
                val date = monday.plusDays(i.toLong())
                val epochDay = date.toEpochDay()
                BarEntry(
                    label = labels[i],
                    minutes = (durationMap[epochDay] ?: 0L) / 60f
                )
            }
        } else {
            // Group by ISO week within the month
            val firstDay = java.time.Instant.ofEpochMilli(from).atZone(zone).toLocalDate()
            val lastDay = java.time.Instant.ofEpochMilli(to - 1).atZone(zone).toLocalDate()
            val weekMap = mutableMapOf<Int, Float>()
            var day = firstDay
            while (!day.isAfter(lastDay)) {
                val epochDay = day.toEpochDay()
                val weekOfMonth = ((day.dayOfMonth - 1) / 7) + 1
                weekMap[weekOfMonth] = (weekMap[weekOfMonth] ?: 0f) + ((durationMap[epochDay] ?: 0L) / 60f)
                day = day.plusDays(1)
            }
            weekMap.entries.sortedBy { it.key }.map { (week, minutes) ->
                BarEntry(label = "W$week", minutes = minutes)
            }
        }
    }
}
