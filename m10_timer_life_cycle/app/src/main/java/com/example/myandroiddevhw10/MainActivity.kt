package com.example.myandroiddevhw10

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myandroiddevhw10.databinding.ActivityMainBinding



class MainActivity : AppCompatActivity() {
    private var handler = Handler(Looper.getMainLooper())

    private var timerStartValue = 10
    private var countDownTimer = 10
    private var timerIsActive = false

    private lateinit var timerThread: Thread
    private var timerThreadStop = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState != null) {
            timerStartValue = savedInstanceState.getInt("timerValue")
            timerIsActive = savedInstanceState.getBoolean("timerIsActive")
            Log.d("msg", "onCreate: после изменения $timerStartValue")
            countDownTimer = timerStartValue
            handler.post {
                updateTimer(binding)
                if (timerIsActive) startTimer(binding)
            }
        }

        handler.post {
            binding.sliderTimer.addOnChangeListener { _, value, _ ->
                timerStartValue = value.toInt()
                countDownTimer = timerStartValue
                updateUI(binding)
            }

            binding.buttonStart.setOnClickListener {
                startTimer(binding)
            }
        }
    }

    private fun updateUI(binding: ActivityMainBinding) {
        when (timerIsActive) {
            true -> {
                binding.sliderTimer.isEnabled = false
                binding.buttonStart.visibility = View.GONE
                binding.buttonStop.visibility = View.VISIBLE
            }

            false -> {
                binding.sliderTimer.isEnabled = true
                binding.buttonStop.visibility = View.GONE
                binding.buttonStart.visibility = View.VISIBLE
            }
        }
        binding.progressBar.max = binding.sliderTimer.value.toInt()
        binding.progressBar.progress = countDownTimer
        binding.progressTimer.text = timerStartValue.toString()
    }

    private fun updateTimer(binding: ActivityMainBinding) {
        binding.progressTimer.text = countDownTimer.toString()
        binding.progressBar.progress = countDownTimer
    }

    private fun startTimer(binding: ActivityMainBinding) {
        timerThread = Thread {
            while (!timerThreadStop && countDownTimer > 0) {
                try {
                    Thread.sleep(1000)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }

                binding.buttonStop.setOnClickListener {
                    timerIsActive = false
                    stopTimerThread()
                }

                countDownTimer--

                handler.post {
                    updateTimer(binding)
                }
            }
            if (countDownTimer == 0) timerIsActive = false
            handler.post { finishTimer(binding) }
        }

        timerIsActive = true
        handler.post { updateUI(binding) }
        timerThread.start()
    }

    private fun finishTimer(binding: ActivityMainBinding) {
        if (!timerIsActive) {
            Toast.makeText(this, "Timer Task Finished", Toast.LENGTH_SHORT).show()
        }
        timerStartValue = binding.sliderTimer.value.toInt()
        countDownTimer = timerStartValue
        timerThreadStop = false
        handler.post { updateUI(binding) }
    }

    private fun stopTimerThread() {
        timerThreadStop = true
        timerThread.interrupt()
    }

    override fun onPause() {
        super.onPause()
        try {
            stopTimerThread()
        } catch (ex: Exception) {
            Log.d("msg", "onPause: $ex")
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt("timerValue", countDownTimer)
        outState.putBoolean("timerIsActive", timerIsActive)
        super.onSaveInstanceState(outState)
    }
}