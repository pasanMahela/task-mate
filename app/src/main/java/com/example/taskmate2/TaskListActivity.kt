package com.example.taskmate2

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity

class TaskListActivity : AppCompatActivity() {

    private lateinit var tasksListView: ListView
    private lateinit var tasksList: List<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_list)

        tasksListView = findViewById(R.id.tasks_list_view)

        tasksList = intent.getStringArrayListExtra("today_tasks") ?: emptyList()

        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, tasksList)
        tasksListView.adapter = adapter
    }
}

