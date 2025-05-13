package com.example.taskmaster

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.provider.CalendarContract
import android.widget.DatePicker
import android.widget.TimePicker
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import java.util.*

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
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    val originalTask = tasks.find { it.id == taskId }

    var title by remember { mutableStateOf(originalTask?.title ?: "") }
    var description by remember { mutableStateOf(originalTask?.description ?: "") }
    var dueDate by remember { mutableStateOf(originalTask?.date ?: "") }
    var dueTime by remember { mutableStateOf(originalTask?.time ?: "") }
    var priority by remember { mutableStateOf(originalTask?.priority ?: "High") }

    if (originalTask == null) {
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
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            Column {
                Button(
                    onClick = {
                        if (title.isBlank() || dueDate.isBlank() || dueTime.isBlank()) {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Please fill in all fields.")
                            }
                            return@Button
                        }

                        val updatedTask = originalTask.copy(
                            title = title,
                            description = description,
                            date = dueDate,
                            time = dueTime,
                            priority = priority
                        )
                        viewModel.updateTask(updatedTask)
                        onSaveClick()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    Text("UPDATE")
                }

                Button(
                    onClick = {
                        try {
                            val calendarIntent = Intent(Intent.ACTION_INSERT).apply {
                                data = CalendarContract.Events.CONTENT_URI
                                putExtra(CalendarContract.Events.TITLE, title)
                                putExtra(CalendarContract.Events.DESCRIPTION, description)

                                val calendar = Calendar.getInstance()
                                val parts = dueDate.split("/")
                                val timeParts = dueTime.split(":")
                                calendar.set(parts[2].toInt(), parts[1].toInt() - 1, parts[0].toInt(), timeParts[0].toInt(), timeParts[1].toInt())

                                putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, calendar.timeInMillis)
                                putExtra(CalendarContract.EXTRA_EVENT_END_TIME, calendar.timeInMillis + 60 * 60 * 1000)
                            }
                            context.startActivity(calendarIntent)
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Task added to Google Calendar!")
                            }
                        } catch (e: Exception) {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Failed to open Calendar.")
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(Icons.Filled.CalendarToday, contentDescription = "Calendar")
                    Spacer(Modifier.width(8.dp))
                    Text("Add to Google Calendar")
                }
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
                    val calendar = Calendar.getInstance()
                    DatePickerDialog(context, { _: DatePicker, y, m, d ->
                        dueDate = "$d/${m + 1}/$y"
                    }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).apply {
                        datePicker.minDate = calendar.timeInMillis
                    }.show()
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
                    val calendar = Calendar.getInstance()
                    val selectedDateParts = dueDate.split("/")
                    val selectedDay = selectedDateParts.getOrNull(0)?.toIntOrNull()
                    val selectedMonth = selectedDateParts.getOrNull(1)?.toIntOrNull()
                    val selectedYear = selectedDateParts.getOrNull(2)?.toIntOrNull()

                    TimePickerDialog(context, { _: TimePicker, h, m ->
                        val now = Calendar.getInstance()
                        val isToday = selectedDay == now.get(Calendar.DAY_OF_MONTH) &&
                                selectedMonth == now.get(Calendar.MONTH) + 1 &&
                                selectedYear == now.get(Calendar.YEAR)

                        if (isToday && (h < now.get(Calendar.HOUR_OF_DAY) || (h == now.get(Calendar.HOUR_OF_DAY) && m < now.get(Calendar.MINUTE)))) {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("You cannot pick a past time for today.")
                            }
                        } else {
                            dueTime = String.format("%02d:%02d", h, m)
                        }
                    }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false).show()
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
