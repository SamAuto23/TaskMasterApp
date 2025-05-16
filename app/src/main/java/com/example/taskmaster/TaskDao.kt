package com.example.taskmaster

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import android.database.Cursor

@Dao
interface TaskDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task)

    @Query("SELECT * FROM tasks")
    fun getAllTasks(): Flow<List<Task>>

    @Delete
    suspend fun deleteTask(task: Task)

    @Update
    suspend fun updateTask(task: Task)

    @Query("SELECT * FROM tasks WHERE date = :date")
    fun getTasksForDate(date: String): List<Task>

    // Used for ContentProvider query access
    @RawQuery(observedEntities = [Task::class])
    fun queryTasksViaProvider(query: androidx.sqlite.db.SupportSQLiteQuery): Cursor

    @Query("SELECT * FROM tasks")
    fun getAllTasksCursor(): Cursor

    @Insert
    fun insertTaskViaProvider(task: Task): Long

    @Delete
    fun deleteTaskViaProvider(task: Task)

}
