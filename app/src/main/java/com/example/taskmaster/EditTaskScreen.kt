package com.example.taskmaster

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.widget.DatePicker
import android.widget.TimePicker
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTaskScreen(
    viewModel: TaskViewModel,
    taskId: Int,
    onSaveClick: () -> Unit,
    onBackClick: () -> Unit = {}
) {
    val tasks by viewModel.allTasks.collectAsState()
    val context = LocalContext.current

    // Find the task by ID
    val originalTask = tasks.find { it.id == taskId }

    // Hold local state for each field, initialize with task data
    var title by remember { mutableStateOf(originalTask?.title ?: "") }
    var description by remember { mutableStateOf(originalTask?.description ?: "") }
    var dueDate by remember { mutableStateOf(originalTask?.date ?: "") }
    var dueTime by remember { mutableStateOf(originalTask?.time ?: "") }
    var priority by remember { mutableStateOf(originalTask?.priority ?: "High") }

    if (originalTask == null) {
        // Show error if no such task (shouldn’t happen in normal flow)
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Task not found.", style = MaterialTheme.typography.titleMedium)
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Task") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            Button(
                onClick = {
                    if (title.isNotBlank() && dueDate.isNotBlank() && dueTime.isNotBlank()) {
                        val updatedTask = originalTask.copy(
                            title = title,
                            description = description,
                            date = dueDate,
                            time = dueTime,
                            priority = priority
                        )
                        viewModel.updateTask(updatedTask)
                        onSaveClick()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text("UPDATE")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Task Title") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth()
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Due Date")
                Spacer(modifier = Modifier.width(16.dp))
                OutlinedButton(onClick = {
                    val calendar = java.util.Calendar.getInstance()
                    DatePickerDialog(context, { _: DatePicker, y, m, d ->
                        dueDate = "$d/${m + 1}/$y"
                    }, calendar.get(java.util.Calendar.YEAR), calendar.get(java.util.Calendar.MONTH), calendar.get(java.util.Calendar.DAY_OF_MONTH)).show()
                }) {
                    Text(if (dueDate.isEmpty()) "Select Date" else dueDate)
                    Spacer(Modifier.width(8.dp))
                    Icon(Icons.Filled.DateRange, contentDescription = "Date")
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Due Time")
                Spacer(modifier = Modifier.width(16.dp))
                OutlinedButton(onClick = {
                    val calendar = java.util.Calendar.getInstance()
                    TimePickerDialog(context, { _: TimePicker, h, m ->
                        dueTime = String.format("%02d:%02d", h, m)
                    }, calendar.get(java.util.Calendar.HOUR_OF_DAY), calendar.get(java.util.Calendar.MINUTE), false).show()
                }) {
                    Text(if (dueTime.isEmpty()) "Select Time" else dueTime)
                    Spacer(Modifier.width(8.dp))
                    Icon(Icons.Filled.Schedule, contentDescription = "Time")
                }
            }
            Text("Priority")
            Row {
                listOf("Low", "Medium", "High").forEach { level ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(end = 16.dp)
                    ) {
                        RadioButton(
                            selected = priority == level,
                            onClick = { priority = level }
                        )
                        Text(level)
                    }
                }
            }
        }
    }
}
