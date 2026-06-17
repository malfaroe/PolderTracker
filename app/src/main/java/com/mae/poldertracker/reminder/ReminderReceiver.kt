package com.mae.poldertracker.reminder

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import androidx.core.app.NotificationCompat
import com.mae.poldertracker.MainActivity
import com.mae.poldertracker.R
import kotlin.math.PI
import kotlin.math.sin

class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        showNotification(context)
        val pending = goAsync()
        Thread {
            try { playMeow() } finally { pending.finish() }
        }.start()
    }

    private fun showNotification(context: Context) {
        val tapIntent = PendingIntent.getActivity(
            context, 0,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Hora de tu Grounding")
            .setContentText("Tu momento de calma te espera 🐱")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(tapIntent)
            .setAutoCancel(true)
            .build()

        context.getSystemService(NotificationManager::class.java)
            ?.notify(NOTIFICATION_ID, notification)
    }

    private fun playMeow() {
        val sampleRate = 22050
        val durationSec = 0.75
        val n = (sampleRate * durationSec).toInt()
        val buf = ShortArray(n)

        for (i in 0 until n) {
            val t = i.toDouble() / sampleRate
            val p = t / durationSec

            // Frequency contour: rise then fall (cat meow shape)
            val f0 = when {
                p < 0.45 -> 700.0 + 350.0 * (p / 0.45)
                else     -> 1050.0 - 280.0 * ((p - 0.45) / 0.55)
            }
            // Vibrato 5 Hz, depth 1.5 %
            val fv = f0 * (1.0 + 0.015 * sin(2 * PI * 5.0 * t))

            // Waveform with harmonics for vocal timbre
            val wave = 0.65 * sin(2 * PI * fv * t) +
                       0.25 * sin(2 * PI * 2 * fv * t) +
                       0.10 * sin(2 * PI * 3 * fv * t)

            // Amplitude envelope
            val env = when {
                p < 0.06 -> p / 0.06
                p > 0.80 -> (1.0 - p) / 0.20
                else     -> 1.0
            }

            buf[i] = (0.78 * Short.MAX_VALUE * wave * env).toInt()
                .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }

        val track = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(sampleRate)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setTransferMode(AudioTrack.MODE_STATIC)
            .setBufferSizeInBytes(buf.size * 2)
            .build()

        track.write(buf, 0, buf.size)
        track.play()
        Thread.sleep(800)
        track.stop()
        track.release()
    }

    companion object {
        const val CHANNEL_ID = "grounding_reminder"
        const val NOTIFICATION_ID = 1001
    }
}
