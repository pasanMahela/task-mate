package com.example.taskmate2

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment

class ProfileFragment : Fragment() {

    private lateinit var helloText: TextView
    private lateinit var mobileNumberText: TextView
    private lateinit var changePasswordButton: Button
    private lateinit var aboutUsButton: Button
    private lateinit var signOutButton: Button
    private lateinit var deleteAccountButton: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        helloText = view.findViewById(R.id.hello_text)
        mobileNumberText = view.findViewById(R.id.mobile_number_text)
        changePasswordButton = view.findViewById(R.id.change_password_button)
        aboutUsButton = view.findViewById(R.id.about_us_button)
        signOutButton = view.findViewById(R.id.sign_out_button)
        deleteAccountButton = view.findViewById(R.id.delete_account_button)

        val sharedPref = requireContext().getSharedPreferences("UserPref", Context.MODE_PRIVATE)
        val firstName = sharedPref.getString("firstName", "User") ?: "User"
        val mobileNumber = sharedPref.getString("mobileNumber", "Not Available") ?: "Not Available"

        helloText.text = "Hello, $firstName"
        mobileNumberText.text = mobileNumber

        changePasswordButton.setOnClickListener {
            showChangePasswordDialog(sharedPref)
        }

        aboutUsButton.setOnClickListener {
            val description = "TaskMate is a productivity app that helps you manage tasks effectively."
            Toast.makeText(requireContext(), description, Toast.LENGTH_LONG).show()
        }

        signOutButton.setOnClickListener {
            startActivity(Intent(requireContext(), LoginActivity::class.java))
            activity?.finish()
        }

        deleteAccountButton.setOnClickListener {
            sharedPref.edit().clear().apply()
            startActivity(Intent(requireContext(), SignupActivity::class.java))
            requireActivity().finish()
        }


        return view
    }

    private fun showChangePasswordDialog(sharedPref: SharedPreferences) {
        val builder = AlertDialog.Builder(requireContext())
        val dialogView = layoutInflater.inflate(R.layout.dialog_change_password, null)
        val newPasswordEditText = dialogView.findViewById<EditText>(R.id.new_password_edit_text)

        builder.setView(dialogView)
            .setTitle("Change Password")
            .setPositiveButton("Change") { dialog, _ ->
                val newPassword = newPasswordEditText.text.toString()
                if (newPassword.isNotEmpty()) {
                    with(sharedPref.edit()) {
                        putString("password", newPassword)
                        apply()
                    }
                    Toast.makeText(requireContext(), "Password changed successfully", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Password cannot be empty", Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
            .create()
            .show()
    }
}
