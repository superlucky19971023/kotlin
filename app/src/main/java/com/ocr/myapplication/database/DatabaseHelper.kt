package com.ocr.myapplication.database
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class DatabaseHelper(private val context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val TAG = "DatabaseHelper"
        private const val DATABASE_NAME = "OCR.db"
        private const val DATABASE_VERSION = 1
        private const val DATABASE_PATH = "database/"
    }

    private var database: SQLiteDatabase? = null

    init {
        // Check if the database exists before copying
        if (!checkDatabase()) {
            try {
                copyDatabase()
            } catch (e: IOException) {
                Log.e(TAG, "Error copying database", e)
                throw Error("Error copying database")
            }
        }
    }

    /**
     * Check if the database already exists
     */
    private fun checkDatabase(): Boolean {
        val dbFile = context.getDatabasePath(DATABASE_NAME)
        return dbFile.exists()
    }

    /**
     * Copy the database from assets to the application's data directory
     */
    private fun copyDatabase() {
        // Open the local db as the input stream
        val inputStream = context.assets.open("$DATABASE_PATH$DATABASE_NAME")

        // Create the empty database file
        val outputFile = context.getDatabasePath(DATABASE_NAME)

        // Make sure the database directory exists
        outputFile.parentFile?.mkdirs()

        // Copy the database
        val outputStream = FileOutputStream(outputFile)
        val buffer = ByteArray(1024)
        var length: Int
        while (inputStream.read(buffer).also { length = it } > 0) {
            outputStream.write(buffer, 0, length)
        }

        // Close the streams
        outputStream.flush()
        outputStream.close()
        inputStream.close()

        Log.i(TAG, "Database copied successfully")
    }

    /**
     * Open the database
     */
    @Throws(IOException::class)
    fun openDatabase(): SQLiteDatabase {
        val dbPath = context.getDatabasePath(DATABASE_NAME).path
        if (database == null || !database!!.isOpen) {
            database = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READWRITE)
        }
        return database!!
    }

    /**
     * Close the database if it exists
     */
    override fun close() {
        database?.close()
        super.close()
    }

    // These methods are required but won't be used since we're using a pre-existing database
    override fun onCreate(db: SQLiteDatabase) {
        // No need to create tables as we're using a pre-existing database
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Handle database upgrades if needed
    }
}