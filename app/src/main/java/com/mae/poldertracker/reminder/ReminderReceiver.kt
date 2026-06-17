package com.mae.poldertracker.reminder

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import androidx.core.app.NotificationCompat
import com.mae.poldertracker.MainActivity
import com.mae.poldertracker.R
import java.io.File
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.math.PI
import kotlin.math.sin

class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        showNotification(context)
        val pending = goAsync()
        Thread {
            try { playMeow(context) } catch (_: Exception) { }
            finally { pending.finish() }
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
            .setContentTitle("Grounding time, dear barbarian!")
            .setContentText("Tu momento de calma te espera 🐱")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(tapIntent)
            .setAutoCancel(true)
            .build()

        context.getSystemService(NotificationManager::class.java)
            ?.notify(NOTIFICATION_ID, notification)
    }

    private fun playMeow(context: Context) {
        val samples = synthesizeMeow()
        val wav = encodeWav(samples, SAMPLE_RATE)
        val file = File(context.cacheDir, "meow_${System.currentTimeMillis()}.wav")
        file.writeBytes(wav)
        try {
            val latch = CountDownLatch(1)
            val mp = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION_EVENT)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                setDataSource(file.absolutePath)
                setOnCompletionListener { latch.countDown() }
                prepare()
                start()
            }
            latch.await(3, TimeUnit.SECONDS)
            mp.release()
        } finally {
            file.delete()
        }
    }

    // WAV file builder (PCM 16-bit mono little-endian)
    private fun encodeWav(samples: ShortArray, sampleRate: Int): ByteArray {
        val dataBytes = samples.size * 2
        return ByteArray(44 + dataBytes).also { b ->
            b.str(0, "RIFF"); b.i32(4, dataBytes + 36); b.str(8, "WAVE")
            b.str(12, "fmt "); b.i32(16, 16); b.i16(20, 1); b.i16(22, 1)
            b.i32(24, sampleRate); b.i32(28, sampleRate * 2); b.i16(32, 2); b.i16(34, 16)
            b.str(36, "data"); b.i32(40, dataBytes)
            for (i in samples.indices) {
                val s = samples[i].toInt()
                b[44 + i * 2] = (s and 0xFF).toByte()
                b[44 + i * 2 + 1] = ((s shr 8) and 0xFF).toByte()
            }
        }
    }

    private fun ByteArray.str(o: Int, s: String) = s.forEachIndexed { i, c -> this[o + i] = c.code.toByte() }
    private fun ByteArray.i32(o: Int, v: Int) { this[o]=(v and 0xFF).toByte(); this[o+1]=((v shr 8) and 0xFF).toByte(); this[o+2]=((v shr 16) and 0xFF).toByte(); this[o+3]=((v shr 24) and 0xFF).toByte() }
    private fun ByteArray.i16(o: Int, v: Int) { this[o]=(v and 0xFF).toByte(); this[o+1]=((v shr 8) and 0xFF).toByte() }

    private fun synthesizeMeow(): ShortArray {
        val n = (SAMPLE_RATE * 0.75).toInt()
        return ShortArray(n) { i ->
            val t = i.toDouble() / SAMPLE_RATE
            val p = t / 0.75
            val f0 = if (p < 0.45) 700.0 + 350.0 * (p / 0.45) else 1050.0 - 280.0 * ((p - 0.45) / 0.55)
            val fv = f0 * (1.0 + 0.015 * sin(2 * PI * 5.0 * t))
            val wave = 0.65 * sin(2 * PI * fv * t) + 0.25 * sin(4 * PI * fv * t) + 0.10 * sin(6 * PI * fv * t)
            val env = when { p < 0.06 -> p / 0.06; p > 0.80 -> (1.0 - p) / 0.20; else -> 1.0 }
            (0.78 * Short.MAX_VALUE * wave * env).toInt()
                .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
    }

    companion object {
        const val CHANNEL_ID = "grounding_reminder"
        const val NOTIFICATION_ID = 1001
        private const val SAMPLE_RATE = 22050
    }
}
