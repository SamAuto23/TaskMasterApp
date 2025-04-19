package com.example.taskmaster

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class Task(val title: String, val time: String, val priority: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(onAddClick: () -> Unit) {
    val tasks = remember {
        listOf(
            Task("3KM RUN", "2:00 PM", "High"),
            Task("Maths Homework", "4:00 PM", "Mid"),
            Task("Go to the Gym", "6:00 PM", "Low")
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text("Task Master", fontWeight = FontWeight.Bold)
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddClick) {
                Text("+")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
        ) {
            Text("Task List", fontSize = 18.sp, modifier = Modifier.padding(bottom = 8.dp))

            tasks.forEach { task ->
                TaskCard(task)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun TaskCard(task: Task) {
    val priorityColor = when (task.priority) {
        "High" -> Color.Red
        "Mid" -> Color(0xFFFF9800) // Orange
        "Low" -> Color(0xFF4CAF50) // Green
        else -> Color.Gray
    }

    Card(
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .background(Color.LightGray, CircleShape)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(task.title, fontWeight = FontWeight.Bold)
                Text(task.time)
            }
            Box(
                modifier = Modifier
                    .background(priorityColor, RoundedCornerShape(4.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(task.priority, color = Color.White, fontSize = 12.sp)
            }
        }
    }
}
