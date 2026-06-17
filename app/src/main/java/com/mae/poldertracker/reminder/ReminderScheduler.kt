package com.mae.poldertracker.reminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.mae.poldertracker.MainActivity
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
        // setAlarmClock: always exact, fires in Doze mode, needs no SCHEDULE_EXACT_ALARM permission
        val showIntent = PendingIntent.getActivity(
            context, reminder.id,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        am.setAlarmClock(
            AlarmManager.AlarmClockInfo(trigger.timeInMillis, showIntent),
            buildAlarmIntent(context, reminder)
        )
    }

    fun cancel(context: Context, id: Int) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        am.cancel(buildAlarmIntent(context, Reminder(id, 0, 0)))
    }

    fun cancelAll(context: Context, reminders: List<Reminder>) =
        reminders.forEach { cancel(context, it.id) }

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
