package com.example.taskmaster

import kotlinx.coroutines.flow.Flow

class TaskRepository(private val taskDao: TaskDao) {

    suspend fun insertTask(task: Task) = taskDao.insertTask(task)

    fun getAllTasks(): Flow<List<Task>> = taskDao.getAllTasks()

    suspend fun deleteTask(task: Task) = taskDao.deleteTask(task)

    suspend fun updateTask(task: Task) = taskDao.updateTask(task) // ✅ Added
}
