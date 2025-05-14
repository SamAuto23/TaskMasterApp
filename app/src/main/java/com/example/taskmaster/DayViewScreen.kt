// AI Debugging Declaration:
// This file was debugged with help from ChatGPT to implement swipe-to-delete,
// enable task editing for future dates, add ShareSheet functionality for selected days,
// and improve scroll functionality for proper display in all orientations.
// AI was not used to write the entire file — only to assist in resolving layout issues and bugs,
// as permitted by the coursework guidelines.

package com.example.taskmaster

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DayViewScreen(
    viewModel: TaskViewModel,
    navController: NavController,
    selectedDate: String
) {
    val tasks by viewModel.allTasks.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
    val today = LocalDate.now().format(formatter)

    val isPast = try {
        LocalDate.parse(selectedDate, formatter).isBefore(LocalDate.parse(today, formatter))
    } catch (e: Exception) {
        false
    }

    var recentlyDeletedTask by remember { mutableStateOf<Task?>(null) }

    val filteredTasks = remember(tasks, selectedDate) {
        tasks.filter { it.date.replace("/", "-") == selectedDate }.sortedBy {
            when (it.priority) {
                "High" -> 0
                "Medium" -> 1
                "Low" -> 2
                else -> 3
            }
        }
    }

    fun shareTasks() {
        val taskText = if (filteredTasks.isEmpty()) {
            "No tasks for $selectedDate."
        } else {
            "${getDayOfWeek(selectedDate)}'s Tasks:\n" +
                    filteredTasks.joinToString("\n") { "• ${it.title} (${it.priority}) - ${it.time}" }
        }

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, taskText)
        }
        context.startActivity(Intent.createChooser(intent, "Share tasks via"))
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("${getDayOfWeek(selectedDate)}'s Tasks", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigate("weekly_view") }) {
                        Icon(Icons.Default.CalendarToday, contentDescription = "Back to Weekly View")
                    }
                },
                actions = {
                    IconButton(onClick = { shareTasks() }) {
                        Icon(Icons.Default.Share, contentDescription = "Share Tasks")
                    }
                }
            )
        },
        floatingActionButton = {
            if (!isPast) {
                FloatingActionButton(onClick = { navController.navigate("add_task") }) {
                    Text("+")
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            filteredTasks.forEach { task ->
                val dismissState = remember(task.id) {
                    DismissState(
                        initialValue = DismissValue.Default,
                        confirmValueChange = { value ->
                            if (value == DismissValue.DismissedToStart || value == DismissValue.DismissedToEnd) {
                                viewModel.deleteTask(task)
                                recentlyDeletedTask = task
                                coroutineScope.launch {
                                    val result = snackbarHostState.showSnackbar(
                                        message = "Task deleted",
                                        actionLabel = "Undo"
                                    )
                                    if (result == SnackbarResult.ActionPerformed) {
                                        recentlyDeletedTask?.let(viewModel::addTask)
                                        recentlyDeletedTask = null
                                    }
                                }
                                true
                            } else false
                        }
                    )
                }

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
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = Color.White
                            )
                        }
                    },
                    dismissContent = {
                        TaskCard(
                            task = task,
                            onDelete = {},
                            onClick = {
                                if (!isPast) {
                                    navController.navigate("edit_task/${task.id}")
                                }
                            },
                            hideDeleteIcon = true
                        )
                    },
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

fun getDayOfWeek(dateString: String): String {
    return try {
        val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
        val date = LocalDate.parse(dateString, formatter)
        date.dayOfWeek.getDisplayName(java.time.format.TextStyle.FULL, Locale.getDefault())
    } catch (e: Exception) {
        ""
    }
}
