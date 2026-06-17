package com.mae.poldertracker.ui.session

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mae.poldertracker.data.local.GroundingSession
import com.mae.poldertracker.data.repository.GroundingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CloseSessionUiState(
    val durationSeconds: Int = 0,
    val startTimestamp: Long = 0L,
    val feelingRating: Int = 0,
    val notes: String = "",
    val isSaved: Boolean = false
)

@HiltViewModel
class CloseSessionViewModel @Inject constructor(
    private val repository: GroundingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CloseSessionUiState())
    val uiState: StateFlow<CloseSessionUiState> = _uiState.asStateFlow()

    fun init(durationSeconds: Int, startTimestamp: Long) {
        _uiState.update { it.copy(durationSeconds = durationSeconds, startTimestamp = startTimestamp) }
    }

    fun setRating(rating: Int) = _uiState.update { it.copy(feelingRating = rating) }

    fun setNotes(notes: String) = _uiState.update { it.copy(notes = notes) }

    fun save() {
        val state = _uiState.value
        if (state.feelingRating == 0) return
        viewModelScope.launch {
            repository.insert(
                GroundingSession(
                    startTimestamp = state.startTimestamp,
                    durationSeconds = state.durationSeconds,
                    feelingRating = state.feelingRating,
                    notes = state.notes.ifBlank { null }
                )
            )
            _uiState.update { it.copy(isSaved = true) }
        }
    }
}
