package com.shayan.remindersios.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.icu.util.Calendar
import android.os.Build
import android.provider.Settings
import android.util.Log
import com.shayan.remindersios.data.models.Tasks
import com.shayan.remindersios.receivers.TaskReminderReceiver
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * An object to handle scheduling and cancelling task reminders with [AlarmManager].
 */
object AlarmManagerHelper {

    private const val TAG = "AlarmManagerHelper"

    // region Schedule a Task Reminder
    /**
     * Schedules an alarm for the given [task], if [task.date] is set.
     * Uses different AlarmManager APIs depending on the Android version.
     *
     * @param context Application [Context].
     * @param task The [Tasks] object containing date/time info to schedule.
     */
    fun scheduleTaskReminder(context: Context, task: Tasks) {
        // If no date is set, we can't schedule anything
        if (task.date.isNullOrEmpty()) return

        // Handle exact alarms permission for Android 12+ (S)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(AlarmManager::class.java)
            if (alarmManager?.canScheduleExactAlarms() == false) {
                try {
                    val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(intent)
                    // Could log or inform the user that the permission is required
                    Log.w(TAG, "Cannot schedule exact alarms; user prompted to allow it.")
                    return
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to request exact alarm permission: ${e.message}", e)
                    return
                }
            }
        }

        // Prepare the AlarmManager
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Parse the task's date
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val taskDate = try {
            dateFormat.parse(task.date) ?: return
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse task date: ${task.date}", e)
            return
        }

        // Build a Calendar for the alarm trigger time
        val calendar = Calendar.getInstance().apply {
            time = taskDate
            if (task.time.isNullOrEmpty()) {
                // Default time is 11:00 AM if not specified
                set(Calendar.HOUR_OF_DAY, 11)
                set(Calendar.MINUTE, 0)
            } else {
                try {
                    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                    val taskTime = timeFormat.parse(task.time)
                    taskTime?.let {
                        set(Calendar.HOUR_OF_DAY, it.hours)
                        set(Calendar.MINUTE, it.minutes)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to parse task time: ${task.time}", e)
                    return
                }
            }
            set(Calendar.SECOND, 0)
        }

        // If the reminder time is in the past, skip scheduling
        if (calendar.timeInMillis < System.currentTimeMillis()) {
            Log.d(TAG, "Skipping scheduling: alarm time is in the past.")
            return
        }

        // Create broadcast intent to fire the reminder
        val intent = Intent(context, TaskReminderReceiver::class.java).apply {
            putExtra("taskId", task.roomTaskId)
            putExtra("taskTitle", task.title)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context, task.roomTaskId.hashCode(), // Unique requestCode
            intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Schedule the alarm based on API level
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
        Log.d(TAG, "Alarm scheduled for ${task.title} at ${calendar.time}")
    }
    // endregion

    // region Cancel a Task Reminder
    /**
     * Cancels a previously scheduled alarm for the given [taskId].
     *
     * @param context The application [Context].
     * @param taskId The unique task ID used when scheduling the alarm.
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
        Log.d(TAG, "Alarm canceled for taskId=$taskId")
    }
    // endregion
}