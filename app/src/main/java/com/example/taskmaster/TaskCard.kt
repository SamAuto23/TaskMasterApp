// AI Debugging Declaration:
// This file was debugged with help from ChatGPT to remove the delete icon from the card
// and ensure tasks can only be deleted via swipe gesture. AI was not used to write the entire file —
// only to assist in fixing logic and layout issues, as permitted by the coursework guidelines.

package com.example.taskmaster

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete

@Composable
fun TaskCard(
    task: Task,
    onDelete: () -> Unit,
    onClick: () -> Unit,
    hideDeleteIcon: Boolean = false
) {
    val priorityColor = when (task.priority) {
        "High" -> Color.Red
        "Mid", "Medium" -> Color(0xFFFF9800)
        "Low" -> Color(0xFF4CAF50)
        else -> Color.Gray
    }

    Card(
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .clickable { onClick() }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(task.title, style = MaterialTheme.typography.titleMedium)
                Text("${task.date} • ${task.time}", fontSize = 12.sp)
            }

            Box(
                modifier = Modifier
                    .background(priorityColor, RoundedCornerShape(4.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(task.priority, color = Color.White, fontSize = 12.sp)
            }

            if (!hideDeleteIcon) {
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Task"
                    )
                }
            }
        }
    }
}
