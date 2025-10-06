package com.example.taskmaster.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.taskmaster.model.Task
import com.example.taskmaster.repository.TaskRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TaskViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = TaskRepository()
    private val auth = FirebaseAuth.getInstance()

    private val _allTasks = MutableLiveData<List<Task>>()
    val allTasks: LiveData<List<Task>> get() = _allTasks

    fun fetchTasks() {
        viewModelScope.launch(Dispatchers.IO) {
            val userId = auth.currentUser?.uid ?: return@launch
            val tasks = repository.getAllTasks(userId)
            _allTasks.postValue(tasks)
        }
    }

    fun addTask(task: Task) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addTask(task)
            fetchTasks()
        }
    }

    fun updateTask(task: Task) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateTask(task)
            fetchTasks()
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch(Dispatchers.IO) {
            task.id?.let { repository.deleteTask(it) }
            fetchTasks()
        }
    }

    fun syncTasks() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.syncPendingTasks()
        }
    }
}
