package com.example.taskmate2

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Vibrator
import androidx.appcompat.app.AlertDialog
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import org.json.JSONArray
import org.json.JSONObject
import androidx.activity.result.contract.ActivityResultContracts
import android.Manifest
import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.NotificationChannel
import android.app.TimePickerDialog
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.os.VibrationEffect
import android.widget.EditText
import android.widget.TextView
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class TaskFragment : Fragment() {

    private lateinit var prefs: SharedPreferences
    private lateinit var ongoingTaskAdapter: TaskAdapter
    private lateinit var completedTaskAdapter: TaskAdapter
    private lateinit var ongoingTasks: MutableList<Task>
    private lateinit var completedTasks: MutableList<Task>
    private lateinit var helloText: TextView

    companion object {
        const val TASKS_PREF = "tasks_pref"
        const val ONGOING_TASKS_KEY = "ongoing_tasks"
        const val COMPLETED_TASKS_KEY = "completed_tasks"
    }

    private val requestNotificationPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (!isGranted) {
            Toast.makeText(requireContext(), "Notification permission required", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_task, container, false)

        // Request notification permission if Android 13 or higher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        helloText = view.findViewById(R.id.inbox_title)

        val sharedPref = requireContext().getSharedPreferences("UserPref", Context.MODE_PRIVATE)
        val firstName = sharedPref.getString("firstName", "User") ?: "User"

        helloText.text = "Hello, $firstName"

        // Initialize SharedPreferences
        prefs = requireContext().getSharedPreferences(TASKS_PREF, Context.MODE_PRIVATE)

        // Initialize Task Lists
        ongoingTasks = loadTasks(ONGOING_TASKS_KEY).toMutableList()
        completedTasks = loadTasks(COMPLETED_TASKS_KEY).toMutableList()

        // Setup RecyclerViews
        val ongoingRecyclerView = view.findViewById<RecyclerView>(R.id.ongoing_task_list)
        val completedRecyclerView = view.findViewById<RecyclerView>(R.id.completed_task_list)

        ongoingTaskAdapter = TaskAdapter(requireContext(), ongoingTasks) { task, action -> handleTaskAction(task, action) }
        completedTaskAdapter = TaskAdapter(requireContext(), completedTasks) { task, action -> handleTaskAction(task, action) }

        ongoingRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        ongoingRecyclerView.adapter = ongoingTaskAdapter

        completedRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        completedRecyclerView.adapter = completedTaskAdapter

        // Floating action button to add task
        val addTaskButton = view.findViewById<FloatingActionButton>(R.id.add_task_button)
        addTaskButton.setOnClickListener {
            showAddTaskDialog()
        }

        checkForTodayTasks()

        return view
    }


    private fun showAddTaskDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_task, null)
        val taskNameInput = dialogView.findViewById<EditText>(R.id.task_name_input)
        val taskDateInput = dialogView.findViewById<EditText>(R.id.task_date_input)
        val taskTimeInput = dialogView.findViewById<EditText>(R.id.task_time_input)

        taskDateInput.setOnClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(requireContext(), { _, year, month, day ->
                taskDateInput.setText("$year-${month + 1}-$day")
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        taskTimeInput.setOnClickListener {
            val calendar = Calendar.getInstance()
            TimePickerDialog(requireContext(), { _, hour, minute ->
                taskTimeInput.setText(String.format("%02d:%02d", hour, minute))
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show()
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Add New Task")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val taskName = taskNameInput.text.toString()
                val taskDate = taskDateInput.text.toString()
                val taskTime = taskTimeInput.text.toString()

                if (taskName.isNotEmpty() && taskDate.isNotEmpty() && taskTime.isNotEmpty()) {
                    val newTask = Task(name = taskName, date = taskDate, time = taskTime)
                    ongoingTasks.add(newTask)
                    saveTasks(ONGOING_TASKS_KEY, ongoingTasks)
                    ongoingTaskAdapter.notifyItemInserted(ongoingTasks.size - 1)
                    scheduleNotification(newTask)
                } else {
                    Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun scheduleNotification(task: Task) {
        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(requireContext(), TaskReminderReceiver::class.java).apply {
            putExtra("task_name", task.name)
            putExtra("task_description", "You have a task to complete.")
        }

        val calendar = Calendar.getInstance().apply {
            time = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).parse("${task.date} ${task.time}")
        }

        if (calendar.timeInMillis > System.currentTimeMillis()) {
            val pendingIntent = PendingIntent.getBroadcast(
                requireContext(), System.currentTimeMillis().toInt(), intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (alarmManager.canScheduleExactAlarms()) {
                        alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
                    } else {
                        alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent) // Use inexact alarm
                        Toast.makeText(requireContext(), "New Task added.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
                }
            } catch (e: SecurityException) {
                Toast.makeText(requireContext(), "New Task added(E).", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(requireContext(), "Task time is in the past!", Toast.LENGTH_SHORT).show()
        }
    }




    private fun loadTasks(key: String): List<Task> {
        val tasksJson = prefs.getString(key, "[]")
        val taskList = mutableListOf<Task>()
        val jsonArray = JSONArray(tasksJson)

        for (i in 0 until jsonArray.length()) {
            val taskObject = jsonArray.getJSONObject(i)
            val task = Task(
                id = taskObject.getString("id"),
                name = taskObject.getString("name"),
                date = taskObject.getString("date"),
                time = taskObject.getString("time"),
                isCompleted = taskObject.getBoolean("isCompleted")
            )
            taskList.add(task)
        }

        return taskList
    }

    private fun saveTasks(key: String, tasks: List<Task>) {
        val jsonArray = JSONArray()
        tasks.forEach { task ->
            val taskObject = JSONObject()
            taskObject.put("id", task.id)
            taskObject.put("name", task.name)
            taskObject.put("date", task.date)
            taskObject.put("time", task.time)
            taskObject.put("isCompleted", task.isCompleted)
            jsonArray.put(taskObject)
        }
        prefs.edit().putString(key, jsonArray.toString()).apply()
    }

    private fun handleTaskAction(task: Task, action: TaskAdapter.Action) {
        when (action) {
            TaskAdapter.Action.DONE -> {
                confirmAction("Mark as completed?") {
                    val taskIndex = ongoingTasks.indexOf(task)
                    task.isCompleted = true
                    ongoingTasks.remove(task)
                    completedTasks.add(task)
                    saveTasks(ONGOING_TASKS_KEY, ongoingTasks)
                    saveTasks(COMPLETED_TASKS_KEY, completedTasks)
                    ongoingTaskAdapter.notifyItemRemoved(taskIndex)
                    completedTaskAdapter.notifyItemInserted(completedTasks.size - 1)
                    vibratePhone()
                    updateAppWidget() // Update widget after completing a task
                }
            }
            TaskAdapter.Action.UNDONE -> {
                confirmAction("Mark as ongoing?") {
                    val taskIndex = completedTasks.indexOf(task)
                    task.isCompleted = false
                    completedTasks.remove(task)
                    ongoingTasks.add(task)
                    saveTasks(ONGOING_TASKS_KEY, ongoingTasks)
                    saveTasks(COMPLETED_TASKS_KEY, completedTasks)
                    completedTaskAdapter.notifyItemRemoved(taskIndex)
                    ongoingTaskAdapter.notifyItemInserted(ongoingTasks.size - 1)
                    updateAppWidget() // Update widget after undoing a task
                }
            }
            TaskAdapter.Action.DELETE -> {
                confirmAction("Delete task?") {
                    val taskIndex = if (task.isCompleted) {
                        completedTasks.indexOf(task)
                    } else {
                        ongoingTasks.indexOf(task)
                    }

                    if (task.isCompleted) {
                        completedTasks.remove(task)
                        saveTasks(COMPLETED_TASKS_KEY, completedTasks)
                        completedTaskAdapter.notifyItemRemoved(taskIndex)
                    } else {
                        ongoingTasks.remove(task)
                        saveTasks(ONGOING_TASKS_KEY, ongoingTasks)
                        ongoingTaskAdapter.notifyItemRemoved(taskIndex)
                    }

                    updateAppWidget() // Update widget after deleting a task
                }
            }
            else -> {
                // Handle any unexpected cases
                Toast.makeText(requireContext(), "Unknown action", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun confirmAction(message: String, onConfirmed: () -> Unit) {
        AlertDialog.Builder(requireContext())
            .setMessage(message)
            .setPositiveButton("Yes") { _, _ -> onConfirmed() }
            .setNegativeButton("No", null)
            .show()
    }

    private fun checkForTodayTasks() {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        ongoingTasks.forEach { task ->
            if (task.date == today) {
                showNotification(task)
            }
        }
    }

    private fun showNotification(task: Task) {
        val notificationManager = ContextCompat.getSystemService(requireContext(), NotificationManager::class.java)
        val notificationId = System.currentTimeMillis().toInt()
        val channelId = "task_reminder"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Task Reminder", NotificationManager.IMPORTANCE_HIGH)
            notificationManager?.createNotificationChannel(channel)
        }

        val intent = Intent(requireContext(), MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(requireContext(), notificationId, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val notification = NotificationCompat.Builder(requireContext(), channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Task Reminder")
            .setContentText(task.name)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager?.notify(notificationId, notification)
    }

    private fun vibratePhone() {
        val vibrator = requireContext().getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            vibrator.vibrate(500)
        }
    }

    private fun updateAppWidget() {
        val appWidgetManager = AppWidgetManager.getInstance(requireContext())
        val widgetComponent = ComponentName(requireContext(), TaskWidgetProvider::class.java)
        val ids = appWidgetManager.getAppWidgetIds(widgetComponent)
        appWidgetManager.notifyAppWidgetViewDataChanged(ids, R.id.task_list_view)
        appWidgetManager.updateAppWidget(widgetComponent, null)
    }
}
