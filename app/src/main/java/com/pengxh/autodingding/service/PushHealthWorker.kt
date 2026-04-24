package com.pengxh.autodingding.service

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.work.Worker
import androidx.work.WorkerParameters
import cn.jpush.android.api.JPushInterface
import com.pengxh.autodingding.health.DeviceHealthStatus
import com.pengxh.autodingding.health.PushHealthStore
import com.pengxh.autodingding.health.PushRuntimeManager

class PushHealthWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {
    override fun doWork(): Result {
        PushRuntimeManager.refreshSetupState(applicationContext)
        val hasNetwork = isNetworkAvailable(applicationContext)
        val regId = PushRuntimeManager.syncRegistration(applicationContext)
        val isConnected = JPushInterface.getConnectionState(applicationContext)
        if (!hasNetwork || regId.isBlank() || !isConnected || JPushInterface.isPushStopped(applicationContext)) {
            PushRuntimeManager.recoverPushRuntime(
                applicationContext,
                "health_worker network=$hasNetwork regId=${regId.isNotBlank()} connected=$isConnected"
            )
        }
        val status = PushRuntimeManager.refreshLocalStatus()
        if (status == DeviceHealthStatus.OFFLINE && hasNetwork) {
            PushRuntimeManager.recoverPushRuntime(applicationContext, "local_status_offline")
            PushRuntimeManager.refreshLocalStatus()
        }
        return Result.success()
    }

    private fun isNetworkAvailable(context: Context): Boolean {
        val manager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = manager.activeNetwork ?: return false
        val capabilities = manager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}
