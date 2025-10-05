package com.example.taskmaster

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.taskmaster.adapter.TaskAdapter
import com.example.taskmaster.model.Task
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var taskAdapter: TaskAdapter
    private val tasks = mutableListOf<Task>() // Shared local list for now

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

        // --- RecyclerView Setup ---
        taskAdapter = TaskAdapter(tasks)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = taskAdapter

        // --- Load sample or shared tasks (temporary placeholder) ---
        loadSampleTasks()

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

    private fun loadSampleTasks() {
        // You can later replace this with Firestore or shared preferences
        if (tasks.isEmpty()) {
            tasks.add(Task("1", "Check mail", "Read new messages", false))
            tasks.add(Task("2", "Meeting prep", "Prepare slides for project update", true))
            tasks.add(Task("3", "Grocery shopping", "Buy milk and eggs", false))
            taskAdapter.notifyDataSetChanged()
        }
    }
}
