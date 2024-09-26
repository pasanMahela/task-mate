package com.example.taskmate2

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val loginButton = findViewById<Button>(R.id.login_button)
        val mobileNumberEditText = findViewById<EditText>(R.id.username)
        val passwordEditText = findViewById<EditText>(R.id.password)

        loginButton.setOnClickListener {
            val mobileNumber = mobileNumberEditText.text.toString()
            val password = passwordEditText.text.toString()

            // Retrieve SharedPreferences data
            val sharedPref = getSharedPreferences("UserPref", Context.MODE_PRIVATE)
            val savedMobileNumber = sharedPref.getString("mobileNumber", null)
            val savedPassword = sharedPref.getString("password", null)

            // Verify mobile number and password
            if (mobileNumber == savedMobileNumber && password == savedPassword) {
                // Start the MainActivity after successful login
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Invalid credentials", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
