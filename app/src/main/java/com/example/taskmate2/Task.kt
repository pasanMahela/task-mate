package com.example.taskmate2

import java.util.UUID

data class Task(
    var id: String = UUID.randomUUID().toString(),
    var name: String,
    var date: String,   // Format: YYYY-MM-DD
    var time: String,   // Format: HH:MM
    var isCompleted: Boolean = false
)
