package com.example.taskmate2

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class TaskReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val taskTitle = intent.getStringExtra("task_name") ?: "Task Reminder"
        val taskDescription = intent.getStringExtra("task_description") ?: "You have a task to complete."

        Log.d("TaskReminderReceiver", "Notification triggered for task: $taskTitle")

        NotificationHelper.createNotificationChannel(context)
        NotificationHelper.showNotification(context, taskTitle, taskDescription)
    }
}
