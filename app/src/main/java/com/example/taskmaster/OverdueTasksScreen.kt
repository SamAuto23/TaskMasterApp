package com.example.taskmaster.com.example.taskmaster.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import androidx.compose.material3.DismissDirection
import androidx.compose.material3.DismissValue
import androidx.compose.material3.rememberDismissState
import androidx.compose.material3.SwipeToDismiss
import com.example.taskmaster.Task
import com.example.taskmaster.TaskCard
import com.example.taskmaster.TaskViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OverdueTasksScreen(
    viewModel: TaskViewModel,
    onBackClick: () -> Unit
) {
    val tasks by viewModel.allTasks.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    val today = LocalDate.now()

    val overdueTasks = tasks
        .filter {
            val taskDate = try {
                LocalDate.parse(it.date, formatter)
            } catch (e: Exception) {
                null
            }
            taskDate != null && taskDate.isBefore(today)
        }
        .sortedWith(
            compareBy<Task> {
                when (it.priority) {
                    "High" -> 0
                    "Medium" -> 1
                    "Low" -> 2
                    else -> 3
                }
            }.thenByDescending {
                LocalDate.parse(it.date, formatter)
            }
        )

    var recentlyDeletedTask by remember { mutableStateOf<Task?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Overdue Tasks") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
        ) {
            if (overdueTasks.isEmpty()) {
                Text("No overdue tasks!", modifier = Modifier.align(Alignment.CenterHorizontally))
            } else {
                overdueTasks.forEach { task ->
                    val dismissState = rememberDismissState(
                        confirmValueChange = {
                            if (it == DismissValue.DismissedToStart || it == DismissValue.DismissedToEnd) {
                                viewModel.deleteTask(task)
                                recentlyDeletedTask = task
                                coroutineScope.launch {
                                    val result = snackbarHostState.showSnackbar(
                                        "Task deleted",
                                        actionLabel = "Undo"
                                    )
                                    if (result == SnackbarResult.ActionPerformed) {
                                        recentlyDeletedTask?.let(viewModel::addTask)
                                        recentlyDeletedTask = null
                                    }
                                }
                            }
                            true
                        }
                    )

                    SwipeToDismiss(
                        state = dismissState,
                        directions = setOf(DismissDirection.StartToEnd, DismissDirection.EndToStart),
                        background = {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Red),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.White)
                            }
                        },
                        dismissContent = {
                            TaskCard(
                                task = task,
                                onDelete = {
                                    viewModel.deleteTask(task)
                                    recentlyDeletedTask = task
                                    coroutineScope.launch {
                                        val result = snackbarHostState.showSnackbar(
                                            "Task deleted",
                                            actionLabel = "Undo"
                                        )
                                        if (result == SnackbarResult.ActionPerformed) {
                                            recentlyDeletedTask?.let(viewModel::addTask)
                                            recentlyDeletedTask = null
                                        }
                                    }
                                },
                                onClick = {}
                            )
                        },
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
            }
        }
    }
}
