package com.example.taskmaster

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest

class SettingsActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
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

        // Pre-fill the current display name
        etDisplayName.setText(user?.displayName ?: "")

        // Save button click
        btnSave.setOnClickListener {
            val newName = etDisplayName.text.toString().trim()
            if (newName.isNotEmpty()) {
                updateDisplayName(newName)
            } else {
                Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show()
            }
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
