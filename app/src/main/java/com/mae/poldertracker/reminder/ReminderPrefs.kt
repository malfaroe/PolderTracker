package com.mae.poldertracker.reminder

import android.content.Context
import androidx.core.content.edit

class ReminderPrefs(context: Context) {
    private val prefs = context.getSharedPreferences("reminder_prefs", Context.MODE_PRIVATE)

    fun getReminders(): List<Reminder> =
        prefs.getStringSet("reminders", emptySet())
            ?.mapNotNull(::decode)
            ?.sortedWith(compareBy({ it.hour }, { it.minute }))
            ?: emptyList()

    fun saveReminders(reminders: List<Reminder>) =
        prefs.edit { putStringSet("reminders", reminders.map(::encode).toSet()) }

    fun nextId(): Int {
        val id = prefs.getInt("next_id", 1)
        prefs.edit { putInt("next_id", id + 1) }
        return id
    }

    private fun encode(r: Reminder) = "${r.id}:${r.hour}:${r.minute}"
    private fun decode(s: String): Reminder? {
        val p = s.split(":")
        return if (p.size == 3) runCatching {
            Reminder(p[0].toInt(), p[1].toInt(), p[2].toInt())
        }.getOrNull() else null
    }
}
