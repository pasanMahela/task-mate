package com.example.taskmate2

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment

class TimerFragment : Fragment() {

    private lateinit var timerTextView: TextView
    private lateinit var startButton: Button
    private lateinit var pauseButton: Button
    private lateinit var resetButton: Button

    private var timerRunning = false
    private var timeInSeconds = 0L
    private var handler = Handler(Looper.getMainLooper())
    private var timerRunnable: Runnable? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_timer, container, false)

        timerTextView = view.findViewById(R.id.timer_text)
        startButton = view.findViewById(R.id.btn_start)
        pauseButton = view.findViewById(R.id.btn_pause)
        resetButton = view.findViewById(R.id.btn_reset)

        setupButtons()

        return view
    }

    private fun setupButtons() {
        startButton.setOnClickListener {
            if (!timerRunning) {
                startTimer()
            }
        }

        pauseButton.setOnClickListener {
            pauseTimer()
        }

        resetButton.setOnClickListener {
            resetTimer()
        }
    }

    private fun startTimer() {
        timerRunning = true
        timerTextView.animate().scaleX(1.2f).scaleY(1.2f).setDuration(300).withEndAction {
            timerTextView.animate().scaleX(1f).scaleY(1f).setDuration(300).start()
        }
        timerRunnable = object : Runnable {
            override fun run() {
                timeInSeconds++
                updateTimerText()
                handler.postDelayed(this, 1000)
            }
        }
        handler.post(timerRunnable!!)
    }

    private fun pauseTimer() {
        if (timerRunning) {
            timerRunning = false
            handler.removeCallbacks(timerRunnable!!)
            timerTextView.animate().scaleX(0.8f).scaleY(0.8f).setDuration(300).withEndAction {
                timerTextView.animate().scaleX(1f).scaleY(1f).setDuration(300).start()
            }
        }
    }

    private fun resetTimer() {
        pauseTimer()
        timeInSeconds = 0L
        updateTimerText()
    }

    private fun updateTimerText() {
        val hours = timeInSeconds / 3600
        val minutes = (timeInSeconds % 3600) / 60
        val seconds = timeInSeconds % 60
        timerTextView.text = String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }
}

