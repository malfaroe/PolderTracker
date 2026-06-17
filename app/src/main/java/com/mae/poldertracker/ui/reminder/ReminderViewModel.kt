package com.mae.poldertracker.ui.reminder

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import com.mae.poldertracker.reminder.Reminder
import com.mae.poldertracker.reminder.ReminderPrefs
import com.mae.poldertracker.reminder.ReminderReceiver
import com.mae.poldertracker.reminder.ReminderScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class ReminderUiState(
    val reminders: List<Reminder> = emptyList(),
    val showAddDialog: Boolean = false,
    val editingReminder: Reminder? = null,
)

@HiltViewModel
class ReminderViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val prefs = ReminderPrefs(context)
    private val _state = MutableStateFlow(ReminderUiState(reminders = prefs.getReminders()))
    val uiState: StateFlow<ReminderUiState> = _state.asStateFlow()

    fun addReminder(hour: Int, minute: Int) {
        val reminder = Reminder(prefs.nextId(), hour, minute)
        val updated = (prefs.getReminders() + reminder)
            .sortedWith(compareBy({ it.hour }, { it.minute }))
        prefs.saveReminders(updated)
        ReminderScheduler.schedule(context, reminder)
        _state.value = _state.value.copy(reminders = updated, showAddDialog = false)
    }

    fun updateReminder(updated: Reminder) {
        val list = prefs.getReminders().map { if (it.id == updated.id) updated else it }
            .sortedWith(compareBy({ it.hour }, { it.minute }))
        prefs.saveReminders(list)
        ReminderScheduler.cancel(context, updated.id)
        ReminderScheduler.schedule(context, updated)
        _state.value = _state.value.copy(reminders = list, editingReminder = null)
    }

    fun deleteReminder(reminder: Reminder) {
        val list = prefs.getReminders().filter { it.id != reminder.id }
        prefs.saveReminders(list)
        ReminderScheduler.cancel(context, reminder.id)
        _state.value = _state.value.copy(reminders = list)
    }

    /** Fires the notification + sound immediately for testing. */
    fun testNotification() {
        context.sendBroadcast(Intent(context, ReminderReceiver::class.java))
    }

    fun openAddDialog() { _state.value = _state.value.copy(showAddDialog = true) }
    fun dismissAddDialog() { _state.value = _state.value.copy(showAddDialog = false) }
    fun openEditDialog(r: Reminder) { _state.value = _state.value.copy(editingReminder = r) }
    fun dismissEditDialog() { _state.value = _state.value.copy(editingReminder = null) }
}
