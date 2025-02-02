package com.shayan.remindersios.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.shayan.remindersios.utils.Notification

/**
 * A [BroadcastReceiver] that triggers a task reminder notification when an alarm fires.
 */
class TaskReminderReceiver : BroadcastReceiver() {

    // region BroadcastReceiver
    override fun onReceive(context: Context, intent: Intent) {
        val taskTitle = intent.getStringExtra("taskTitle") ?: DEFAULT_TASK_TITLE

        // Optional: Log for debugging
        Log.d(TAG, "Received reminder broadcast for task title: $taskTitle")

        // Show the task notification
        Notification.showTaskNotification(context, taskTitle)
    }
    // endregion

    companion object {
        private const val TAG = "TaskReminderReceiver"
        private const val DEFAULT_TASK_TITLE = "Pending Task Reminder"
    }
}