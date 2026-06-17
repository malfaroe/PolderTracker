package com.mae.poldertracker.reminder

import android.content.Context
import androidx.core.content.edit

class ReminderPrefs(context: Context) {
    private val prefs = context.getSharedPreferences("reminder_prefs", Context.MODE_PRIVATE)

    var isEnabled: Boolean
        get() = prefs.getBoolean("enabled", false)
        set(v) = prefs.edit { putBoolean("enabled", v) }

    var hourOfDay: Int
        get() = prefs.getInt("hour", 9)
        set(v) = prefs.edit { putInt("hour", v) }

    var minute: Int
        get() = prefs.getInt("minute", 0)
        set(v) = prefs.edit { putInt("minute", v) }
}
