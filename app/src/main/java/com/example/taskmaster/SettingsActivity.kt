package com.example.taskmaster

import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest

class SettingsActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {

        // --- Load saved theme first ---
        prefs = getSharedPreferences("app_settings", MODE_PRIVATE)
        val isDarkMode = prefs.getBoolean("dark_mode", false)
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        auth = FirebaseAuth.getInstance()
        val user = auth.currentUser

        // Enable the back button in the ActionBar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Settings"

        // --- Views ---
        val etDisplayName = findViewById<EditText>(R.id.etDisplayName)
        val btnSave = findViewById<Button>(R.id.btnSave)
        val switchDarkMode = findViewById<Switch>(R.id.switchDarkMode)
        val switchShowCompleted = findViewById<Switch>(R.id.switchShowCompleted)
        val switchAutoSort = findViewById<Switch>(R.id.switchAutoSort)

        // Pre-fill the current display name
        etDisplayName.setText(user?.displayName ?: "")

        // Set switches based on saved preferences
        switchDarkMode.isChecked = isDarkMode
        switchShowCompleted.isChecked = prefs.getBoolean("show_completed", true)
        switchAutoSort.isChecked = prefs.getBoolean("auto_sort", true)

        // --- Save display name ---
        btnSave.setOnClickListener {
            val newName = etDisplayName.text.toString().trim()
            if (newName.isNotEmpty()) {
                updateDisplayName(newName)
            } else {
                Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show()
            }
        }

        // --- Dark mode toggle ---
        switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("dark_mode", isChecked).apply()
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }

        // --- Show Completed Tasks toggle ---
        switchShowCompleted.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("show_completed", isChecked).apply()
        }

        // --- Auto-Sort Tasks toggle ---
        switchAutoSort.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("auto_sort", isChecked).apply()
        }
    }

    private fun updateDisplayName(name: String) {
        val user = auth.currentUser ?: return

        val profileUpdates = UserProfileChangeRequest.Builder()
            .setDisplayName(name)
            .build()

        user.updateProfile(profileUpdates)
            .addOnSuccessListener {
                Toast.makeText(this, "Display name updated!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to update: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // Handle ActionBar back button click
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
