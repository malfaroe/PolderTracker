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
            "Grounding herinnering",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Dagelijkse herinnering voor je Grounding sessie"
            enableVibration(true)
            vibrationPattern = longArrayOf(0, 400, 100, 400)
            // No default sound: the meow is played via MediaPlayer in the receiver
            setSound(null, null)
        }
        getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
    }
}
