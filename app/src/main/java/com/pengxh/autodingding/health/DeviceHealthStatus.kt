package com.pengxh.autodingding.health

enum class DeviceHealthStatus(val rawValue: String) {
    ONLINE("online"),
    DEGRADED("degraded"),
    OFFLINE("offline"),
    SETUP_INCOMPLETE("setup_incomplete");

    companion object {
        fun from(rawValue: String?): DeviceHealthStatus {
            return entries.firstOrNull { it.rawValue == rawValue } ?: OFFLINE
        }
    }
}
