package com.mae.poldertracker

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import com.mae.poldertracker.reminder.ReminderReceiver
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class PolderTrackerApp : Application() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            ReminderReceiver.CHANNEL_ID,
            "Recordatorio de Grounding",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Recordatorio diario para tu sesión de Grounding"
        }
        getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
    }
}
