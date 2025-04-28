package com.example.taskmaster

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task)

    @Query("SELECT * FROM Task")
    fun getAllTasks(): Flow<List<Task>>

    @Delete
    suspend fun deleteTask(task: Task)

    @Update
    suspend fun updateTask(task: Task)

    // ✅ NEW: Get tasks for a specific date (for ReminderWorker)
    @Query("SELECT * FROM Task WHERE date = :date")
    fun getTasksForDate(date: String): List<Task>
}
