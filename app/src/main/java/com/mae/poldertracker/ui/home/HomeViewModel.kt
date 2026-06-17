package com.mae.poldertracker.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mae.poldertracker.data.local.GroundingSession
import com.mae.poldertracker.data.repository.GroundingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val greeting: String = "",
    val streak: Int = 0,
    val weekSessionCount: Int = 0,
    val weekTotalMinutes: Long = 0,
    val lastSession: GroundingSession? = null,
    val isLoading: Boolean = true
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: GroundingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        observeData()
    }

    private fun observeData() {
        viewModelScope.launch {
            combine(
                repository.lastSession,
                repository.allSessions
            ) { last, _ -> last }.collect {
                refresh()
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            val (weekFrom, weekTo) = repository.currentWeekRange()
            val streak = repository.calculateStreak()
            val weekCount = repository.getSessionCountInRange(weekFrom, weekTo)
            val weekSeconds = repository.getTotalDurationSecondsInRange(weekFrom, weekTo)
            val last = repository.lastSession.first()

            _uiState.update {
                it.copy(
                    streak = streak,
                    weekSessionCount = weekCount,
                    weekTotalMinutes = weekSeconds / 60,
                    lastSession = last,
                    isLoading = false
                )
            }
        }
    }
}
