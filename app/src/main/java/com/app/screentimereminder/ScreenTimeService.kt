package com.app.screentimereminder

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import java.util.Calendar

class ScreenTimeService : Service() {

    companion object {
        const val ACTION_START = "ACTION_START"
        const val ACTION_PAUSE = "ACTION_PAUSE"
        const val ACTION_STOP  = "ACTION_STOP"
        private const val CHANNEL_ID = "screen_time_channel"
        private const val NOTIF_ID   = 1001
    }

    private val handler = Handler(Looper.getMainLooper())
    private val intervalMillis = 15_000L // 15 seconds
    private var isRunning = false
    private var trackedSeconds = 0L      // <- we count seconds now

    private val updater = object : Runnable {
        override fun run() {
            if (!isRunning) return

            // increment by 15 seconds each tick
            trackedSeconds += intervalMillis / 1000L

            // update notification with TOTAL SECONDS
            updateNotification(trackedSeconds)

            // play reminder sound (optional; you may want this only every few minutes)
            playRingtone()

            // schedule next tick
            handler.postDelayed(this, intervalMillis)

            // auto-stop at end of day (23:59)
            val now = Calendar.getInstance()
            if (now.get(Calendar.HOUR_OF_DAY) == 23 && now.get(Calendar.MINUTE) == 59) {
                stopTracking()
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        createChannelIfNeeded()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> startTracking()
            ACTION_PAUSE -> pauseTracking()
            ACTION_STOP  -> stopTracking()
            // no default: don’t auto-start without intent action
        }
        return START_STICKY
    }

    override fun onDestroy() {
        handler.removeCallbacks(updater)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun startTracking() {
        if (isRunning) return
        isRunning = true
        trackedSeconds = 0L // reset on fresh start; remove this line if you want resume semantics
        startForeground(NOTIF_ID, buildNotification(trackedSeconds))
        handler.post(updater)
    }

    private fun pauseTracking() {
        isRunning = false
        handler.removeCallbacks(updater)
        // keep trackedSeconds so resume continues from where it paused
    }

    private fun stopTracking() {
        isRunning = false
        handler.removeCallbacks(updater)
        stopForeground(true)
        stopSelf()
    }

    private fun createChannelIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Screen Time Tracking",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                .createNotificationChannel(channel)
        }
    }

    private fun buildNotification(totalSeconds: Long): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Screen Time Reminder")
            .setContentText("You've used your phone for $totalSeconds second(s)")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setOngoing(true)
            .build()
    }

    private fun updateNotification(totalSeconds: Long) {
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(NOTIF_ID, buildNotification(totalSeconds))
    }

    private fun playRingtone() {
        try {
            val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            RingtoneManager.getRingtone(applicationContext, uri)?.play()
        } catch (_: Exception) { /* ignore */ }
    }
}
