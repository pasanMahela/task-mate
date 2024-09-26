package com.example.taskmate2

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Button

class OnboardingFragment3 : Fragment(R.layout.fragment_onboarding3) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Handle back button to go to previous fragment (OnboardingFragment2)
        view.findViewById<Button>(R.id.back_button).setOnClickListener {
            (activity as? Onboarding)?.viewPager?.currentItem = 1
        }

        // Handle get started button to navigate to the main activity
        view.findViewById<Button>(R.id.get_started_button).setOnClickListener {
            val intent = Intent(activity, SignupActivity::class.java)
            startActivity(intent)
            activity?.finish() // Finish the onboarding activity to prevent going back to it
        }
    }
}

