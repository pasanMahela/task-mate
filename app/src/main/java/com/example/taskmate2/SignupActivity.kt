package com.example.taskmate2

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class SignupActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        val signupButton = findViewById<Button>(R.id.signup_button)
        val firstNameEditText = findViewById<EditText>(R.id.first_name)
        val mobileNumberEditText = findViewById<EditText>(R.id.username)
        val passwordEditText = findViewById<EditText>(R.id.password)

        signupButton.setOnClickListener {
            val firstName = firstNameEditText.text.toString()
            val mobileNumber = mobileNumberEditText.text.toString()
            val password = passwordEditText.text.toString()

            // Save user info in SharedPreferences
            val sharedPref = getSharedPreferences("UserPref", Context.MODE_PRIVATE)
            with(sharedPref.edit()) {
                putString("firstName", firstName)
                putString("mobileNumber", mobileNumber)
                putString("password", password)
                putBoolean("isRegistered", true)
                apply()
            }

            // Start the MainActivity after registration
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
