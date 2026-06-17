package com.mae.poldertracker.ui.reminder

import android.content.Context
import androidx.lifecycle.ViewModel
import com.mae.poldertracker.reminder.ReminderPrefs
import com.mae.poldertracker.reminder.ReminderScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class ReminderUiState(
    val isEnabled: Boolean = false,
    val hour: Int = 9,
    val minute: Int = 0,
)

@HiltViewModel
class ReminderViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val prefs = ReminderPrefs(context)

    private val _uiState = MutableStateFlow(
        ReminderUiState(
            isEnabled = prefs.isEnabled,
            hour = prefs.hourOfDay,
            minute = prefs.minute,
        )
    )
    val uiState: StateFlow<ReminderUiState> = _uiState.asStateFlow()

    fun setEnabled(enabled: Boolean) {
        prefs.isEnabled = enabled
        _uiState.value = _uiState.value.copy(isEnabled = enabled)
        if (enabled) {
            ReminderScheduler.schedule(context, prefs.hourOfDay, prefs.minute)
        } else {
            ReminderScheduler.cancel(context)
        }
    }

    fun setTime(hour: Int, minute: Int) {
        prefs.hourOfDay = hour
        prefs.minute = minute
        _uiState.value = _uiState.value.copy(hour = hour, minute = minute)
        if (prefs.isEnabled) {
            ReminderScheduler.schedule(context, hour, minute)
        }
    }
}
