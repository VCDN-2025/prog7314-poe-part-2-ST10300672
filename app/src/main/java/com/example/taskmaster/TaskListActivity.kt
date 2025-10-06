package com.example.taskmaster

import android.os.Bundle
import android.text.InputType
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.taskmaster.adapter.TaskAdapter
import com.example.taskmaster.api.RetrofitInstance
import com.example.taskmaster.model.Task
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TaskListActivity : AppCompatActivity() {

    private lateinit var recycler: RecyclerView
    private lateinit var adapter: TaskAdapter
    private val tasks = mutableListOf<Task>()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_list)

        recycler = findViewById(R.id.recyclerTasks)
        recycler.layoutManager = LinearLayoutManager(this)

        adapter = TaskAdapter(
            tasks,
            onTaskUpdated = { task -> updateTaskOnServer(task) },
            onTaskDeleted = { task -> deleteTaskOnServer(task) }
        )
        recycler.adapter = adapter

        fetchTasksFromServer()

        val fab = findViewById<FloatingActionButton>(R.id.fabAddTask)
        fab.setOnClickListener { showAddTaskDialog() }
    }

    override fun onResume() {
        super.onResume()
        fetchTasksFromServer()
    }

    private fun showAddTaskDialog() {
        val linear = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 20, 40, 10)
        }

        val titleInput = EditText(this).apply {
            hint = "Task title"
            inputType = InputType.TYPE_CLASS_TEXT
        }

        val descInput = EditText(this).apply {
            hint = "Description (optional)"
            inputType = InputType.TYPE_CLASS_TEXT
        }

        linear.addView(titleInput)
        linear.addView(descInput)

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("New Task")
            .setView(linear)
            .setPositiveButton("Add") { _, _ ->
                val title = titleInput.text.toString().trim()
                val desc = descInput.text.toString().trim()
                if (title.isNotEmpty()) {
                    val userId = auth.currentUser?.uid ?: return@setPositiveButton
                    val task = Task(id = null, userId = userId, title = title, description = desc)
                    createTaskOnServer(task)
                } else {
                    Toast.makeText(this, "Title cannot be empty", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun fetchTasksFromServer() {
        val userId = auth.currentUser?.uid ?: return
        val prefs = getSharedPreferences("app_settings", MODE_PRIVATE)
        val showCompleted = prefs.getBoolean("show_completed", true)
        val autoSort = prefs.getBoolean("auto_sort", true)

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val response = RetrofitInstance.api.getTasks(userId)
                if (response.isSuccessful) {
                    val fetchedTasksRaw: List<Task> = response.body() ?: emptyList()

                    // --- Apply settings ---
                    val filteredTasks: List<Task> = if (!showCompleted) {
                        fetchedTasksRaw.filter { task -> !task.completed }
                    } else {
                        fetchedTasksRaw
                    }

                    val sortedTasks: List<Task> = if (autoSort) {
                        filteredTasks.sortedByDescending { task -> task.id?.toIntOrNull() ?: 0 }
                    } else {
                        filteredTasks.sortedBy { task -> task.id?.toIntOrNull() ?: 0 }
                    }

                    withContext(Dispatchers.Main) {
                        tasks.clear()
                        tasks.addAll(sortedTasks)
                        adapter.replaceAll(sortedTasks)
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@TaskListActivity, "Failed to load tasks", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@TaskListActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun createTaskOnServer(task: Task) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val response = RetrofitInstance.api.createTask(task)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val createdTask = response.body()?.let { task.copy(id = it.taskId) } ?: task
                        tasks.add(0, createdTask)
                        adapter.replaceAll(tasks)
                        Toast.makeText(this@TaskListActivity, "Task added!", Toast.LENGTH_SHORT).show()
                        fetchTasksFromServer()
                    } else {
                        Toast.makeText(this@TaskListActivity, "Failed to add task", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@TaskListActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun updateTaskOnServer(task: Task) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val taskId = task.id ?: return@launch
                val response = RetrofitInstance.api.updateTask(taskId, task.copy(id = null))
                if (!response.isSuccessful) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@TaskListActivity, "Failed to update task (${response.code()})", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@TaskListActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun deleteTaskOnServer(task: Task) {
        val taskId = task.id ?: run {
            Toast.makeText(this, "Cannot delete: missing id", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val response = RetrofitInstance.api.deleteTask(taskId)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        adapter.removeTaskById(taskId)
                        Toast.makeText(this@TaskListActivity, "Task deleted", Toast.LENGTH_SHORT).show()
                        fetchTasksFromServer()
                    } else {
                        Toast.makeText(this@TaskListActivity, "Failed to delete task", Toast.LENGTH_SHORT).show()
                        fetchTasksFromServer()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@TaskListActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    fetchTasksFromServer()
                }
            }
        }
    }
}
