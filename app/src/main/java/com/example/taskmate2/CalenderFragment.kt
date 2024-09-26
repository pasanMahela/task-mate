package com.example.taskmate2

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CalendarView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class CalenderFragment : Fragment() {

    private lateinit var calendarView: CalendarView
    private lateinit var recyclerView: RecyclerView
    private lateinit var calenderTaskAdapter: CalenderTaskAdapter
    private var filteredTasks: MutableList<Task> = mutableListOf()
    private lateinit var prefs: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_calender, container, false)

        // Initialize SharedPreferences
        prefs = requireContext().getSharedPreferences("TaskPrefs", Context.MODE_PRIVATE)

        // Initialize UI elements
        calendarView = view.findViewById(R.id.calendar_view)
        recyclerView = view.findViewById(R.id.recycler_view_filtered)

        // Setup RecyclerView
        calenderTaskAdapter = CalenderTaskAdapter(filteredTasks)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = calenderTaskAdapter

        // Handle calendar date selection
        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val selectedDate = Calendar.getInstance().apply {
                set(Calendar.YEAR, year)
                set(Calendar.MONTH, month)
                set(Calendar.DAY_OF_MONTH, dayOfMonth)
            }.time

            filterTasksByDate(selectedDate)
        }

        return view
    }

    private fun filterTasksByDate(date: Date) {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val selectedDateString = dateFormat.format(date)

        filteredTasks.clear()

        // Retrieve tasks for the selected date from SharedPreferences
        val tasksJsonString = prefs.getString(selectedDateString, null)
        if (tasksJsonString != null) {
            val tasksJsonArray = JSONArray(tasksJsonString)
            for (i in 0 until tasksJsonArray.length()) {
                val taskObject = tasksJsonArray.getJSONObject(i)
                val task = Task(
                    id = taskObject.getString("id"),
                    name = taskObject.getString("name"),
                    date = taskObject.getString("date"),
                    time = taskObject.getString("time"),
                    isCompleted = taskObject.getBoolean("isCompleted")
                )
                filteredTasks.add(task)
            }
        }

        calenderTaskAdapter.notifyDataSetChanged()

        if (filteredTasks.isEmpty()) {
            Toast.makeText(requireContext(), "No tasks for this date", Toast.LENGTH_SHORT).show()
        }
    }
}
