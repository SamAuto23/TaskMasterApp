package com.example.taskmaster

import android.os.Build
import android.os.Bundle
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
import java.util.*
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 1)
        }

        scheduleDailyReminder()

        setContent {
            TaskMasterTheme {
                val navController = rememberNavController()
                AppNavigation(navController)
            }
        }
    }

    private fun scheduleDailyReminder() {
        val now = Calendar.getInstance()
        val due = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 6)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            if (before(now)) add(Calendar.DAY_OF_MONTH, 1)
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
                onWeeklyViewClick = { navController.navigate("weekly_view") }
            )
        }

        composable("task_list/{date}") { backStackEntry ->
            val selectedDate = backStackEntry.arguments?.getString("date")
            HomeScreen(
                viewModel = viewModel,
                onAddClick = { navController.navigate("add_task") },
                onEditClick = { taskId -> navController.navigate("edit_task/$taskId") },
                onWeeklyViewClick = { navController.navigate("weekly_view") },
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
                    onBackClick = { navController.popBackStack() }
                )
            }
        }
    }
}
