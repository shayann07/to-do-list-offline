package com.shayan.remindersios.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.icu.util.Calendar
import android.os.Build
import android.provider.Settings
import com.shayan.remindersios.data.models.Tasks
import com.shayan.remindersios.receivers.TaskReminderReceiver
import java.text.SimpleDateFormat
import java.util.Locale

object AlarmManagerHelper {

    /**
     * Schedules a reminder for a task in the AlarmManager.
     * @param context: Application context.
     * @param task: Task object representing the task data.
     */
    fun scheduleTaskReminder(context: Context, task: Tasks) {
        if (task.date.isNullOrEmpty()) return

        // Handle permission for exact alarms (Android 12+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(AlarmManager::class.java)
            if (alarmManager?.canScheduleExactAlarms() == false) {
                try {
                    val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(intent)
                    return
                } catch (e: Exception) {
                    e.printStackTrace()
                    return
                }
            }
        }

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Parse the task date string
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val taskDate = dateFormat.parse(task.date) ?: return

        // Set up the Calendar instance to hold the reminder time
        val calendar = Calendar.getInstance().apply {
            time = taskDate

            // Check if the time field is provided
            if (task.time.isNullOrEmpty()) {
                // If time is null, default to 11:00 AM
                set(Calendar.HOUR_OF_DAY, 11)
                set(Calendar.MINUTE, 0)
            } else {
                // If a time is provided, parse the time and set it
                try {
                    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                    val taskTime = timeFormat.parse(task.time)

                    taskTime?.let {
                        set(Calendar.HOUR_OF_DAY, it.hours)
                        set(Calendar.MINUTE, it.minutes)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    return
                }
            }
            set(Calendar.SECOND, 0)
        }

        // Skip scheduling if the alarm time is already in the past
        if (calendar.timeInMillis < System.currentTimeMillis()) return

        // Prepare the intent to trigger the receiver
        val intent = Intent(context, TaskReminderReceiver::class.java).apply {
            putExtra("taskId", task.roomTaskId)
            putExtra("taskTitle", task.title)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            task.roomTaskId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Schedule the alarm
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // For API 23+ (Doze Mode), use setExactAndAllowWhileIdle
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent
            )
        } else {
            // For older versions, use setExact
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent
            )
        }
    }

    /**
     * Cancels a previously scheduled alarm for a task.
     * @param context: Application context.
     * @param taskId: Unique task ID to cancel the reminder.
     */
    fun cancelTaskReminder(context: Context, taskId: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, TaskReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            taskId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.cancel(pendingIntent)
    }
}
