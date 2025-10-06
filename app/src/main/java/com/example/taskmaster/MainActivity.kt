package com.example.taskmaster

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.taskmaster.adapter.TaskAdapter
import com.example.taskmaster.api.RetrofitInstance
import com.example.taskmaster.model.Task
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var taskAdapter: TaskAdapter
    private val tasks = mutableListOf<Task>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()
        val user = auth.currentUser

        val tvName = findViewById<TextView>(R.id.tvName)
        val tvEmail = findViewById<TextView>(R.id.tvEmail)
        val imgProfile = findViewById<ImageView>(R.id.imgProfile)
        val btnOpenTasks = findViewById<Button>(R.id.btnOpenTasks)
        val btnSettings = findViewById<Button>(R.id.btnSettings)
        val btnLogout = findViewById<Button>(R.id.btnLogout)
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewTasks)

        // --- User Info ---
        tvName.text = user?.displayName ?: "No Name"
        tvEmail.text = user?.email ?: "No Email"
        Glide.with(this).load(user?.photoUrl).into(imgProfile)

        // --- RecyclerView setup ---
        taskAdapter = TaskAdapter(
            tasks,
            onTaskUpdated = { task -> updateTaskOnServer(task) },
            onTaskDeleted = { task -> deleteTaskOnServer(task) }
        )
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = taskAdapter

        // --- Fetch tasks from your API ---
        fetchTasksFromServer()

        // --- Navigation buttons ---
        btnOpenTasks.setOnClickListener {
            startActivity(Intent(this, TaskListActivity::class.java))
        }

        btnSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        // --- Logout ---
        btnLogout.setOnClickListener {
            auth.signOut()
            val googleSignInClient = GoogleSignIn.getClient(
                this,
                GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build()
            )
            googleSignInClient.signOut().addOnCompleteListener {
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
        }
    }

    // Auto-refresh whenever user returns to MainActivity
    override fun onResume() {
        super.onResume()
        fetchTasksFromServer() // refresh every time user returns
        val user = auth.currentUser
        findViewById<TextView>(R.id.tvName).text = user?.displayName ?: "No Name"
    }

    // --- Fetch tasks from Node.js API ---
    private fun fetchTasksFromServer() {
        val userId = auth.currentUser?.uid ?: return

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val response = RetrofitInstance.api.getTasks(userId)
                if (response.isSuccessful) {
                    val fetchedTasks = response.body() ?: emptyList()
                    withContext(Dispatchers.Main) {
                        tasks.clear()
                        tasks.addAll(fetchedTasks)
                        taskAdapter.replaceAll(fetchedTasks)
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@MainActivity, "Failed to load tasks", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // --- Update task on Node.js API ---
    private fun updateTaskOnServer(task: Task) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val taskId = task.id ?: return@launch
                val response = RetrofitInstance.api.updateTask(taskId, task.copy(id = null))
                Log.d("API_UPDATE", "Response code: ${response.code()} | body: ${response.errorBody()?.string()}")

                if (!response.isSuccessful) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@MainActivity, "Failed to update task (${response.code()})", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    // --- Delete task on Node.js API ---
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
                        // remove locally for immediate feedback
                        taskAdapter.removeTaskById(taskId)
                        Toast.makeText(this@MainActivity, "Task deleted", Toast.LENGTH_SHORT).show()
                        // refresh from server to ensure consistency
                        fetchTasksFromServer()
                    } else {
                        Toast.makeText(this@MainActivity, "Failed to delete task", Toast.LENGTH_SHORT).show()
                        fetchTasksFromServer()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    fetchTasksFromServer()
                }
            }
        }
    }
}
