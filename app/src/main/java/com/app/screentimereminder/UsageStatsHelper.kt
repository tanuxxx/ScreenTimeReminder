package com.app.screentimereminder

import android.app.usage.UsageStatsManager
import android.content.Context
import android.os.Build

object UsageStatsHelper {

    fun getTotalScreenTime(context: Context): Long {
        val usm = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val endTime = System.currentTimeMillis()

        // Start of today
        val cal = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }
        val startTime = cal.timeInMillis

        // Query daily stats
        val stats = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            usm.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY,
                startTime,
                endTime
            )
        } else {
            usm.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY,
                startTime,
                endTime
            )
        }

        var totalTime: Long = 0
        stats?.forEach {
            totalTime += it.totalTimeInForeground
        }
        return totalTime
    }
}
