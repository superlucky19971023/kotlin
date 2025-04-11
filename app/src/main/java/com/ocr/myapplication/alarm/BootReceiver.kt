package com.ocr.myapplication.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.ocr.myapplication.database.ReminderRepository

/**
 * This receiver is triggered when the device boots up
 * It reschedules all alarms for existing reminders
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {

            val reminderRepository = ReminderRepository(context)
            val alarmHelper = AlarmHelper(context)

            // Get all reminders from the database
            val reminders = reminderRepository.getAllReminders()

            // Schedule alarms for all reminders
            alarmHelper.scheduleAlarmsForAllReminders(reminders)

            Log.d(TAG, "Rescheduled alarms for ${reminders.size} reminders")
        }
    }

    companion object {
        private const val TAG = "BootReceiver"
    }
}