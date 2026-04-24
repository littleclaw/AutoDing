package com.pengxh.autodingding.ui.fragment

import androidx.databinding.ObservableField
import androidx.databinding.ObservableBoolean
import com.pengxh.autodingding.base.BaseViewModel

class PermissionVM : BaseViewModel() {
    val accessibilityEnable = ObservableBoolean()
    val bgStartEnable = ObservableBoolean()
    val notificationListenerEnable = ObservableBoolean()
    val batteryOptimizationIgnored = ObservableBoolean()
    val keepAliveEnable = ObservableBoolean()
    val protectionSummary = ObservableField("")
    val localStatus = ObservableField("")
}
