// AI Debugging Declaration:
// This file was debugged with help from ChatGPT to fix swipe-to-delete functionality,
// improve task deletion logic with undo support, ensure scrolling works correctly,
// and implement Android ShareSheet for sharing today's tasks. AI was not used to write the full file.

package com.example.taskmaster

import android.content.Intent
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
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import androidx.compose.material3.DismissDirection
import androidx.compose.material3.DismissValue
import androidx.compose.material3.SwipeToDismiss
import androidx.compose.material3.DismissState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: TaskViewModel,
    onAddClick: () -> Unit,
    onEditClick: (Int) -> Unit,
    onWeeklyViewClick: () -> Unit,
    onOverdueClick: () -> Unit,
    selectedDate: String? = null
) {
    val tasks by viewModel.allTasks.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    val todayDate = remember { LocalDate.now().format(formatter) }
    val currentDate = selectedDate ?: todayDate

    val isPast = remember(currentDate) {
        val selected = LocalDate.parse(currentDate, formatter)
        val today = LocalDate.now()
        selected.isBefore(today)
    }

    var recentlyDeletedTask by remember { mutableStateOf<Task?>(null) }

    val filteredTasks = remember(tasks, currentDate) {
        tasks.filter { it.date == currentDate }.sortedBy {
            when (it.priority) {
                "High" -> 0
                "Medium" -> 1
                "Low" -> 2
                else -> 3
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Task Master", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = {
                        val taskText = filteredTasks.joinToString("\n") {
                            "• ${it.title} (${it.priority}) - ${it.time}"
                        }
                        val shareIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, "Today's Tasks:\n$taskText")
                            type = "text/plain"
                        }
                        context.startActivity(
                            Intent.createChooser(shareIntent, "Share Tasks")
                        )
                    }) {
                        Icon(Icons.Default.Share, contentDescription = "Share Tasks")
                    }
                },
                actions = {
                    IconButton(onClick = onWeeklyViewClick) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = "Weekly View"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            if (!isPast) {
                FloatingActionButton(onClick = onAddClick) {
                    Text("+")
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .align(Alignment.TopCenter)
            ) {
                Text(
                    text = if (selectedDate == null) "Today's Tasks" else "Tasks for $currentDate",
                    fontSize = 18.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Button(
                    onClick = onOverdueClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    Text("View Overdue Tasks")
                }

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
                                onDelete = {
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
                                },
                                onClick = { onEditClick(task.id) }
                            )
                        },
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}
