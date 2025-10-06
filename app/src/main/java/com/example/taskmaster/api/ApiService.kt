package com.example.taskmaster.api

import com.example.taskmaster.model.Task
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    @GET("tasks")
    suspend fun getTasks(@Query("userId") userId: String? = null): Response<List<Task>>

    @GET("tasks/{id}")
    suspend fun getTask(@Path("id") id: String): Response<Task>

    @POST("tasks")
    suspend fun createTask(@Body task: Task): Response<TaskResponse>

    @PUT("tasks/{id}")
    suspend fun updateTask(@Path("id") id: String, @Body task: Task): Response<TaskResponse>

    @DELETE("tasks/{id}")
    suspend fun deleteTask(@Path("id") id: String): Response<TaskResponse>
}

data class TaskResponse(
    val message: String,
    val taskId: String? = null
)
