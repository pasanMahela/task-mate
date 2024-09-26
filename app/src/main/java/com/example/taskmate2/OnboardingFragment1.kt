package com.example.taskmate2

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.fragment.app.Fragment

class OnboardingFragment1 : Fragment(R.layout.fragment_onboarding1) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<Button>(R.id.skip_button).setOnClickListener {
            val intent = Intent(activity, SignupActivity::class.java)
            startActivity(intent)
            activity?.finish()
        }

        view.findViewById<Button>(R.id.next_button).setOnClickListener {
            Log.d("OnboardingFragment1", "Next button clicked")
            // Check if activity is not null and cast it to Onboarding
            (activity as? Onboarding)?.let { onboardingActivity ->
                onboardingActivity.viewPager.currentItem = 1 // Navigate to the next fragment
            } ?: run {
                Log.e("OnboardingFragment1", "Activity is null or not of type Onboarding")
            }
        }
    }
}

