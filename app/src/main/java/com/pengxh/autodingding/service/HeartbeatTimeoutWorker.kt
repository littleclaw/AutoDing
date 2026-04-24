package com.pengxh.autodingding.service

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.pengxh.autodingding.health.DeviceHealthStatus
import com.pengxh.autodingding.health.PushHealthStore

class HeartbeatTimeoutWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {
    override fun doWork(): Result {
        val snapshot = PushHealthStore.getTargetSnapshot()
        if (snapshot.targetRegId.isBlank() || snapshot.lastHeartbeatPingSentAt <= 0L) {
            return Result.success()
        }
        if (snapshot.lastHeartbeatAckReceivedAt >= snapshot.lastHeartbeatPingSentAt) {
            PushHealthStore.updateTargetStatus(DeviceHealthStatus.ONLINE)
            return Result.success()
        }
        val missed = PushHealthStore.incrementMissedHeartbeat()
        val status = if (missed >= 2) DeviceHealthStatus.OFFLINE else DeviceHealthStatus.DEGRADED
        PushHealthStore.updateTargetStatus(status)
        return Result.success()
    }
}
