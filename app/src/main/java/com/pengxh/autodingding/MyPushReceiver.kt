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
import com.blankj.utilcode.util.ToastUtils
import com.pengxh.autodingding.actions.SwipeUnlockAction
import com.pengxh.autodingding.ui.MainActivity
import com.pengxh.autodingding.utils.SendMailUtil
import com.pengxh.autodingding.utils.Utils
import com.pengxh.autodingding.utils.launchWithExpHandler
import kotlinx.coroutines.delay

class MyPushReceiver : JPushMessageReceiver() {
    override fun onMessage(context: Context, customMessage: CustomMessage) {
        Log.d(TAG, customMessage.message)
        val intent = Intent(context, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        if (MSG_MAIL_CHECK == customMessage.message) {
            wakePhone(context)
            unlockPhone(context){
                intent.putExtra(MainActivity.EXTRA_ACTION, MainActivity.ACTION_SEND_MAIL)
                context.startActivity(intent)
            }
        } else if (MSG_SIGN == customMessage.message) {
            wakePhone(context)
            unlockPhone(context){
                intent.putExtra(MainActivity.EXTRA_ACTION, MainActivity.ACTION_LAUNCH_DING)
                context.startActivity(intent)
            }
        } else if (MSG_STATUS_REPORT == customMessage.message){
            val emailAddress = Utils.readEmailAddress()
            val manager = context.getSystemService(AndroidxBaseActivity.BATTERY_SERVICE) as BatteryManager
            val charging = manager.isCharging
            val curBattery = manager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)///当前电量百分比
            val regId = JPushInterface.getRegistrationID(context)
            val screenLock = ScreenUtils.isScreenLock()
            val message = "注册ID: $regId ${if (screenLock) "是" else "未"}锁屏 " +
                    "当前${if (charging) "正在" else "未"}充电： 当前电量百分比：$curBattery %"
            SendMailUtil.send(emailAddress, message)
        } else if (MSG_SCREEN_SHOT == customMessage.message){
            wakePhone(context)
            unlockPhone(context){
                intent.putExtra(MainActivity.EXTRA_ACTION, MainActivity.ACTION_SCREENSHOT)
                context.startActivity(intent)
            }
        } else if (MSG_SLEEP == customMessage.message){
            val ifLock = ScreenUtils.isScreenLock()
            if (ifLock.not()){
                //TODO turn off screen
            }
        } else if (MSG_MANUAL_SIGN == customMessage.message){
            wakePhone(context)
            unlockPhone(context){
                intent.putExtra(MainActivity.EXTRA_ACTION, MainActivity.ACTION_MANUAL_SIGN)
                context.startActivity(intent)
            }
        }
        super.onMessage(context, customMessage)
    }

    private fun wakePhone(context: Context){
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val screenOn = powerManager.isInteractive
        if (!screenOn) {
            //唤醒屏幕
            Log.d(TAG, "screen off, now waking up phone")
            val wakeLock = powerManager.newWakeLock(
                PowerManager.ACQUIRE_CAUSES_WAKEUP or PowerManager.SCREEN_BRIGHT_WAKE_LOCK,
                "autoDing:bright"
            )
            wakeLock.acquire(10000)
            wakeLock.release()
        }
    }

    private fun unlockPhone(context: Context, callback: (()->Unit)){
        val keyGuardManager = context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        if (keyGuardManager.isKeyguardLocked){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Log.d("unlock", "8.0+ version dismiss function")
                keyGuardManager.requestDismissKeyguard(ActivityUtils.getTopActivity(), object : KeyguardManager.KeyguardDismissCallback(){
                    override fun onDismissSucceeded() {
                        Log.d("unlock", "success")
                        callback.invoke()
                    }

                    override fun onDismissError() {
                        Log.d("unlock", "error")
                        ToastUtils.showShort("解锁错误")
                        launchWithExpHandler {
                            SwipeUnlockAction().run(ActivityUtils.getTopActivity())
                            delay(3000)
                            callback.invoke()
                        }
                    }

                    override fun onDismissCancelled() {
                        Log.d("unlock", "cancelled")
                        ToastUtils.showShort("解锁取消")
                    }
                })
            }else{
                Log.d("unlock", "old version dismiss function")
                keyGuardManager.newKeyguardLock("dismiss").disableKeyguard()
            }
        }else{
            callback.invoke()
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