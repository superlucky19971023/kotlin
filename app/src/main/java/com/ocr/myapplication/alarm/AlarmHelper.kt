package com.ocr.myapplication.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.ocr.myapplication.database.Reminder
import java.util.Calendar

class AlarmHelper(private val context: Context) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    /**
     * Schedule an alarm for the reminder
     * The alarm will trigger 10 minutes before the reminder's start time
     */
    fun scheduleAlarm(reminder: Reminder) {
        try {
            val reminderId = reminder.id
            if (reminderId == 0L) {
                Log.e(TAG, "Cannot schedule alarm for reminder with ID 0")
                return
            }

            // Parse the start time
            val startTimeMillis = reminder.startAt.toLong()

            // Calculate the alarm time (10 minutes before start time)
            val alarmTimeMillis = startTimeMillis - (10 * 60 * 1000) // 10 minutes in milliseconds

            // Only schedule if the alarm time is in the future
            val currentTimeMillis = System.currentTimeMillis()
            if (alarmTimeMillis <= currentTimeMillis) {
                Log.d(TAG, "Not scheduling alarm for reminder $reminderId as the time has already passed")
                return
            }

            // Create the intent for the alarm
            val intent = Intent(context, AlarmReceiver::class.java).apply {
                putExtra(AlarmReceiver.EXTRA_REMINDER_ID, reminderId)
                putExtra(AlarmReceiver.EXTRA_REMINDER_CONTENT, reminder.content)
            }

            // Create a unique request code based on the reminder ID
            val requestCode = reminderId.toInt()

            // Create the pending intent
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // Schedule the alarm
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    alarmTimeMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    alarmTimeMillis,
                    pendingIntent
                )
            }

            Log.d(TAG, "Alarm scheduled for reminder $reminderId at ${formatTime(alarmTimeMillis)}")
        } catch (e: Exception) {
            Log.e(TAG, "Error scheduling alarm", e)
        }
    }

    /**
     * Cancel the alarm for the reminder
     */
    fun cancelAlarm(reminderId: Long) {
        try {
            val intent = Intent(context, AlarmReceiver::class.java)
            val requestCode = reminderId.toInt()

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // Cancel the alarm
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()

            Log.d(TAG, "Alarm canceled for reminder $reminderId")
        } catch (e: Exception) {
            Log.e(TAG, "Error canceling alarm", e)
        }
    }

    /**
     * Schedule alarms for all reminders
     */
    fun scheduleAlarmsForAllReminders(reminders: List<Reminder>) {
        for (reminder in reminders) {
            // Only schedule alarms for future reminders
            val startTimeMillis = reminder.startAt.toLong()
            val currentTimeMillis = System.currentTimeMillis()
            val alarmTimeMillis = startTimeMillis - (10 * 60 * 1000) // 10 minutes before

            if (alarmTimeMillis > currentTimeMillis) {
                scheduleAlarm(reminder)
            }
        }
    }

    /**
     * Format time for logging
     */
    private fun formatTime(timeMillis: Long): String {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = timeMillis
        }

        return "${calendar.get(Calendar.MONTH) + 1}/${calendar.get(Calendar.DAY_OF_MONTH)}/${calendar.get(Calendar.YEAR)} " +
                "${calendar.get(Calendar.HOUR_OF_DAY)}:${calendar.get(Calendar.MINUTE)}"
    }

    companion object {
        private const val TAG = "AlarmHelper"
    }
}