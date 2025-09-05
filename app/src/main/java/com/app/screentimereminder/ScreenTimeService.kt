package com.app.screentimereminder

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat

class ScreenTimeService : Service() {
    private val CHANNEL_ID = "ScreenTimeChannel"
    private val TAG = "ScreenTimeService"
    override fun onBind(p0: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()

        // Foreground notification (pinned in shutter)
        val notif = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Screen Time Tracker")
            .setContentText("Tracking started...")
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .build()

        startForeground(1, notif)

        // Background thread for timer
        Thread {
            var counter = 0
            while (true) {
                counter++
                Log.d(TAG, "Tick: $counter second(s) elapsed")

                if (counter % 30 == 0) { // every 30 seconds
                    sendTestNotification(counter)
                }

                Thread.sleep(1000) // tick every 1 sec
            }
        }.start()
    }

    private fun sendTestNotification(seconds: Int) {
        val notif = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("⏱ Screen Time Reminder")
            .setContentText("Service running for $seconds seconds.")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(false)
            .build()

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(2, notif) // same ID -> updates same notification

        Log.d(TAG, "Notification sent → $seconds seconds elapsed")
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Screen Time Tracker",
                NotificationManager.IMPORTANCE_HIGH
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

//    override fun onBind(p0: Intent?): IBinder? = null
}
