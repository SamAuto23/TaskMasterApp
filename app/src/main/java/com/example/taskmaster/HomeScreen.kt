package com.example.taskmaster

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import androidx.compose.material3.DismissDirection
import androidx.compose.material3.DismissValue
import androidx.compose.material3.rememberDismissState
import androidx.compose.material3.SwipeToDismiss

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: TaskViewModel,
    onAddClick: () -> Unit,
    onEditClick: (Int) -> Unit,
    onWeeklyViewClick: () -> Unit,
    selectedDate: String? = null
) {
    val tasks by viewModel.allTasks.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    val todayDate = remember { LocalDate.now().format(formatter) }

    val currentDate = selectedDate ?: todayDate

    val isPast = remember(currentDate) {
        val selected = LocalDate.parse(currentDate, formatter)
        val today = LocalDate.now()
        selected.isBefore(today)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Task Master", fontWeight = FontWeight.Bold) },
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
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
        ) {
            Text(
                text = if (selectedDate == null) "Today's Tasks" else "Tasks for $currentDate",
                fontSize = 18.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            tasks
                .filter { it.date == currentDate }
                .sortedBy {
                    when (it.priority) {
                        "High" -> 0
                        "Mid" -> 1
                        "Low" -> 2
                        else -> 3
                    }
                }
                .forEach { task ->
                    val dismissState = rememberDismissState(
                        confirmValueChange = {
                            if (it == DismissValue.DismissedToEnd || it == DismissValue.DismissedToStart) {
                                viewModel.deleteTask(task)
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Task deleted")
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
                                onDelete = { viewModel.deleteTask(task) },
                                onClick = { onEditClick(task.id) }
                            )
                        },
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
        }
    }
}
