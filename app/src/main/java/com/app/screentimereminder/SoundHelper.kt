package com.app.screentimereminder

import android.content.Context
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri

object SoundHelper {
    private var ringtone: Ringtone? = null

    fun playNotificationSound(context: Context) {
        try {
            val uri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            ringtone = RingtoneManager.getRingtone(context, uri)
            ringtone?.play()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun stopSound() {
        ringtone?.stop()
    }
}
