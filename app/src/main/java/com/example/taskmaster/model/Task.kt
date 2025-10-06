package com.example.taskmaster.model

import com.google.gson.annotations.SerializedName

data class Task(
    @SerializedName("_id") val id: String? = null,  // matches your Node.js MongoDB field
    val userId: String,
    var title: String,
    var description: String? = "",
    var completed: Boolean = false
)
