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
import androidx.appcompat.app.AlertDialog

class TaskAdapter(
    private val tasks: MutableList<Task>,
    private val onTaskUpdated: ((Task) -> Unit)? = null,
    private val onTaskDeleted: ((Task) -> Unit)? = null
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

        // Toggle completion (optimistic update). Activity will call the API and refresh on success/failure.
        holder.cbDone.setOnCheckedChangeListener { _, isChecked ->
            task.completed = isChecked
            onTaskUpdated?.invoke(task)
            // update this row immediately
            notifyItemChanged(holder.adapterPosition)
        }

        holder.itemView.setOnLongClickListener {
            showTaskOptions(holder.itemView.context, task, holder.adapterPosition)
            true
        }
    }

    private fun showTaskOptions(context: Context, task: Task, position: Int) {
        val options = mutableListOf<String>()
        if (onTaskUpdated != null) options.add("Edit")
        if (onTaskDeleted != null) options.add("Delete")

        AlertDialog.Builder(context)
            .setTitle("Task Options")
            .setItems(options.toTypedArray()) { _, which ->
                when (options[which]) {
                    "Edit" -> showEditDialog(context, task, position)
                    "Delete" -> showDeleteConfirmation(context, task, position)
                }
            }
            .show()
    }

    private fun showEditDialog(context: Context, task: Task, position: Int) {
        val layout = LinearLayout(context).apply {
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

        layout.addView(titleInput)
        layout.addView(descInput)

        AlertDialog.Builder(context)
            .setTitle("Edit Task")
            .setView(layout)
            .setPositiveButton("Save") { _, _ ->
                val newTitle = titleInput.text.toString().trim()
                val newDesc = descInput.text.toString().trim()
                if (newTitle.isNotEmpty()) {
                    // Update local object first (optimistic UI). Activity will persist.
                    task.title = newTitle
                    task.description = newDesc
                    onTaskUpdated?.invoke(task)
                    notifyItemChanged(position)
                } else {
                    Toast.makeText(context, "Title cannot be empty", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showDeleteConfirmation(context: Context, task: Task, position: Int) {
        AlertDialog.Builder(context)
            .setTitle("Delete Task")
            .setMessage("Are you sure you want to delete this task?")
            .setPositiveButton("Delete") { _, _ ->
                // Do NOT remove the item locally here — wait for activity/server confirmation.
                onTaskDeleted?.invoke(task)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun getItemCount(): Int = tasks.size

    // Helpers for Activity to update the adapter after server responses:
    fun replaceAll(newList: List<Task>) {
        tasks.clear()
        tasks.addAll(newList)
        notifyDataSetChanged()
    }

    fun removeTaskById(taskId: String) {
        val index = tasks.indexOfFirst { it.id == taskId }
        if (index >= 0) {
            tasks.removeAt(index)
            notifyItemRemoved(index)
        }
    }

    fun updateTaskById(updated: Task) {
        val index = tasks.indexOfFirst { it.id == updated.id }
        if (index >= 0) {
            tasks[index] = updated
            notifyItemChanged(index)
        }
    }
}
