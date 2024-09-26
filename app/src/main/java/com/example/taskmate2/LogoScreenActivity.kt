package com.example.taskmate2

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class LogoScreenActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_logo_screen) // Link to the XML layout

        // Retrieve SharedPreferences
        val sharedPref = getSharedPreferences("UserPref", Context.MODE_PRIVATE)

        // Check if user is registered
        val isRegistered = sharedPref.getBoolean("isRegistered", false)

        if (isRegistered) {
            // If registered, navigate to login screen
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        } else {
            // If not registered, navigate to registration screen
            val intent = Intent(this, Onboarding::class.java)
            startActivity(intent)
        }

        // Close the current activity to prevent navigating back to the splash screen
        finish()
    }
}
