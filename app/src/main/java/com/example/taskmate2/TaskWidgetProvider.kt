package com.example.taskmate2

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews

class TaskWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId, null)
        }
    }

    companion object {
        fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int, tasks: List<Task>?) {
            val views = RemoteViews(context.packageName, R.layout.widget_layout)

            // Set placeholder if no tasks are available
            if (tasks == null || tasks.isEmpty()) {
                views.setTextViewText(R.id.widget_title, "No upcoming tasks")
            } else {
                // Set upcoming tasks title
                views.setTextViewText(R.id.widget_title, "Upcoming Tasks")

                // Prepare task list for the widget
                val taskText = tasks.joinToString(separator = "\n") { task -> "${task.name} - ${task.date}" }
                views.setTextViewText(R.id.task_list_view, taskText)
            }

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}

