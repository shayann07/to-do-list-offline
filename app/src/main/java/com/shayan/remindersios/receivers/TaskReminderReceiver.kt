package com.shayan.remindersios.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.shayan.remindersios.utils.Notification

class TaskReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val taskTitle = intent.getStringExtra("taskTitle") ?: "Pending Task Reminder"
        Notification.showTaskNotification(context, taskTitle)
    }
}