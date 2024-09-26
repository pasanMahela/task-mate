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

        // Date Picker
        taskDateInput.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            DatePickerDialog(requireContext(), { _, selectedYear, selectedMonth, selectedDay ->
                taskDateInput.setText("$selectedYear-${selectedMonth + 1}-$selectedDay")
            }, year, month, day).show()
        }

        // Time Picker
        taskTimeInput.setOnClickListener {
            val calendar = Calendar.getInstance()
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)

            TimePickerDialog(requireContext(), { _, selectedHour, selectedMinute ->
                taskTimeInput.setText(String.format("%02d:%02d", selectedHour, selectedMinute))
            }, hour, minute, true).show()
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
                    updateAppWidget() // Update widget when a new task is added
                } else {
                    Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
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
                    updateAppWidget() // Update widget after unmarking a task
                }
            }
            TaskAdapter.Action.DELETE -> {
                confirmAction("Delete task?") {
                    if (task.isCompleted) {
                        val taskIndex = completedTasks.indexOf(task)
                        completedTasks.remove(task)
                        completedTaskAdapter.notifyItemRemoved(taskIndex)
                    } else {
                        val taskIndex = ongoingTasks.indexOf(task)
                        ongoingTasks.remove(task)
                        ongoingTaskAdapter.notifyItemRemoved(taskIndex)
                    }
                    saveTasks(ONGOING_TASKS_KEY, ongoingTasks)
                    saveTasks(COMPLETED_TASKS_KEY, completedTasks)
                    updateAppWidget() // Update widget after deleting a task
                }
            }
            TaskAdapter.Action.EDIT -> {
                showEditTaskDialog(task)
            }
        }
    }

    private fun showEditTaskDialog(task: Task) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_task, null)
        val taskNameInput = dialogView.findViewById<EditText>(R.id.task_name_input)
        val taskDateInput = dialogView.findViewById<EditText>(R.id.task_date_input)
        val taskTimeInput = dialogView.findViewById<EditText>(R.id.task_time_input)

        // Populate existing task data
        taskNameInput.setText(task.name)
        taskDateInput.setText(task.date)
        taskTimeInput.setText(task.time)

        // Date Picker
        taskDateInput.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            DatePickerDialog(requireContext(), { _, selectedYear, selectedMonth, selectedDay ->
                taskDateInput.setText("$selectedYear-${selectedMonth + 1}-$selectedDay")
            }, year, month, day).show()
        }

        // Time Picker
        taskTimeInput.setOnClickListener {
            val calendar = Calendar.getInstance()
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)

            TimePickerDialog(requireContext(), { _, selectedHour, selectedMinute ->
                taskTimeInput.setText(String.format("%02d:%02d", selectedHour, selectedMinute))
            }, hour, minute, true).show()
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Edit Task")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val updatedTaskName = taskNameInput.text.toString()
                val updatedTaskDate = taskDateInput.text.toString()
                val updatedTaskTime = taskTimeInput.text.toString()

                if (updatedTaskName.isNotEmpty() && updatedTaskDate.isNotEmpty() && updatedTaskTime.isNotEmpty()) {
                    task.name = updatedTaskName
                    task.date = updatedTaskDate
                    task.time = updatedTaskTime

                    // Save updated tasks
                    saveTasks(ONGOING_TASKS_KEY, ongoingTasks)
                    ongoingTaskAdapter.notifyDataSetChanged() // Refresh adapter
                    updateAppWidget() // Update widget after editing
                } else {
                    Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun confirmAction(message: String, onConfirm: () -> Unit) {
        AlertDialog.Builder(requireContext())
            .setMessage(message)
            .setPositiveButton("Yes") { _, _ -> onConfirm() }
            .setNegativeButton("No", null)
            .show()
    }

    private fun checkForTodayTasks() {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        ongoingTasks.forEach { task ->
            if (task.date == today) {
                // Notify user or perform an action
                sendNotification(task)
            }
        }
    }

    private fun sendNotification(task: Task) {
        val notificationManager = requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationChannelId = "task_notifications"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(notificationChannelId, "Task Notifications", NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }

        val notificationIntent = Intent(requireContext(), MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(requireContext(), 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        val notification = NotificationCompat.Builder(requireContext(), notificationChannelId)
            .setContentTitle("Task Reminder")
            .setContentText("Don't forget: ${task.name} at ${task.time}!")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        notificationManager.notify(task.id.hashCode(), notification)
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
        val componentName = ComponentName(requireContext(), TaskWidgetProvider::class.java)
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetManager.getAppWidgetIds(componentName), R.id.task_list_view)
    }
}
