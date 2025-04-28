package com.example.taskmaster

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

    val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy") // ✅ fixed format here
    val today = LocalDate.now().format(formatter)

    val isPast = try {
        LocalDate.parse(selectedDate, formatter).isBefore(LocalDate.parse(today, formatter))
    } catch (e: Exception) {
        false
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("${getDayOfWeek(selectedDate)}'s Tasks", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigate("weekly_view") }) {
                        Icon(Icons.Default.CalendarToday, contentDescription = "Back to Weekly View")
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
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
        ) {
            tasks.filter { it.date.replace("/", "-") == selectedDate } // ✅ compare correctly
                .sortedBy {
                    when (it.priority) {
                        "High" -> 0
                        "Mid" -> 1
                        "Low" -> 2
                        else -> 3
                    }
                }
                .forEach { task ->
                    TaskCard(
                        task = task,
                        onDelete = { viewModel.deleteTask(task) },
                        onClick = { /* no edit for now */ }
                    )
                }
        }
    }
}

fun getDayOfWeek(dateString: String): String {
    return try {
        val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy") // ✅ fixed format here
        val date = LocalDate.parse(dateString, formatter)
        date.dayOfWeek.getDisplayName(java.time.format.TextStyle.FULL, Locale.getDefault())
    } catch (e: Exception) {
        ""
    }
}
