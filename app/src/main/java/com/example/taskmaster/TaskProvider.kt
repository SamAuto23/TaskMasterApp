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
        private const val AUTHORITY = "com.example.taskmaster"
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
                database.taskDao().queryTasksViaProvider(query)
            }
            else -> {
                Log.e("TaskProvider", "Unknown URI: $uri")
                throw IllegalArgumentException("Unknown URI: $uri")
            }
        }
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        return when (uriMatcher.match(uri)) {
            TASKS -> {
                val task = Task(
                    title = values?.getAsString("title") ?: "",
                    description = values?.getAsString("description") ?: "",
                    date = values?.getAsString("date") ?: "",
                    time = values?.getAsString("time") ?: "",
                    priority = values?.getAsString("priority") ?: "Low",
                    isCompleted = values?.getAsBoolean("isCompleted") ?: false
                )
                val id = database.taskDao().insertTaskViaProvider(task)
                Uri.withAppendedPath(CONTENT_URI, id.toString())
            }
            else -> {
                Log.e("TaskProvider", "Insert: Unknown URI $uri")
                throw IllegalArgumentException("Unknown URI: $uri")
            }
        }
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int {
        return when (uriMatcher.match(uri)) {
            TASKS -> {
                0 // Not supported for all tasks
            }
            else -> {
                val segments = uri.pathSegments
                if (segments.size == 2 && segments[0] == TASKS_TABLE) {
                    val id = segments[1].toIntOrNull()
                    if (id != null) {
                        val task = Task(id, "", "", "", "", "", false)
                        database.taskDao().deleteTaskViaProvider(task)
                        return 1
                    }
                }
                Log.e("TaskProvider", "Delete: Invalid URI $uri")
                throw IllegalArgumentException("Invalid URI: $uri")
            }
        }
    }

    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int {
        return 0
    }

    override fun getType(uri: Uri): String? {
        return when (uriMatcher.match(uri)) {
            TASKS -> "vnd.android.cursor.dir/$TASKS_TABLE"
            else -> null
        }
    }
}
