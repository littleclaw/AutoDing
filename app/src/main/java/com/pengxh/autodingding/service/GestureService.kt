package com.pengxh.autodingding.service

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import cn.vove7.andro_accessibility_api.AccessibilityApi

class GestureService: AccessibilityService() {

    override fun onCreate() {
        super.onCreate()
        //must call
        AccessibilityApi.gestureService = this
    }
    override fun onDestroy() {
        super.onDestroy()
        //must call
        AccessibilityApi.gestureService = null
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
    }

    override fun onInterrupt() {
    }
}