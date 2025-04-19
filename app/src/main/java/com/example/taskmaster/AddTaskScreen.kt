package com.example.taskmaster

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.widget.DatePicker
import android.widget.TimePicker
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Schedule

import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskScreen(onSaveClick: () -> Unit, onBackClick: () -> Unit = {}) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var dueDate by remember { mutableStateOf("") }
    var dueTime by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf("High") }

    val context = LocalContext.current

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
        bottomBar = {
            Button(
                onClick = onSaveClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text("SAVE")
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
                    }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
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
                    TimePickerDialog(context, { _: TimePicker, h, m ->
                        dueTime = String.format("%02d:%02d", h, m)
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
