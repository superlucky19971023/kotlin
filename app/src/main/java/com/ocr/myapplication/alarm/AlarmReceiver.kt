package com.ocr.myapplication.alarm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.ocr.myapplication.MainActivity
import com.ocr.myapplication.R

class AlarmReceiver : BroadcastReceiver() {
    private var mediaPlayer: MediaPlayer? = null

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "Alarm received!")

        val reminderId = intent.getLongExtra(EXTRA_REMINDER_ID, -1)
        val reminderContent = intent.getStringExtra(EXTRA_REMINDER_CONTENT) ?: "Reminder"

        // Play alarm sound
        playAlarmSound(context)

        // Show notification
        showNotification(context, reminderId, reminderContent)
    }

    private fun playAlarmSound(context: Context) {
        try {
            // Release any existing MediaPlayer
            mediaPlayer?.release()

            // Create and prepare a new MediaPlayer with the alarm sound
            mediaPlayer = MediaPlayer.create(context, R.raw.alarm)
            mediaPlayer?.apply {
                isLooping = false
                start()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error playing alarm sound", e)
        }
    }

    private fun showNotification(context: Context, reminderId: Long, content: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create notification channel for Android O and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Reminder Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for upcoming reminders"

                // Set sound for the channel
                val audioAttributes = AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .build()
                setSound(Uri.parse("android.resource://${context.packageName}/${R.raw.alarm}"), audioAttributes)
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Create intent to open the app when notification is tapped
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(EXTRA_REMINDER_ID, reminderId)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            reminderId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Build the notification
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.icon_1)
            .setContentTitle("Reminder Alert")
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        // Show the notification
        notificationManager.notify(reminderId.toInt(), notification)
    }

    companion object {
        private const val TAG = "AlarmReceiver"
        const val CHANNEL_ID = "reminder_channel"
        const val EXTRA_REMINDER_ID = "reminder_id"
        const val EXTRA_REMINDER_CONTENT = "reminder_content"
    }
}