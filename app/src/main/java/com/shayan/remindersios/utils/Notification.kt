package com.shayan.remindersios.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.shayan.remindersios.R

/**
 * Utility object for creating and displaying task reminder notifications.
 */
object Notification {

    // region Constants
    private const val CHANNEL_ID = "task_reminder_channel"
    private const val CHANNEL_NAME = "Task Reminders"
    // endregion

    // region Public Methods
    /**
     * Displays a notification with [taskTitle] in the system tray.
     * Creates a notification channel on API 26+ devices if not already present.
     *
     * @param context The [Context] used to obtain a [NotificationManager].
     * @param taskTitle The title content for the notification (typically the task’s title).
     */
    fun showTaskNotification(context: Context, taskTitle: String) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create Notification Channel for API 26+ devices
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(notificationManager)
        }

        // Build and display the notification
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable._174968_apple_bird_code_ios_logo_icon)
            .setContentTitle(context.getString(R.string.reminder_title)) // "Task Reminder"
            .setContentText(taskTitle).setPriority(NotificationCompat.PRIORITY_HIGH).build()

        notificationManager.notify(taskTitle.hashCode(), notification)
    }
    // endregion

    // region Private Methods
    /**
     * Creates a high-importance notification channel for task reminders (API 26+).
     */
    private fun createNotificationChannel(manager: NotificationManager) {
        val channel = NotificationChannel(
            CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH
        )
        manager.createNotificationChannel(channel)
    }
    // endregion
}