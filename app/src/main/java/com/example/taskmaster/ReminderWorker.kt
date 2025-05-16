package com.example.taskmaster

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class ReminderWorker(
    context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    override fun doWork(): Result {
        val db = TaskDatabase.getDatabase(applicationContext)
        val repository = TaskRepository(db.taskDao())

        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        val yesterday = LocalDate.now().minusDays(1).format(formatter)

        val highPriorityTasks = repository.getTasksForDate(yesterday).filter {
            it.priority == "High" && !it.isCompleted
        }

        if (highPriorityTasks.isNotEmpty()) {
            NotificationHelper.showHighPriorityReminder(
                context = applicationContext,
                title = "High Priority Tasks from Yesterday",
                message = "You have ${highPriorityTasks.size} unfinished high-priority task(s) from yesterday."
            )
        }

        return Result.success()
    }
}