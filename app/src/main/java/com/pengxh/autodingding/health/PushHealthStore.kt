package com.pengxh.autodingding.health

import android.text.format.DateFormat
import com.blankj.utilcode.util.SPUtils
import java.util.Date

object PushHealthStore {
    private const val SP_NAME = "push_health"
    private const val KEY_CURRENT_REG_ID = "current_reg_id"
    private const val KEY_LAST_JPUSH_INIT_AT = "last_jpush_init_at"
    private const val KEY_LAST_REGISTER_AT = "last_register_at"
    private const val KEY_LAST_INBOUND_COMMAND_AT = "last_inbound_command_at"
    private const val KEY_LAST_HEARTBEAT_PING_RECEIVED_AT = "last_heartbeat_ping_received_at"
    private const val KEY_LAST_HEARTBEAT_PING_SENT_AT = "last_heartbeat_ping_sent_at"
    private const val KEY_LAST_HEARTBEAT_ACK_RECEIVED_AT = "last_heartbeat_ack_received_at"
    private const val KEY_LAST_RECOVERY_ATTEMPT_AT = "last_recovery_attempt_at"
    private const val KEY_LAST_RECOVERY_REASON = "last_recovery_reason"
    private const val KEY_CONSECUTIVE_MISSED_HEARTBEATS = "consecutive_missed_heartbeats"
    private const val KEY_LAST_HEARTBEAT_SENDER_REG_ID = "last_heartbeat_sender_reg_id"
    private const val KEY_LAST_ACK_FROM_REG_ID = "last_ack_from_reg_id"
    private const val KEY_TARGET_REG_ID = "target_reg_id"
    private const val KEY_TARGET_STATUS = "target_status"
    private const val KEY_LOCAL_KEEP_ALIVE_ENABLED = "local_keep_alive_enabled"
    private const val KEY_LOCAL_NOTIFICATION_LISTENER_ENABLED = "local_notification_listener_enabled"
    private const val KEY_LOCAL_ACCESSIBILITY_ENABLED = "local_accessibility_enabled"
    private const val KEY_LOCAL_BATTERY_OPTIMIZATION_IGNORED = "local_battery_optimization_ignored"
    private const val KEY_LOCAL_BACKGROUND_START_ALLOWED = "local_background_start_allowed"
    private const val KEY_LOCAL_STATUS = "local_status"

    private val sp: SPUtils
        get() = SPUtils.getInstance(SP_NAME)

    data class SetupState(
        val keepAliveEnabled: Boolean,
        val notificationListenerEnabled: Boolean,
        val accessibilityEnabled: Boolean,
        val batteryOptimizationIgnored: Boolean,
        val backgroundStartAllowed: Boolean
    ) {
        val isComplete: Boolean
            get() = keepAliveEnabled &&
                notificationListenerEnabled &&
                accessibilityEnabled &&
                batteryOptimizationIgnored &&
                backgroundStartAllowed
    }

    data class LocalSnapshot(
        val regId: String,
        val lastJPushInitAt: Long,
        val lastRegisterAt: Long,
        val lastInboundCommandAt: Long,
        val lastHeartbeatPingReceivedAt: Long,
        val lastRecoveryAttemptAt: Long,
        val lastRecoveryReason: String,
        val status: DeviceHealthStatus,
        val setupState: SetupState
    )

    data class TargetSnapshot(
        val targetRegId: String,
        val lastHeartbeatPingSentAt: Long,
        val lastHeartbeatAckReceivedAt: Long,
        val lastAckFromRegId: String,
        val missedHeartbeats: Int,
        val status: DeviceHealthStatus
    )

    fun saveTargetRegId(regId: String) {
        sp.put(KEY_TARGET_REG_ID, regId.trim())
    }

    fun getTargetRegId(): String = sp.getString(KEY_TARGET_REG_ID, "")

    fun updateRegistrationId(regId: String) {
        val trimmed = regId.trim()
        sp.put(KEY_CURRENT_REG_ID, trimmed)
        if (trimmed.isNotEmpty()) {
            sp.put(KEY_LAST_REGISTER_AT, now())
        }
    }

