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
fun AddTaskScreen(
    viewModel: TaskViewModel,
    onSaveClick: () -> Unit,
    onBackClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var dueDate by remember { mutableStateOf("") }
    var dueTime by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf("High") }

    val allFieldsValid = title.isNotBlank() && description.isNotBlank() && dueDate.isNotBlank() && dueTime.isNotBlank()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Task Master") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = {
                        val formattedDate = try {
                            val parts = dueDate.split("/")
                            val day = parts[0].padStart(2, '0')
                            val month = parts[1].padStart(2, '0')
                            val year = parts[2]
                            "$day/$month/$year"
                        } catch (e: Exception) {
                            dueDate
                        }

                        val task = Task(
                            title = title,
                            description = description,
                            date = formattedDate,
                            time = dueTime,
                            priority = priority
                        )
                        viewModel.addTask(task)
                        onSaveClick()
                    },
                    enabled = allFieldsValid,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text("SAVE")
                }

                Button(
                    onClick = {
                        if (allFieldsValid) {
                            try {
                                val dateParts = dueDate.split("/")
                                val timeParts = dueTime.split(":")
                                val calendar = Calendar.getInstance().apply {
                                    set(
                                        dateParts[2].toInt(),
                                        dateParts[1].toInt() - 1,
                                        dateParts[0].toInt(),
                                        timeParts[0].toInt(),
                                        timeParts[1].toInt()
                                    )
                                }

                                val intent = Intent(Intent.ACTION_INSERT).apply {
                                    data = CalendarContract.Events.CONTENT_URI
                                    putExtra(CalendarContract.Events.TITLE, title)
                                    putExtra(CalendarContract.Events.DESCRIPTION, description)
                                    putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, calendar.timeInMillis)
                                    putExtra(CalendarContract.EXTRA_EVENT_END_TIME, calendar.timeInMillis + 60 * 60 * 1000)
                                }

                                context.startActivity(intent)
                            } catch (e: Exception) {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Failed to open Calendar.")
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
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
                    val datePicker = DatePickerDialog(
                        context,
                        { _: DatePicker, y, m, d ->
                            dueDate = "$d/${m + 1}/$y"
                        },
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)
                    )
                    datePicker.datePicker.minDate = calendar.timeInMillis
                    datePicker.show()
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
