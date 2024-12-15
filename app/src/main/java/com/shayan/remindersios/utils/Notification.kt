package com.shayan.remindersios.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import com.shayan.remindersios.R

object Notification {
    private const val CHANNEL_ID = "task_reminder_channel"

    fun showTaskNotification(context: android.content.Context, taskTitle: String) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create Notification Channel for API 26+
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, "Task Reminders", NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        // Build and display the notification
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable._174968_apple_bird_code_ios_logo_icon)
            .setContentTitle("Task Reminder").setContentText(taskTitle)
            .setPriority(NotificationCompat.PRIORITY_HIGH).build()

        notificationManager.notify(taskTitle.hashCode(), notification)
    }
}