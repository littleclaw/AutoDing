package com.pengxh.autodingding.health

import android.app.ActivityManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import cn.jpush.android.api.JPushInterface
import com.blankj.utilcode.util.CacheDiskUtils
import com.pengxh.autodingding.service.BaseAccessibilityService
import com.pengxh.autodingding.service.HeartbeatTimeoutWorker
import com.pengxh.autodingding.service.KeepOnService
import com.pengxh.autodingding.service.NotificationMonitorService
import com.pengxh.autodingding.service.PushHealthWorker
import com.pengxh.autodingding.service.TargetHeartbeatWorker
import com.pengxh.autodingding.service.WakeUpWorker
import com.pengxh.autodingding.utils.AccessibilityUtil
import com.pengxh.autodingding.utils.RomUtils
import java.util.concurrent.TimeUnit

object PushRuntimeManager {
    private const val PUSH_TAG = "main"
    private const val WORK_WAKE = "wakeAndLog"
    private const val WORK_PUSH_HEALTH = "pushHealth"
    private const val WORK_TARGET_HEARTBEAT = "targetHeartbeat"
    private const val WORK_HEARTBEAT_TIMEOUT = "heartbeatTimeout"

    fun bootstrap(context: Context) {
        syncRegistration(context)
        initOrResumePush(context)
        refreshSetupState(context)
        refreshLocalStatus()
        ensureKeepAliveService(context)
        enqueuePeriodicWork(context)
    }

    fun enqueuePeriodicWork(context: Context) {
        val workManager = WorkManager.getInstance(context)
        workManager.enqueueUniquePeriodicWork(
            WORK_WAKE,
            ExistingPeriodicWorkPolicy.UPDATE,
            PeriodicWorkRequestBuilder<WakeUpWorker>(3, TimeUnit.HOURS, 5, TimeUnit.MINUTES).build()
        )
        workManager.enqueueUniquePeriodicWork(
            WORK_PUSH_HEALTH,
            ExistingPeriodicWorkPolicy.UPDATE,
            PeriodicWorkRequestBuilder<PushHealthWorker>(15, TimeUnit.MINUTES).build()
        )
        workManager.enqueueUniquePeriodicWork(
            WORK_TARGET_HEARTBEAT,
            ExistingPeriodicWorkPolicy.UPDATE,
            PeriodicWorkRequestBuilder<TargetHeartbeatWorker>(15, TimeUnit.MINUTES).build()
        )
    }

    fun scheduleHeartbeatTimeout(context: Context) {
        val request = OneTimeWorkRequestBuilder<HeartbeatTimeoutWorker>()
            .setInitialDelay(2, TimeUnit.MINUTES)
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork(
            WORK_HEARTBEAT_TIMEOUT,
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

    fun ensureKeepAliveService(context: Context) {
        val intent = Intent(context, KeepOnService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ContextCompat.startForegroundService(context, intent)
        } else {
            context.startService(intent)
        }
    }

    fun initOrResumePush(context: Context) {
        JPushInterface.init(context)
        JPushInterface.resumePush(context)
        JPushInterface.setTags(context, 0, hashSetOf(PUSH_TAG))
        PushHealthStore.markJPushInitialized()
        syncRegistration(context)
    }

    fun syncRegistration(context: Context): String {
        val regId = JPushInterface.getRegistrationID(context).orEmpty()
        CacheDiskUtils.getInstance().put("pushRegId", regId)
        PushHealthStore.updateRegistrationId(regId)
        return regId
    }

    fun recoverPushRuntime(context: Context, reason: String) {
        PushHealthStore.markRecoveryAttempt(reason)
        initOrResumePush(context)
        ensureKeepAliveService(context)
        rebindNotificationMonitor(context)
        refreshSetupState(context)
        refreshLocalStatus()
    }

    fun refreshSetupState(context: Context): PushHealthStore.SetupState {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val setupState = PushHealthStore.SetupState(
            keepAliveEnabled = isKeepAliveServiceRunning(context),
            notificationListenerEnabled = NotificationManagerCompat.getEnabledListenerPackages(context).contains(context.packageName),
            accessibilityEnabled = AccessibilityUtil.isServiceOn(context, BaseAccessibilityService::class.java.canonicalName ?: ""),
            batteryOptimizationIgnored = powerManager.isIgnoringBatteryOptimizations(context.packageName),
            backgroundStartAllowed = RomUtils.isBackgroundStartAllowed(context)
        )
        PushHealthStore.updateSetupState(setupState)
        return setupState
    }

    fun refreshLocalStatus(): DeviceHealthStatus {
        val status = PushHealthStore.deriveLocalStatus()
        PushHealthStore.updateLocalStatus(status)
        return status
    }

    fun rebindNotificationMonitor(context: Context) {
        val packageManager = context.packageManager
        val componentName = ComponentName(context, NotificationMonitorService::class.java)
        packageManager.setComponentEnabledSetting(
            componentName,
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
            PackageManager.DONT_KILL_APP
        )
        packageManager.setComponentEnabledSetting(
            componentName,
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            PackageManager.DONT_KILL_APP
        )
    }

    fun isKeepAliveServiceRunning(context: Context): Boolean {
        val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        @Suppress("DEPRECATION")
        val runningServices = manager.getRunningServices(Int.MAX_VALUE)
        return runningServices.any { it.service.className == KeepOnService::class.java.name }
    }

    fun buildKeepAliveNotificationText(): String {
        val local = PushHealthStore.getLocalSnapshot()
        val regIdState = if (local.regId.isBlank()) "未获取" else "已就绪"
        return "状态：${PushHealthStore.describeStatus(local.status)}  注册ID：$regIdState"
    }

    fun openBatteryOptimizationSettings(context: Context) {
        val packageUri = Uri.parse("package:${context.packageName}")
        val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, packageUri)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            context.startActivity(intent)
        } catch (_: Exception) {
            context.startActivity(
                Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        }
    }
}
