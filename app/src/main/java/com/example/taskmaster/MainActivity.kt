package com.example.taskmaster

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
import com.example.taskmaster.ui.theme.TaskMasterTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TaskMasterTheme {
                val navController = rememberNavController()
                AppNavigation(navController)
            }
        }
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
                navController = navController,
                viewModel = viewModel,
                onAddClick = { navController.navigate("add_task") },
                onEditClick = { taskId ->
                    navController.navigate("edit_task/$taskId")
                }
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
