package com.example.taskmate2

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TaskAdapter(
    private val context: Context,
    private val tasks: List<Task>,
    private val onTaskAction: (Task, Action) -> Unit
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    enum class Action {
        DONE, UNDONE, DELETE, EDIT
    }

    inner class TaskViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val taskName: TextView = view.findViewById(R.id.task_name)
        val taskDate: TextView = view.findViewById(R.id.task_date)
        val taskTime: TextView = view.findViewById(R.id.task_time)
        val taskDone: ImageView = view.findViewById(R.id.task_done)
        val taskUndone: ImageView = view.findViewById(R.id.task_undone)
        val taskDelete: ImageView = view.findViewById(R.id.task_delete)
        val taskEdit: ImageView = view.findViewById(R.id.task_edit) // Add an ImageView for editing

        fun bind(task: Task) {
            taskName.text = task.name
            taskDate.text = task.date
            taskTime.text = task.time

            taskDone.visibility = if (task.isCompleted) View.GONE else View.VISIBLE
            taskUndone.visibility = if (task.isCompleted) View.VISIBLE else View.GONE

            taskDone.setOnClickListener { onTaskAction(task, Action.DONE) }
            taskUndone.setOnClickListener { onTaskAction(task, Action.UNDONE) }
            taskDelete.setOnClickListener { onTaskAction(task, Action.DELETE) }
            taskEdit.setOnClickListener { onTaskAction(task, Action.EDIT) } // Handle edit action
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.task_item, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.bind(tasks[position])
    }

    override fun getItemCount(): Int = tasks.size
}
