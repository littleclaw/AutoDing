package com.pengxh.autodingding.service

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.pengxh.autodingding.health.DeviceHealthStatus
import com.pengxh.autodingding.health.PushHealthStore
import com.pengxh.autodingding.health.PushRuntimeManager
import com.pengxh.autodingding.net.RetrofitManager
import com.pengxh.autodingding.net.api.PushApi
import kotlinx.coroutines.runBlocking

class TargetHeartbeatWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {
    override fun doWork(): Result {
        val targetRegId = PushHealthStore.getTargetRegId()
        if (targetRegId.isBlank()) {
            return Result.success()
        }
        val response = runBlocking {
            PushApiHelper.pushCommand(applicationContext, targetRegId, PushReceiver.MSG_HEALTH_PING)
        }
        return if (response?.errorCode == 0) {
            PushHealthStore.markHeartbeatPingSent(targetRegId)
            PushHealthStore.updateTargetStatus(DeviceHealthStatus.DEGRADED)
            PushRuntimeManager.scheduleHeartbeatTimeout(applicationContext)
            Result.success()
        } else {
            Result.retry()
        }
    }
}
