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
        val today = LocalDate.now().format(formatter)

        val highPriorityTasks = repository.getTasksForDate(today).filter {
            it.priority == "High" && !it.isCompleted
        }

        if (highPriorityTasks.isNotEmpty()) {
            NotificationHelper.showHighPriorityReminder(
                context = applicationContext,
                title = "High Priority Tasks Today",
                message = "You have ${highPriorityTasks.size} important task(s) to complete!"
            )
        }

        return Result.success()
    }
}
