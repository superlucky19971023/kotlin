package com.ocr.myapplication.database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import java.util.*

// Data class to represent a Note
data class Note(
    var id: Long = 0,
    val topic: String,
    val description: String,
    val icon: Int,
    val createdAt: String
)

// Repository class to handle database operations
class NoteRepository(context: Context) {
    private val dbHelper =DatabaseHelper(context)

    // Insert a new note
    fun insertNote(note: Note): Long {
        val db = dbHelper.openDatabase()

        val values = ContentValues().apply {
            put("icon", note.icon)
            put("topic", note.topic)
            put("description", note.description)
            put("createdAt", System.currentTimeMillis().toString())
        }

        return try {
            val id = db.insert("table_note", null, values)
            id
        } catch (e: Exception) {
            Log.e("UserRepository", "Error adding user", e)
            -1
        } finally {
            dbHelper.close()
        }
    }

    // Get all notes
    fun getAllNotes(): List<Note> {
        val notesList = mutableListOf<Note>()
        val selectQuery = "SELECT * FROM table_note ORDER BY createdAt DESC"

        val db = dbHelper.readableDatabase
        val cursor: Cursor = db.rawQuery(selectQuery, null)

        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getLong(cursor.getColumnIndexOrThrow("id"))
                val topic = cursor.getString(cursor.getColumnIndexOrThrow("topic"))
                val description = cursor.getString(cursor.getColumnIndexOrThrow("description"))
                val icon = cursor.getInt(cursor.getColumnIndexOrThrow("icon"))
                val createdAt = cursor.getString(cursor.getColumnIndexOrThrow("createdAt"))

                val note = Note(id, topic, description, icon, createdAt)
                notesList.add(note)
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()

        return notesList
    }
    fun searchNotes(query: String): List<Note> {
        val notesList = mutableListOf<Note>()
        val searchQuery = "%$query%" // Add wildcards for partial matching

        val db = dbHelper.readableDatabase

        // Create the SQL query to search in both topic and description
        val selectQuery = """
            SELECT * FROM table_note 
            WHERE topic LIKE ? OR description LIKE ? 
            ORDER BY createdAt DESC
        """.trimIndent()

        try {
            val cursor = db.rawQuery(selectQuery, arrayOf(searchQuery, searchQuery))

            if (cursor.moveToFirst()) {
                do {
                    val id = cursor.getLong(cursor.getColumnIndexOrThrow("id"))
                    val topic = cursor.getString(cursor.getColumnIndexOrThrow("topic"))
                    val description = cursor.getString(cursor.getColumnIndexOrThrow("description"))
                    val icon = cursor.getInt(cursor.getColumnIndexOrThrow("icon"))
                    val createdAt = cursor.getString(cursor.getColumnIndexOrThrow("createdAt"))

                    val note = Note(id, topic, description, icon, createdAt)
                    notesList.add(note)
                } while (cursor.moveToNext())
            }

            cursor.close()
        } catch (e: Exception) {
            Log.e("NoteRepository", "Error searching notes", e)
        } finally {
            db.close()
        }

        return notesList
    }
    // Get a single note by ID
    fun getNoteById(id: Long): Note? {
        val db = dbHelper.readableDatabase
        var note: Note? = null

        val cursor = db.query(
            "table_note",
            null,
            "id = ?",
            arrayOf(id.toString()),
            null,
            null,
            null
        )

        if (cursor.moveToFirst()) {
            val topic = cursor.getString(cursor.getColumnIndexOrThrow("topic"))
            val description = cursor.getString(cursor.getColumnIndexOrThrow("description"))
            val icon = cursor.getInt(cursor.getColumnIndexOrThrow("icon"))
            val createdAt = cursor.getString(cursor.getColumnIndexOrThrow("createdAt"))

            note = Note(id, topic, description, icon, createdAt)
        }

        cursor.close()
        db.close()

        return note
    }

    // Update an existing note
    fun updateNote(note: Note): Int {
        val db = dbHelper.writableDatabase

        val values = ContentValues().apply {
            put("topic", note.topic)
            put("description", note.description)
            put("icon", note.icon)
            put("createdAt", note.createdAt)
        }

        val rowsAffected = db.update(
            "table_note",
            values,
            "id = ?",
            arrayOf(note.id.toString())
        )

        db.close()

        return rowsAffected
    }

    // Delete a note
    fun deleteNote(id: Long): Int {
        val db = dbHelper.writableDatabase

        val rowsAffected = db.delete(
            "table_note",
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