package com.example.taskmaster.model

data class Task(
    var id: String = "",
    var title: String = "",
    var description: String = "",
    var completed: Boolean = false
)
