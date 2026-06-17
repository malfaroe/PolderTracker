package com.mae.poldertracker.reminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
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
        am.setRepeating(
            AlarmManager.RTC_WAKEUP,
            trigger.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            buildPendingIntent(context, reminder.id)
        )
    }

    fun cancel(context: Context, id: Int) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        am.cancel(buildPendingIntent(context, id))
    }

    fun cancelAll(context: Context, reminders: List<Reminder>) =
        reminders.forEach { cancel(context, it.id) }

    private fun buildPendingIntent(context: Context, id: Int): PendingIntent =
        PendingIntent.getBroadcast(
            context, id,
            Intent(context, ReminderReceiver::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
}
