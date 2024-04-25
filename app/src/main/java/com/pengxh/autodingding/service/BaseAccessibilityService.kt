package com.pengxh.autodingding.service

import android.util.Log
import cn.vove7.andro_accessibility_api.AccessibilityApi
import cn.vove7.auto.core.AppScope

class BaseAccessibilityService: AccessibilityApi() {

    override val enableListenPageUpdate: Boolean
        get() = true
    companion object {
        private const val TAG = "BaseAccessibilityService"
    }

    override fun onCreate() {
        //must set
        baseService = this
        super.onCreate()
    }

    override fun onDestroy() {
        //must set
        baseService = null
        super.onDestroy()
    }

    //页面更新回调
    override fun onPageUpdate(currentScope: AppScope) {
        Log.d(TAG, "onPageUpdate: $currentScope")
    }

}