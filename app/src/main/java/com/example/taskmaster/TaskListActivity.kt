package com.example.taskmaster

import android.os.Bundle
import android.text.InputType
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.taskmaster.adapter.TaskAdapter
import com.example.taskmaster.model.Task
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.firestore
import java.util.*

class TaskListActivity : AppCompatActivity() {

    private lateinit var recycler: RecyclerView
    private lateinit var adapter: TaskAdapter
    private val tasks = mutableListOf<Task>()

    // Firebase instances
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_list)

        recycler = findViewById(R.id.recyclerTasks)
        recycler.layoutManager = LinearLayoutManager(this)
        adapter = TaskAdapter(tasks,
            onTaskUpdated = { task -> showEditTaskDialog(task) },
            onTaskDeleted = { task -> showDeleteConfirmation(task) }
        )
        recycler.adapter = adapter

        // Enable offline persistence
        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .build()
        db.firestoreSettings = settings

        // Real-time updates
        listenToTasksRealtime()

        val fab = findViewById<FloatingActionButton>(R.id.fabAddTask)
        fab.setOnClickListener { showAddTaskDialog() }

        // Optional Firestore test
        testFirestoreConnection()
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
                    val task = Task(UUID.randomUUID().toString(), title, desc, false)
                    saveTaskToFirestore(task)
                } else {
                    Toast.makeText(this, "Title cannot be empty", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun saveTaskToFirestore(task: Task) {
        val userId = auth.currentUser?.uid ?: return

        db.collection("users")
            .document(userId)
            .collection("tasks")
            .document(task.id)
            .set(task)
            .addOnSuccessListener {
                Toast.makeText(this, "Task added!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to save task: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun listenToTasksRealtime() {
        val userId = auth.currentUser?.uid ?: return

        db.collection("users")
            .document(userId)
            .collection("tasks")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Toast.makeText(this, "Error loading tasks: ${error.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                tasks.clear()
                snapshot?.documents?.forEach { doc ->
                    val task = doc.toObject(Task::class.java)
                    if (task != null) tasks.add(task)
                }
                adapter.notifyDataSetChanged()
            }
    }

    // 🔹 Edit task directly (no pre-confirmation)
    private fun showEditTaskDialog(task: Task) {
        val linear = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 20, 40, 10)
        }

        val titleInput = EditText(this).apply {
            hint = "Task title"
            inputType = InputType.TYPE_CLASS_TEXT
            setText(task.title)
        }

        val descInput = EditText(this).apply {
            hint = "Description"
            inputType = InputType.TYPE_CLASS_TEXT
            setText(task.description)
        }

        linear.addView(titleInput)
        linear.addView(descInput)

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Edit Task")
            .setView(linear)
            .setPositiveButton("Save") { _, _ ->
                val newTitle = titleInput.text.toString().trim()
                val newDesc = descInput.text.toString().trim()
                if (newTitle.isNotEmpty()) {
                    task.title = newTitle
                    task.description = newDesc
                    updateTaskInFirestore(task)
                } else {
                    Toast.makeText(this, "Title cannot be empty", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // 🔹 Confirm before deleting task
    private fun showDeleteConfirmation(task: Task) {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Delete Task")
            .setMessage("Are you sure you want to delete this task?")
            .setPositiveButton("Delete") { _, _ -> deleteTaskFromFirestore(task) }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // 🔹 Update task (title, description, completed)
    private fun updateTaskInFirestore(task: Task) {
        val userId = auth.currentUser?.uid ?: return
        db.collection("users")
            .document(userId)
            .collection("tasks")
            .document(task.id)
            .set(task)
            .addOnSuccessListener {
                Toast.makeText(this, "Task updated!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to update task: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // 🔹 Delete task
    private fun deleteTaskFromFirestore(task: Task) {
        val userId = auth.currentUser?.uid ?: return
        db.collection("users")
            .document(userId)
            .collection("tasks")
            .document(task.id)
            .delete()
            .addOnSuccessListener {
                tasks.remove(task)
                adapter.notifyDataSetChanged()
                Toast.makeText(this, "Task deleted!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to delete task: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // 🔹 Optional Firestore test function
    private fun testFirestoreConnection() {
        val testDoc = hashMapOf(
            "message" to "Hello Firestore!",
            "timestamp" to System.currentTimeMillis()
        )

        db.collection("test")
            .document("connection_test")
            .set(testDoc)
            .addOnSuccessListener {
                Toast.makeText(this, "Firestore test succeeded!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Firestore test failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}
