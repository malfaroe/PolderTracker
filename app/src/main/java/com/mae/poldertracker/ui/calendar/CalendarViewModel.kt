package com.mae.poldertracker.ui.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mae.poldertracker.data.local.GroundingSession
import com.mae.poldertracker.data.repository.GroundingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneId
import javax.inject.Inject

data class CalendarUiState(
    val yearMonth: YearMonth = YearMonth.now(),
    val activeDays: Set<Int> = emptySet(),
    val sessions: List<GroundingSession> = emptyList(),
    val selectedSession: GroundingSession? = null
)

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val repository: GroundingRepository
) : ViewModel() {

    private val _yearMonth = MutableStateFlow(YearMonth.now())
    private val _selectedSession = MutableStateFlow<GroundingSession?>(null)

    val uiState: StateFlow<CalendarUiState> = _yearMonth.flatMapLatest { ym ->
        val (from, to) = repository.monthRange(ym.year, ym.monthValue)
        repository.sessionsInRange(from, to).map { sessions ->
            val zone = ZoneId.systemDefault()
            val activeDays = sessions.map { s ->
                Instant.ofEpochMilli(s.startTimestamp).atZone(zone).dayOfMonth
            }.toSet()
            CalendarUiState(
                yearMonth = ym,
                activeDays = activeDays,
                sessions = sessions,
                selectedSession = _selectedSession.value
            )
        }
    }.combine(_selectedSession) { state, selected ->
        state.copy(selectedSession = selected)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CalendarUiState())

    fun previousMonth() = _yearMonth.update { it.minusMonths(1) }

    fun nextMonth() = _yearMonth.update { it.plusMonths(1) }

    fun selectSession(session: GroundingSession?) = _selectedSession.update { session }

    fun deleteSession(session: GroundingSession) {
        viewModelScope.launch {
            repository.delete(session)
            _selectedSession.value = null
        }
    }
}
