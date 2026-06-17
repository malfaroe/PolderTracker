package com.mae.poldertracker.ui.session

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class SessionUiState(
    val startEpochMillis: Long = 0L,
    val pausedAccumulatedSeconds: Int = 0,
    val pauseStartEpochMillis: Long = 0L,
    val isRunning: Boolean = false,
    val isStarted: Boolean = false
)

@HiltViewModel
class SessionViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(SessionUiState())
    val uiState: StateFlow<SessionUiState> = _uiState.asStateFlow()

    fun start() {
        val now = System.currentTimeMillis()
        _uiState.update {
            it.copy(
                startEpochMillis = now,
                pausedAccumulatedSeconds = 0,
                pauseStartEpochMillis = 0L,
                isRunning = true,
                isStarted = true
            )
        }
    }

    fun pause() {
        val now = System.currentTimeMillis()
        _uiState.update {
            it.copy(isRunning = false, pauseStartEpochMillis = now)
        }
    }

    fun resume() {
        val now = System.currentTimeMillis()
        _uiState.update {
            val pausedSeconds = ((now - it.pauseStartEpochMillis) / 1000).toInt()
            it.copy(
                isRunning = true,
                pausedAccumulatedSeconds = it.pausedAccumulatedSeconds + pausedSeconds,
                pauseStartEpochMillis = 0L
            )
        }
    }

    fun reset() {
        _uiState.value = SessionUiState()
    }

    // Returns total elapsed seconds excluding paused time, up to 'now'
    fun computeElapsedSeconds(nowMillis: Long): Int {
        val state = _uiState.value
        if (!state.isStarted) return 0
        val rawElapsed = ((nowMillis - state.startEpochMillis) / 1000).toInt()
        val pauseOngoing = if (!state.isRunning && state.pauseStartEpochMillis > 0) {
            ((nowMillis - state.pauseStartEpochMillis) / 1000).toInt()
        } else 0
        return (rawElapsed - state.pausedAccumulatedSeconds - pauseOngoing).coerceAtLeast(0)
    }
}
