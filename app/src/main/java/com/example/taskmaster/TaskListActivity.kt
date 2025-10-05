package com.example.taskmaster

import android.app.AlertDialog
import android.os.Bundle
import android.text.InputType
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.taskmaster.adapter.TaskAdapter
import com.example.taskmaster.model.Task
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.util.*

class TaskListActivity : AppCompatActivity() {

    private lateinit var recycler: RecyclerView
    private lateinit var adapter: TaskAdapter
    private val tasks = mutableListOf<Task>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_list)

        recycler = findViewById(R.id.recyclerTasks)
        recycler.layoutManager = LinearLayoutManager(this)
        adapter = TaskAdapter(tasks)
        recycler.adapter = adapter

        // Optional: sample tasks
        tasks.add(Task(UUID.randomUUID().toString(), "Welcome task", "This is a sample task", false))
        adapter.notifyItemInserted(0)

        val fab = findViewById<FloatingActionButton>(R.id.fabAddTask)
        fab.setOnClickListener { showAddTaskDialog() }
    }

    private fun showAddTaskDialog() {
        // Dialog with two EditTexts: title and description
        val container = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        )
        val titleInput = EditText(this)
        titleInput.hint = "Task title"
        titleInput.inputType = InputType.TYPE_CLASS_TEXT

        val descInput = EditText(this)
        descInput.hint = "Description (optional)"
        descInput.inputType = InputType.TYPE_CLASS_TEXT

        val linear = android.widget.LinearLayout(this)
        linear.orientation = android.widget.LinearLayout.VERTICAL
        linear.setPadding(40, 20, 40, 10)
        linear.addView(titleInput)
        linear.addView(descInput)

        AlertDialog.Builder(this)
            .setTitle("New Task")
            .setView(linear)
            .setPositiveButton("Add") { _, _ ->
                val title = titleInput.text.toString().trim()
                val desc = descInput.text.toString().trim()
                if (title.isNotEmpty()) {
                    val t = Task(UUID.randomUUID().toString(), title, desc, false)
                    adapter.addTask(t)
                    recycler.scrollToPosition(0)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
