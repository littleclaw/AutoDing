package com.pengxh.autodingding

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.BatteryManager
import cn.jpush.android.api.JPushInterface
import com.blankj.utilcode.util.ProcessUtils
import com.blankj.utilcode.util.ScreenUtils
import com.pengxh.autodingding.utils.SendMailUtil
import com.pengxh.autodingding.utils.Utils

class BatteryLevelReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (Intent.ACTION_BATTERY_LOW == action){
            val emailAddress = Utils.readEmailAddress()
            val manager = context.getSystemService(AndroidxBaseActivity.BATTERY_SERVICE) as BatteryManager
            val curBatteryCurrent = manager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)
            val curBattery = manager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)///当前电量百分比
            val regId = JPushInterface.getRegistrationID(context)
            val screenLock = ScreenUtils.isScreenLock()
            val message = "警告！低电量！注册ID: $regId 是否锁屏：$screenLock " +
                    "当前电流：$curBatteryCurrent mA  当前电量百分比：$curBattery %"
            SendMailUtil.send(emailAddress, message)
        }
    }
}