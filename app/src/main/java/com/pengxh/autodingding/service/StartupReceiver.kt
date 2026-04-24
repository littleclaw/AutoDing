package com.pengxh.autodingding.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.pengxh.autodingding.health.PushRuntimeManager

class StartupReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            Intent.ACTION_LOCKED_BOOT_COMPLETED -> PushRuntimeManager.bootstrap(context.applicationContext)
        }
    }
}
