package com.example.taskmaster

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.taskmaster.adapter.TaskAdapter
import com.example.taskmaster.model.Task
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.firestore

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var taskAdapter: TaskAdapter
    private val tasks = mutableListOf<Task>()

    // Firebase Firestore
    private val db: FirebaseFirestore = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()
        val user = auth.currentUser

        // --- Views ---
        val tvName = findViewById<TextView>(R.id.tvName)
        val tvEmail = findViewById<TextView>(R.id.tvEmail)
        val imgProfile = findViewById<ImageView>(R.id.imgProfile)
        val btnOpenTasks = findViewById<Button>(R.id.btnOpenTasks)
        val btnLogout = findViewById<Button>(R.id.btnLogout)
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewTasks)

        // --- User Info ---
        tvName.text = user?.displayName ?: "No Name"
        tvEmail.text = user?.email ?: "No Email"
        Glide.with(this).load(user?.photoUrl).into(imgProfile)

        // --- RecyclerView Setup (read-only) ---
        taskAdapter = TaskAdapter(
            tasks,
            onTaskUpdated = { task -> updateTaskCompletionInFirestore(task) },
            onTaskDeleted = {} // disable deletion/edit here
        )
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = taskAdapter

        // --- Enable Firestore offline persistence ---
        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .build()
        db.firestoreSettings = settings

        // --- Load tasks from Firestore ---
        listenToTasksRealtime()

        // --- Navigate to Full Task List ---
        btnOpenTasks.setOnClickListener {
            startActivity(Intent(this, TaskListActivity::class.java))
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

    // 🔹 Real-time Firestore listener
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
                taskAdapter.notifyDataSetChanged()
            }
    }

    // 🔹 Update task completion
    private fun updateTaskCompletionInFirestore(task: Task) {
        val userId = auth.currentUser?.uid ?: return

        db.collection("users")
            .document(userId)
            .collection("tasks")
            .document(task.id)
            .update("completed", task.completed)
            .addOnSuccessListener { /* no toast needed */ }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to update task: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