    fun markJPushInitialized() {
        sp.put(KEY_LAST_JPUSH_INIT_AT, now())
    }

    fun markInboundCommand(command: String) {
        sp.put(KEY_LAST_INBOUND_COMMAND_AT, now())
        if (command.isNotBlank()) {
            sp.put("last_command_name", command)
        }
    }

    fun markHeartbeatPingReceived(senderRegId: String) {
        sp.put(KEY_LAST_HEARTBEAT_PING_RECEIVED_AT, now())
        sp.put(KEY_LAST_HEARTBEAT_SENDER_REG_ID, senderRegId)
    }

    fun markHeartbeatPingSent(targetRegId: String) {
        val trimmed = targetRegId.trim()
        if (trimmed.isNotEmpty()) {
            saveTargetRegId(trimmed)
        }
        sp.put(KEY_LAST_HEARTBEAT_PING_SENT_AT, now())
    }

    fun markHeartbeatAckReceived(senderRegId: String) {
        sp.put(KEY_LAST_HEARTBEAT_ACK_RECEIVED_AT, now())
        sp.put(KEY_LAST_ACK_FROM_REG_ID, senderRegId)
        sp.put(KEY_CONSECUTIVE_MISSED_HEARTBEATS, 0)
        updateTargetStatus(DeviceHealthStatus.ONLINE)
    }

    fun markRecoveryAttempt(reason: String) {
        sp.put(KEY_LAST_RECOVERY_ATTEMPT_AT, now())
        sp.put(KEY_LAST_RECOVERY_REASON, reason)
    }

    fun incrementMissedHeartbeat(): Int {
        val missed = sp.getInt(KEY_CONSECUTIVE_MISSED_HEARTBEATS, 0) + 1
        sp.put(KEY_CONSECUTIVE_MISSED_HEARTBEATS, missed)
        return missed
    }

    fun getMissedHeartbeatCount(): Int = sp.getInt(KEY_CONSECUTIVE_MISSED_HEARTBEATS, 0)

    fun updateTargetStatus(status: DeviceHealthStatus) {
        sp.put(KEY_TARGET_STATUS, status.rawValue)
    }

    fun updateSetupState(setupState: SetupState) {
        sp.put(KEY_LOCAL_KEEP_ALIVE_ENABLED, setupState.keepAliveEnabled)
        sp.put(KEY_LOCAL_NOTIFICATION_LISTENER_ENABLED, setupState.notificationListenerEnabled)
        sp.put(KEY_LOCAL_ACCESSIBILITY_ENABLED, setupState.accessibilityEnabled)
        sp.put(KEY_LOCAL_BATTERY_OPTIMIZATION_IGNORED, setupState.batteryOptimizationIgnored)
        sp.put(KEY_LOCAL_BACKGROUND_START_ALLOWED, setupState.backgroundStartAllowed)
    }

    fun getSetupState(): SetupState {
        return SetupState(
            keepAliveEnabled = sp.getBoolean(KEY_LOCAL_KEEP_ALIVE_ENABLED, false),
            notificationListenerEnabled = sp.getBoolean(KEY_LOCAL_NOTIFICATION_LISTENER_ENABLED, false),
            accessibilityEnabled = sp.getBoolean(KEY_LOCAL_ACCESSIBILITY_ENABLED, false),
            batteryOptimizationIgnored = sp.getBoolean(KEY_LOCAL_BATTERY_OPTIMIZATION_IGNORED, false),
            backgroundStartAllowed = sp.getBoolean(KEY_LOCAL_BACKGROUND_START_ALLOWED, false)
        )
    }

    fun updateLocalStatus(status: DeviceHealthStatus) {
        sp.put(KEY_LOCAL_STATUS, status.rawValue)
    }

