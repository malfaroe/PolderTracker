package com.mae.poldertracker.reminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import java.util.Calendar

object ReminderScheduler {

    fun scheduleAll(context: Context, reminders: List<Reminder>) =
        reminders.forEach { schedule(context, it) }

    fun schedule(context: Context, reminder: Reminder) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val trigger = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, reminder.hour)
            set(Calendar.MINUTE, reminder.minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) add(Calendar.DAY_OF_YEAR, 1)
        }
        val pending = buildAlarmIntent(context, reminder)
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !am.canScheduleExactAlarms()) {
                // Permission not granted: inexact fallback (may be delayed by system batching)
                am.set(AlarmManager.RTC_WAKEUP, trigger.timeInMillis, pending)
            } else {
                // Exact alarm, fires in Doze mode (requires SCHEDULE_EXACT_ALARM)
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, trigger.timeInMillis, pending)
            }
        } catch (e: SecurityException) {
            am.set(AlarmManager.RTC_WAKEUP, trigger.timeInMillis, pending)
        }
    }

    fun cancel(context: Context, id: Int) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        am.cancel(buildAlarmIntent(context, Reminder(id, 0, 0)))
    }

    fun cancelAll(context: Context, reminders: List<Reminder>) =
        reminders.forEach { cancel(context, it.id) }

    /** True when the app can schedule exact alarms on this device. */
    fun canScheduleExact(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return true
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        return am.canScheduleExactAlarms()
    }

    private fun buildAlarmIntent(context: Context, reminder: Reminder): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("id", reminder.id)
            putExtra("hour", reminder.hour)
            putExtra("minute", reminder.minute)
        }
        return PendingIntent.getBroadcast(
            context, reminder.id, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
