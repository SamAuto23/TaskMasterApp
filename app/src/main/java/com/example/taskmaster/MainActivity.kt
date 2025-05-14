package com.example.taskmaster

import android.os.Build
import android.os.Bundle
import android.net.Uri
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.work.*
import com.example.taskmaster.ui.theme.TaskMasterTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 1)
        }

        // TEMP: Trigger ReminderWorker in 5 seconds to test notification
        val testRequest = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInitialDelay(5, TimeUnit.SECONDS)
            .build()
        WorkManager.getInstance(this).enqueue(testRequest)

        // Uncomment this after testing:
        // scheduleDailyReminder()

        setContent {
            TaskMasterTheme {
                val navController = rememberNavController()
                AppNavigation(navController)
            }
        }

        CoroutineScope(Dispatchers.IO).launch {
            val uri = Uri.parse("content://com.example.taskmaster.provider/tasks")
            val cursor = contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                while (it.moveToNext()) {
                    val title = it.getString(it.getColumnIndexOrThrow("title"))
                    val date = it.getString(it.getColumnIndexOrThrow("date"))
                    Log.d("ContentProviderTest", "Title: $title, Date: $date")
                }
            }
        }
    }

    private fun scheduleDailyReminder() {
        val now = java.util.Calendar.getInstance()
        val due = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, 6)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            if (before(now)) add(java.util.Calendar.DAY_OF_MONTH, 1)
        }

        val delay = due.timeInMillis - now.timeInMillis

        val workRequest = PeriodicWorkRequestBuilder<ReminderWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "daily_reminder",
            ExistingPeriodicWorkPolicy.UPDATE,
            workRequest
        )
    }
}

@Composable
fun AppNavigation(navController: NavHostController) {
    val context = LocalContext.current
    val db = TaskDatabase.getDatabase(context)
    val repository = TaskRepository(db.taskDao())
    val factory = TaskViewModelFactory(repository)
    val viewModel: TaskViewModel = viewModel(factory = factory)

    NavHost(navController = navController, startDestination = "task_list") {
        composable("task_list") {
            HomeScreen(
                viewModel = viewModel,
                onAddClick = { navController.navigate("add_task") },
                onEditClick = { taskId -> navController.navigate("edit_task/$taskId") },
                onWeeklyViewClick = { navController.navigate("weekly_view") },
                onOverdueClick = { navController.navigate("overdue_tasks") }
            )
        }

        composable("task_list/{date}") { backStackEntry ->
            val selectedDate = backStackEntry.arguments?.getString("date")
            HomeScreen(
                viewModel = viewModel,
                onAddClick = { navController.navigate("add_task") },
                onEditClick = { taskId -> navController.navigate("edit_task/$taskId") },
                onWeeklyViewClick = { navController.navigate("weekly_view") },
                onOverdueClick = { navController.navigate("overdue_tasks") },
                selectedDate = selectedDate
            )
        }

        composable("weekly_view") {
            WeeklyViewScreen(
                viewModel = viewModel,
                navController = navController
            )
        }

        composable("dayView/{selectedDate}") { backStackEntry ->
            val selectedDate = backStackEntry.arguments?.getString("selectedDate") ?: ""
            DayViewScreen(
                viewModel = viewModel,
                navController = navController,
                selectedDate = selectedDate
            )
        }

        composable("add_task") {
            AddTaskScreen(
                viewModel = viewModel,
                onSaveClick = { navController.popBackStack() },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable("edit_task/{taskId}") { backStackEntry ->
            val taskId = backStackEntry.arguments?.getString("taskId")?.toIntOrNull()
            if (taskId != null) {
                EditTaskScreen(
                    viewModel = viewModel,
                    taskId = taskId,
                    onSaveClick = { navController.popBackStack() },
                    onBackClick = { navController.popBackStack() },
                    navController = navController // ✅ fix passed here
                )
            }
        }

        composable("overdue_tasks") {
            OverdueTasksScreen(
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
