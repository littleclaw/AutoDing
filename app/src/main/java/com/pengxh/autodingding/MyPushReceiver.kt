package com.pengxh.autodingding

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.os.BatteryManager
import android.os.Build
import android.os.PowerManager
import android.util.Log
import cn.jpush.android.api.CustomMessage
import cn.jpush.android.api.JPushInterface
import cn.jpush.android.service.JPushMessageReceiver
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.ScreenUtils
import com.pengxh.autodingding.ui.MainActivity
import com.pengxh.autodingding.utils.SendMailUtil
import com.pengxh.autodingding.utils.Utils

class MyPushReceiver : JPushMessageReceiver() {
    override fun onMessage(context: Context, customMessage: CustomMessage) {
        Log.d(TAG, customMessage.message)
        val intent = Intent(context, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        if (MSG_MAIL_CHECK == customMessage.message) {
            wakePhone(context)
            unlockPhone(context)
            intent.putExtra(MainActivity.EXTRA_ACTION, MainActivity.ACTION_SEND_MAIL)
            context.startActivity(intent)
        } else if (MSG_SIGN == customMessage.message) {
            wakePhone(context)
            unlockPhone(context)
            intent.putExtra(MainActivity.EXTRA_ACTION, MainActivity.ACTION_LAUNCH_DING)
            context.startActivity(intent)
        } else if (MSG_STATUS_REPORT == customMessage.message){
            val emailAddress = Utils.readEmailAddress()
            val manager = context.getSystemService(AndroidxBaseActivity.BATTERY_SERVICE) as BatteryManager
            val charging = manager.isCharging
            val curBattery = manager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)///当前电量百分比
            val regId = JPushInterface.getRegistrationID(context)
            val screenLock = ScreenUtils.isScreenLock()
            val message = "注册ID: $regId 是否锁屏：$screenLock " +
                    "当前充电：$charging 当前电量百分比：$curBattery %"
            SendMailUtil.send(emailAddress, message)
        } else if (MSG_SCREEN_SHOT == customMessage.message){
            wakePhone(context)
            unlockPhone(context)
            intent.putExtra(MainActivity.EXTRA_ACTION, MainActivity.ACTION_SCREENSHOT)
            context.startActivity(intent)
        } else if (MSG_SLEEP == customMessage.message){
            val ifLock = ScreenUtils.isScreenLock()
            if (ifLock.not()){
                //TODO turn off screen
            }
        } else if (MSG_MANUAL_SIGN == customMessage.message){
            wakePhone(context)
            unlockPhone(context)
            intent.putExtra(MainActivity.EXTRA_ACTION, MainActivity.ACTION_MANUAL_SIGN)
            context.startActivity(intent)
        }
        super.onMessage(context, customMessage)
    }

    private fun wakePhone(context: Context){
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val screenOn = powerManager.isInteractive
        if (!screenOn) {
            //唤醒屏幕
            val wakeLock = powerManager.newWakeLock(
                PowerManager.ACQUIRE_CAUSES_WAKEUP or PowerManager.SCREEN_BRIGHT_WAKE_LOCK,
                "autoDing:bright"
            )
            wakeLock.acquire(10000)
            wakeLock.release()
        }
    }

    private fun unlockPhone(context: Context){
        val keyGuardManager = context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        if (keyGuardManager.isKeyguardLocked){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                keyGuardManager.requestDismissKeyguard(ActivityUtils.getTopActivity(), object : KeyguardManager.KeyguardDismissCallback(){
                    override fun onDismissSucceeded() {
                        Log.d("unlock", "success")
                    }
                })
            }else{
                keyGuardManager.newKeyguardLock("dismiss").disableKeyguard()
            }
        }
    }

    companion object {
        private const val TAG = "MyReceiver"
        const val MSG_MAIL_CHECK = "check"
        const val MSG_SIGN = "sign"
        const val MSG_STATUS_REPORT = "statusReport"
        const val MSG_SCREEN_SHOT = "screenShot"
        const val MSG_SLEEP = "goToSleep"
        const val MSG_MANUAL_SIGN = "manualSign"
    }
}