    fun deriveLocalStatus(): DeviceHealthStatus {
        val setupState = getSetupState()
        if (!setupState.isComplete) {
            return DeviceHealthStatus.SETUP_INCOMPLETE
        }
        val regId = sp.getString(KEY_CURRENT_REG_ID, "")
        if (regId.isBlank()) {
            return DeviceHealthStatus.OFFLINE
        }
        val latestActivityAt = listOf(
            sp.getLong(KEY_LAST_REGISTER_AT, 0L),
            sp.getLong(KEY_LAST_HEARTBEAT_PING_RECEIVED_AT, 0L),
            sp.getLong(KEY_LAST_INBOUND_COMMAND_AT, 0L),
            sp.getLong(KEY_LAST_JPUSH_INIT_AT, 0L)
        ).maxOrNull() ?: 0L
        val age = now() - latestActivityAt
        return when {
            latestActivityAt == 0L -> DeviceHealthStatus.DEGRADED
            age <= 30 * 60 * 1000L -> DeviceHealthStatus.ONLINE
            age <= 2 * 60 * 60 * 1000L -> DeviceHealthStatus.DEGRADED
            else -> DeviceHealthStatus.OFFLINE
        }
    }

    fun getLocalSnapshot(): LocalSnapshot {
        val status = DeviceHealthStatus.from(sp.getString(KEY_LOCAL_STATUS, DeviceHealthStatus.OFFLINE.rawValue))
        return LocalSnapshot(
            regId = sp.getString(KEY_CURRENT_REG_ID, ""),
            lastJPushInitAt = sp.getLong(KEY_LAST_JPUSH_INIT_AT, 0L),
            lastRegisterAt = sp.getLong(KEY_LAST_REGISTER_AT, 0L),
            lastInboundCommandAt = sp.getLong(KEY_LAST_INBOUND_COMMAND_AT, 0L),
            lastHeartbeatPingReceivedAt = sp.getLong(KEY_LAST_HEARTBEAT_PING_RECEIVED_AT, 0L),
            lastRecoveryAttemptAt = sp.getLong(KEY_LAST_RECOVERY_ATTEMPT_AT, 0L),
            lastRecoveryReason = sp.getString(KEY_LAST_RECOVERY_REASON, ""),
            status = status,
            setupState = getSetupState()
        )
    }

    fun getTargetSnapshot(): TargetSnapshot {
        return TargetSnapshot(
            targetRegId = getTargetRegId(),
            lastHeartbeatPingSentAt = sp.getLong(KEY_LAST_HEARTBEAT_PING_SENT_AT, 0L),
            lastHeartbeatAckReceivedAt = sp.getLong(KEY_LAST_HEARTBEAT_ACK_RECEIVED_AT, 0L),
            lastAckFromRegId = sp.getString(KEY_LAST_ACK_FROM_REG_ID, ""),
            missedHeartbeats = getMissedHeartbeatCount(),
            status = DeviceHealthStatus.from(sp.getString(KEY_TARGET_STATUS, DeviceHealthStatus.OFFLINE.rawValue))
        )
    }

    fun formatTimestamp(timestamp: Long): String {
        if (timestamp <= 0L) {
            return "暂无"
        }
        return DateFormat.format("yyyy-MM-dd HH:mm:ss", Date(timestamp)).toString()
    }

    fun describeStatus(status: DeviceHealthStatus): String {
        return when (status) {
            DeviceHealthStatus.ONLINE -> "在线"
            DeviceHealthStatus.DEGRADED -> "状态不稳定"
            DeviceHealthStatus.OFFLINE -> "离线"
            DeviceHealthStatus.SETUP_INCOMPLETE -> "设置未完成"
        }
    }

    fun buildSetupSummary(setupState: SetupState): String {
        val statusParts = listOf(
            "保活=${flag(setupState.keepAliveEnabled)}",
            "通知=${flag(setupState.notificationListenerEnabled)}",
            "无障碍=${flag(setupState.accessibilityEnabled)}",
            "电池=${flag(setupState.batteryOptimizationIgnored)}",
            "后台=${flag(setupState.backgroundStartAllowed)}"
        )
        return statusParts.joinToString(separator = "  ")
    }

    private fun flag(enabled: Boolean): String {
        return if (enabled) "正常" else "缺失"
    }

    private fun now(): Long = System.currentTimeMillis()
}
