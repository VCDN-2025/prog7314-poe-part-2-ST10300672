package com.example.taskmaster.repository

import com.example.taskmaster.api.RetrofitInstance
import com.example.taskmaster.model.Task
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TaskRepository {

    // ✅ Get all tasks from the server
    suspend fun getAllTasks(userId: String? = null): List<Task> = withContext(Dispatchers.IO) {
        val response = RetrofitInstance.api.getTasks(userId)
        if (response.isSuccessful) {
            response.body() ?: emptyList()
        } else emptyList()
    }

    // ✅ Add a new task
    suspend fun addTask(task: Task) = withContext(Dispatchers.IO) {
        RetrofitInstance.api.createTask(task)
    }

    // ✅ Update an existing task
    suspend fun updateTask(task: Task) = withContext(Dispatchers.IO) {
        val taskId = task.id ?: return@withContext
        RetrofitInstance.api.updateTask(taskId, task)
    }

    // ✅ Delete a task
    suspend fun deleteTask(taskId: String) = withContext(Dispatchers.IO) {
        RetrofitInstance.api.deleteTask(taskId)
    }

    // ✅ Sync placeholder (not used now)
    suspend fun syncPendingTasks() = withContext(Dispatchers.IO) { }
}
