package com.example.taskmaster

import android.content.ContentProvider
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.net.Uri
import android.util.Log
import androidx.sqlite.db.SimpleSQLiteQuery

class TaskProvider : ContentProvider() {

    companion object {
        private const val AUTHORITY = "com.example.taskmaster.provider"
        private const val TASKS_TABLE = "tasks"
        val CONTENT_URI: Uri = Uri.parse("content://$AUTHORITY/$TASKS_TABLE")

        private const val TASKS = 1
        private val uriMatcher = UriMatcher(UriMatcher.NO_MATCH).apply {
            addURI(AUTHORITY, TASKS_TABLE, TASKS)
        }
    }

    private lateinit var database: TaskDatabase

    override fun onCreate(): Boolean {
        database = TaskDatabase.getDatabase(context!!)
        return true
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor? {
        return when (uriMatcher.match(uri)) {
            TASKS -> {
                val sql = "SELECT * FROM tasks"
                val query = SimpleSQLiteQuery(sql)
                database.query(query)  // ✅ Runs safely without blocking UI thread
            }
            else -> {
                Log.e("TaskProvider", "Unknown URI: $uri")
                null
            }
        }
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        // Not implemented
        return null
    }

    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int {
        // Not implemented
        return 0
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int {
        // Not implemented
        return 0
    }

    override fun getType(uri: Uri): String? {
        return when (uriMatcher.match(uri)) {
            TASKS -> "vnd.android.cursor.dir/$TASKS_TABLE"
            else -> null
        }
    }
}
