package com.example.taskmaster

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeeklyViewScreen(
    viewModel: TaskViewModel,
    navController: NavController
) {
    val allTasks by viewModel.allTasks.collectAsState()
    val today = LocalDate.now()
    val weekStart = today.with(DayOfWeek.MONDAY)

    val daysOfWeek = List(7) { i ->
        weekStart.plusDays(i.toLong())
    }

    val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Weekly View", fontWeight = FontWeight.Bold) }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
        ) {
            daysOfWeek.forEach { day ->
                val formattedDay = day.format(dateFormatter).replace("/", "-") // ✅ fixed here
                val taskCount = allTasks.count { it.date == day.format(dateFormatter) }
                val isToday = day == today

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .clickable {
                            navController.navigate("dayView/$formattedDay")
                        }
                        .background(
                            if (isToday) Color(0xFFFFCDD2) else Color.Transparent,
                            RoundedCornerShape(8.dp)
                        )
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = day.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault()),
                        fontSize = 16.sp,
                        color = if (isToday) Color.Red else Color.Black,
                        fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
                    )
                    Text(
                        text = taskCount.toString(),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
