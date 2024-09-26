package com.example.taskmate2

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CalenderTaskAdapter(private val tasks: List<Task>) : RecyclerView.Adapter<CalenderTaskAdapter.CalenderTaskViewHolder>() {

    class CalenderTaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val taskName: TextView = itemView.findViewById(R.id.calender_task_name)
        val taskDate: TextView = itemView.findViewById(R.id.calender_task_date)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CalenderTaskViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_calender_task, parent, false)
        return CalenderTaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: CalenderTaskViewHolder, position: Int) {
        val task = tasks[position]
        holder.taskName.text = task.name
        holder.taskDate.text = task.date.toString() // You can format the date as needed
    }

    override fun getItemCount(): Int {
        return tasks.size
    }
}