package com.example.taskmaster.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.taskmaster.R
import com.example.taskmaster.model.Task

class TaskAdapter(
    private val tasks: MutableList<Task>,
    private val onTaskUpdated: ((Task) -> Unit)? = null,   // optional callback for updates
    private val onTaskDeleted: ((Task) -> Unit)? = null    // optional callback for deletes
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    inner class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.findViewById(R.id.tvTaskTitle)
        val tvDesc: TextView = itemView.findViewById(R.id.tvTaskDescription)
        val cbDone: CheckBox = itemView.findViewById(R.id.cbCompleted)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_task, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = tasks[position]
        holder.tvTitle.text = task.title
        holder.tvDesc.text = task.description

        holder.cbDone.setOnCheckedChangeListener(null)
        holder.cbDone.isChecked = task.completed

        // Update completed state if callback is provided
        holder.cbDone.setOnCheckedChangeListener { _, isChecked ->
            task.completed = isChecked
            onTaskUpdated?.invoke(task)
        }

        // Only allow long-press edit/delete if callbacks exist (e.g., in TaskListActivity)
        if (onTaskUpdated != null || onTaskDeleted != null) {
            holder.itemView.setOnLongClickListener {
                val context = holder.itemView.context
                val options = mutableListOf<String>()
                if (onTaskUpdated != null) options.add("Edit")
                if (onTaskDeleted != null) options.add("Delete")

                androidx.appcompat.app.AlertDialog.Builder(context)
                    .setTitle("Task Options")
                    .setItems(options.toTypedArray()) { _, which ->
                        when (options[which]) {
                            "Edit" -> showEditDialog(context, task)
                            "Delete" -> showDeleteConfirmation(context, task)
                        }
                    }
                    .show()
                true
            }
        }
    }

    private fun showEditDialog(context: Context, task: Task) {
        val linear = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 20, 40, 10)
        }

        val titleInput = EditText(context).apply {
            hint = "Task title"
            setText(task.title)
        }

        val descInput = EditText(context).apply {
            hint = "Description"
            setText(task.description)
        }

        linear.addView(titleInput)
        linear.addView(descInput)

        androidx.appcompat.app.AlertDialog.Builder(context)
            .setTitle("Edit Task")
            .setView(linear)
            .setPositiveButton("Save") { _, _ ->
                val newTitle = titleInput.text.toString().trim()
                val newDesc = descInput.text.toString().trim()
                if (newTitle.isNotEmpty()) {
                    task.title = newTitle
                    task.description = newDesc
                    onTaskUpdated?.invoke(task)
                    notifyDataSetChanged()
                    Toast.makeText(context, "Task updated!", Toast.LENGTH_SHORT).show() // confirmation toast
                } else {
                    Toast.makeText(context, "Title cannot be empty", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showDeleteConfirmation(context: Context, task: Task) {
        androidx.appcompat.app.AlertDialog.Builder(context)
            .setTitle("Delete Task")
            .setMessage("Are you sure you want to delete this task?")
            .setPositiveButton("Delete") { _, _ ->
                onTaskDeleted?.invoke(task)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun getItemCount(): Int = tasks.size

    fun addTask(task: Task) {
        tasks.add(0, task)
        notifyItemInserted(0)
    }

    fun updateTasks(newTasks: List<Task>) {
        tasks.clear()
        tasks.addAll(newTasks)
        notifyDataSetChanged()
    }
}
