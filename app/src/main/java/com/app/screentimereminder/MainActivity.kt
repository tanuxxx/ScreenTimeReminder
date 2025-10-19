package com.app.screentimereminder

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : ComponentActivity() {

    private val handler = Handler(Looper.getMainLooper())
    private val updateInterval = 5000L // every 5 seconds

    private val updater = object : Runnable {
        override fun run() {
            handler.postDelayed(this, updateInterval)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        checkUsageAccessPermission()
        val startButton: Button = findViewById(R.id.startButton)
        val pauseButton: Button = findViewById(R.id.pauseButton)
        val stopButton: Button = findViewById(R.id.stopButton)

        // Ask for notification permission on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    1
                )
            }
        }

        startButton.setOnClickListener {
            val intent = Intent(this, ScreenTimeService::class.java).apply {
                action = ScreenTimeService.ACTION_START
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent)
            } else {
                startService(intent)
            }
            handler.post(updater)
        }

        pauseButton.setOnClickListener {
            val intent = Intent(this, ScreenTimeService::class.java).apply {
                action = ScreenTimeService.ACTION_PAUSE
            }
            startService(intent)
            handler.removeCallbacks(updater)
        }

        stopButton.setOnClickListener {
            val intent = Intent(this, ScreenTimeService::class.java).apply {
                action = ScreenTimeService.ACTION_STOP
            }
            startService(intent)
            handler.removeCallbacks(updater)
        }
    }

    override fun onDestroy() {
        handler.removeCallbacks(updater)
        super.onDestroy()
    }
    private fun checkUsageAccessPermission() {
        val appOps = getSystemService(APP_OPS_SERVICE) as android.app.AppOpsManager
        val mode = appOps.checkOpNoThrow(
            "android:get_usage_stats",
            android.os.Process.myUid(),
            packageName
        )
        if (mode != android.app.AppOpsManager.MODE_ALLOWED) {
            val intent = Intent(android.provider.Settings.ACTION_USAGE_ACCESS_SETTINGS)
            startActivity(intent) // user must enable manually
        }
    }

}
