package com.pengxh.autodingding.utils

import android.content.Context
import android.provider.Settings
import android.text.TextUtils.SimpleStringSplitter
import android.util.Log
import android.view.accessibility.AccessibilityManager


object AccessibilityUtil {
    fun isServiceOn(context: Context, serviceName: String): Boolean{
        val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        if (am.isEnabled){
            val fullAccName = "${context.packageName}/${serviceName}"
            return isAccessibilitySettingsOn(context, fullAccName)
        }
        return false
    }

    private fun isAccessibilitySettingsOn(context: Context, service: String): Boolean {
        Log.d("param service name", service)
        val mStringColonSplitter = SimpleStringSplitter(':')
        val settingValue: String = Settings.Secure.getString(
            context.applicationContext.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        )
        mStringColonSplitter.setString(settingValue)
        while (mStringColonSplitter.hasNext()) {
            val accessibilityService = mStringColonSplitter.next()
            Log.d("accessibility services", accessibilityService)
            if (accessibilityService.equals(service, ignoreCase = true)) {
                return true
            }
        }
        return false
    }
}