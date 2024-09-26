package com.example.taskmate2

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Button

class OnboardingFragment2 : Fragment(R.layout.fragment_onboarding2) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)  // Pass savedInstanceState to super correctly

        view.findViewById<Button>(R.id.back_button).setOnClickListener {
            (activity as? Onboarding)?.viewPager?.currentItem = 0
        }

        view.findViewById<Button>(R.id.next_button).setOnClickListener {
            (activity as? Onboarding)?.viewPager?.currentItem = 2
        }
    }
}
