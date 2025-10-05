package com.example.taskmaster.model

data class Task(
    val id: String,
    var title: String,
    var description: String = "",
    var completed: Boolean = false
)
