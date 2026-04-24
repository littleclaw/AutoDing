package com.pengxh.autodingding.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.pengxh.autodingding.R
import com.pengxh.autodingding.health.PushRuntimeManager
import com.pengxh.autodingding.ui.MainActivity

class KeepOnService : Service() {
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notificationManager: NotificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "keepService"
        val notificationChannel = NotificationChannel(channelId, "keepOnService", NotificationManager.IMPORTANCE_LOW)
        notificationManager.createNotificationChannel(notificationChannel)
        PushRuntimeManager.refreshSetupState(applicationContext)
        PushRuntimeManager.refreshLocalStatus()
        val contentIntent = PendingIntent.getActivity(
            this,
            1,
            Intent(this, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val builder = NotificationCompat.Builder(this, channelId)
        builder.run {
            setContentTitle(getString(R.string.fore_service))
            setContentText(PushRuntimeManager.buildKeepAliveNotificationText())
            setChannelId(channelId)
            setSmallIcon(R.mipmap.logo_round)
            setOngoing(true)
            setContentIntent(contentIntent)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ServiceCompat.startForeground(this, 1, builder.build(), ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK)
        } else {
            startForeground(1, builder.build())
        }
        return START_STICKY
    }
}
