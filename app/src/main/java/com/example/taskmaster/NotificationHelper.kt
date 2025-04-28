package com.example.taskmaster

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

object NotificationHelper {
    private const val CHANNEL_ID = "task_channel"
    private const val CHANNEL_NAME = "Task Reminders"
    private const val CHANNEL_DESC = "Notifications for high priority tasks"
    private const val NOTIFICATION_ID = 1001

    fun showHighPriorityReminder(context: Context, title: String, message: String) {
        createNotificationChannel(context)

        // ✅ Check if permission is granted
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)

            with(NotificationManagerCompat.from(context)) {
                notify(NOTIFICATION_ID, builder.build())
            }
        }
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESC
            }

            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }
}
