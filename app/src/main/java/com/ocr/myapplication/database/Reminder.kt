package com.ocr.myapplication.database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.log

// Data class to represent a Reminder
data class Reminder(
    var id: Long = 0,
    val content: String,
    val icon: Int,
    val startAt: String,  // Timestamp for when the reminder starts
    val endAt: String     // Timestamp for when the reminder ends
)

// Container class to group reminders by time frame
data class ReminderContainer(
    val timeFrame: Char,    // 'T' for Today, 'M' for Tomorrow, 'L' for Later
    val reminders: List<Reminder>
)

// Repository class to handle database operations for Reminders
class ReminderRepository(context: Context) {
    private val dbHelper = DatabaseHelper(context)

    // Insert a new reminder
    fun insertReminder(reminder: Reminder): Long {

        val db = dbHelper.openDatabase()

        Log.d("ReminderDb", "insertReminder: "+reminder.startAt+reminder.endAt)
        val values = ContentValues().apply {
            put("icon", reminder.icon.toLong())
            put("content", reminder.content.toString())
            put("startAt", reminder.startAt.toString())
            put("endAt", reminder.endAt.toString())
        }

        return try {
            val id = db.insert("table_reminder_container", null, values)
            Log.d("ReminderDb", "insertReminder: "+id)
            id
        } catch (e: Exception) {
            Log.e("ReminderRepository", "Error adding reminder", e)
            -1
        } finally {
            dbHelper.close()
        }
    }

    // Get all reminders
    fun getAllReminders(): List<Reminder> {
        val remindersList = mutableListOf<Reminder>()
        val selectQuery = "SELECT * FROM table_reminder_container ORDER BY startAt ASC"

        val db = dbHelper.readableDatabase
        val cursor: Cursor = db.rawQuery(selectQuery, null)

        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getLong(cursor.getColumnIndexOrThrow("id"))
                val content = cursor.getString(cursor.getColumnIndexOrThrow("content"))
                val icon = cursor.getInt(cursor.getColumnIndexOrThrow("icon"))
                val startAt = cursor.getString(cursor.getColumnIndexOrThrow("startAt"))
                val endAt = cursor.getString(cursor.getColumnIndexOrThrow("endAt"))

                Log.d("adsfasdf", "$id---$content---$icon---$startAt---$endAt",)
                val reminder = Reminder(id, content, icon, startAt, endAt)
                remindersList.add(reminder)
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()

        return remindersList
    }

    // Get reminders by time frame (Today, Tomorrow, Later)
    fun getRemindersByTimeFrame(): List<ReminderContainer> {
        val allReminders = getAllReminders()
        val today = Calendar.getInstance()
        val tomorrow = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }

        // Reset time to start of day for comparison
        today.set(Calendar.HOUR_OF_DAY, 0)
        today.set(Calendar.MINUTE, 0)
        today.set(Calendar.SECOND, 0)
        today.set(Calendar.MILLISECOND, 0)

        tomorrow.set(Calendar.HOUR_OF_DAY, 0)
        tomorrow.set(Calendar.MINUTE, 0)
        tomorrow.set(Calendar.SECOND, 0)
        tomorrow.set(Calendar.MILLISECOND, 0)

        val dayAfterTomorrow = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, 2)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // Group reminders by time frame
        val todayReminders = mutableListOf<Reminder>()
        val tomorrowReminders = mutableListOf<Reminder>()
        val laterReminders = mutableListOf<Reminder>()

        for (reminder in allReminders) {
            val reminderStartTime = Calendar.getInstance().apply {
                timeInMillis = reminder.startAt.toLong()
            }

            when {
                reminderStartTime.before(tomorrow) -> todayReminders.add(reminder)
                reminderStartTime.before(dayAfterTomorrow) -> tomorrowReminders.add(reminder)
                else -> laterReminders.add(reminder)
            }
        }

        val containers = mutableListOf<ReminderContainer>()

        // Only add containers that have reminders
        if (todayReminders.isNotEmpty()) {
            containers.add(ReminderContainer('T', todayReminders))
        }

        if (tomorrowReminders.isNotEmpty()) {
            containers.add(ReminderContainer('M', tomorrowReminders))
        }

        if (laterReminders.isNotEmpty()) {
            containers.add(ReminderContainer('L', laterReminders))
        }

        return containers
    }

    // Search reminders by description
    fun searchReminders(query: String): List<Reminder> {
        val remindersList = mutableListOf<Reminder>()
        val searchQuery = "%$query%" // Add wildcards for partial matching

        val db = dbHelper.readableDatabase

        // Create the SQL query to search in description
        val selectQuery = """
            SELECT * FROM table_reminder 
            WHERE description LIKE ? 
            ORDER BY startAt ASC
        """.trimIndent()

        try {
            val cursor = db.rawQuery(selectQuery, arrayOf(searchQuery))

            if (cursor.moveToFirst()) {
                do {
                    val id = cursor.getLong(cursor.getColumnIndexOrThrow("id"))
                    val description = cursor.getString(cursor.getColumnIndexOrThrow("description"))
                    val icon = cursor.getInt(cursor.getColumnIndexOrThrow("icon"))
                    val startAt = cursor.getString(cursor.getColumnIndexOrThrow("startAt"))
                    val endAt = cursor.getString(cursor.getColumnIndexOrThrow("endAt"))

                    val reminder = Reminder(id, description, icon, startAt, endAt)
                    remindersList.add(reminder)
                } while (cursor.moveToNext())
            }

            cursor.close()
        } catch (e: Exception) {
            Log.e("ReminderRepository", "Error searching reminders", e)
        } finally {
            db.close()
        }

        return remindersList
    }

    // Get a single reminder by ID
    fun getReminderById(id: Long): Reminder? {
        val db = dbHelper.readableDatabase
        var reminder: Reminder? = null

        val cursor = db.query(
            "table_reminder",
            null,
            "id = ?",
            arrayOf(id.toString()),
            null,
            null,
            null
        )

        if (cursor.moveToFirst()) {
            val description = cursor.getString(cursor.getColumnIndexOrThrow("description"))
            val icon = cursor.getInt(cursor.getColumnIndexOrThrow("icon"))
            val startAt = cursor.getString(cursor.getColumnIndexOrThrow("startAt"))
            val endAt = cursor.getString(cursor.getColumnIndexOrThrow("endAt"))

            reminder = Reminder(id, description, icon, startAt, endAt)
        }

        cursor.close()
        db.close()

        return reminder
    }

    // Update an existing reminder
    fun updateReminder(reminder: Reminder): Int {
        val db = dbHelper.writableDatabase

        val values = ContentValues().apply {
            put("content", reminder.content)
            put("icon", reminder.icon)
            put("startAt", reminder.startAt)
            put("endAt", reminder.endAt)
        }

        val rowsAffected = db.update(
            "table_reminder_container",
            values,
            "id = ?",
            arrayOf(reminder.id.toString())
        )

        db.close()

        return rowsAffected
    }

    // Delete a reminder
    fun deleteReminder(id: Long): Int {
        val db = dbHelper.writableDatabase

        val rowsAffected = db.delete(
            "table_reminder_container",
            "id = ?",
            arrayOf(id.toString())
        )

        db.close()

        return rowsAffected
    }

    // Close the database connection
    fun closeDatabase() {
        dbHelper.close()
    }
}