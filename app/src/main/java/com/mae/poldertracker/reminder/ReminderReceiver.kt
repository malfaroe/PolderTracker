package com.mae.poldertracker.reminder

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.PlaybackParams
import android.os.Build
import androidx.core.app.NotificationCompat
import com.mae.poldertracker.MainActivity
import com.mae.poldertracker.R
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getIntExtra("id", -1)
        val hour = intent.getIntExtra("hour", -1)
        val minute = intent.getIntExtra("minute", -1)
        if (id >= 0 && hour >= 0 && minute >= 0) {
            ReminderScheduler.schedule(context, Reminder(id, hour, minute))
        }

        showNotification(context)

        val async = goAsync()
        Thread {
            try { playSound(context) } catch (_: Exception) { }
            finally { async.finish() }
        }.start()
    }

    private fun showNotification(context: Context) {
        val tapIntent = PendingIntent.getActivity(
            context, 0,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Grounding time, dear barbarian!")
            .setContentText("Jouw moment van rust wacht op je 🐱")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setContentIntent(tapIntent)
            .setAutoCancel(true)
            .setVibrate(longArrayOf(0, 400, 100, 400))

        val nm = context.getSystemService(NotificationManager::class.java)
        val canFsi = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            nm?.canUseFullScreenIntent() == true
        } else true
        if (canFsi) builder.setFullScreenIntent(tapIntent, true)

        nm?.notify(NOTIFICATION_ID, builder.build())
    }

    private fun playSound(context: Context) {
        val latch = CountDownLatch(1)
        val afd = context.resources.openRawResourceFd(R.raw.cat) ?: return
        val mp = MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
            afd.close()
            setOnCompletionListener { latch.countDown() }
            setOnErrorListener { _, _, _ -> latch.countDown(); true }
            prepare()
            // 1/3 octave lower: 2^(-1/3) ≈ 0.7937
            playbackParams = PlaybackParams().apply { pitch = 0.7937f }
            start()
        }
        latch.await(15, TimeUnit.SECONDS)
        mp.release()
    }

    companion object {
        const val CHANNEL_ID = "grounding_v2"
        const val NOTIFICATION_ID = 1001
    }
}
