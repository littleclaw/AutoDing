package com.pengxh.autodingding.service

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.BatteryManager
import android.os.Build
import android.os.PowerManager
import android.util.Log
import cn.jpush.android.api.CustomMessage
import cn.jpush.android.api.JPushInterface
import cn.jpush.android.service.JPushMessageService
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.AppUtils
import com.blankj.utilcode.util.CacheDiskUtils
import com.blankj.utilcode.util.DeviceUtils
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.ToastUtils
import com.pengxh.autodingding.AndroidxBaseActivity
import com.pengxh.autodingding.actions.SwipeUnlockAction
import com.pengxh.autodingding.bean.BodyMsg
import com.pengxh.autodingding.bean.PushAudience
import com.pengxh.autodingding.bean.PushMessage
import com.pengxh.autodingding.net.RetrofitManager
import com.pengxh.autodingding.net.api.PushApi
import com.pengxh.autodingding.ui.MainActivity
import com.pengxh.autodingding.utils.SendMailUtil
import com.pengxh.autodingding.utils.Utils
import com.pengxh.autodingding.utils.launchWithExpHandler
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

class PushCoreService : JPushMessageService() {
    override fun onMessage(context: Context, customMessage: CustomMessage) {
        Log.d(TAG, customMessage.message)
        val intent = Intent(context, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        val senderRegId = customMessage.title
        if (MSG_MAIL_CHECK == customMessage.message) {
            wakePhone(context)
            unlockPhone(context){
                intent.putExtra(MainActivity.EXTRA_ACTION, MainActivity.ACTION_SEND_MAIL)
                context.startActivity(intent)
            }
            reply(senderRegId)
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
            val androidAPI = Build.VERSION.SDK_INT
            val manufacturer = Build.MANUFACTURER
            val model = DeviceUtils.getModel()
            val appInfo = AppUtils.getAppInfo()
            val message = "注册ID: $regId ${if (screenLock) "是" else "未"}锁屏 " +
                    "当前${if (charging) "正在" else "未"}充电： 当前电量百分比：$curBattery %," +
                    "安卓版本:${androidAPI}, 厂商:${manufacturer},型号：${model}, 应用版本${appInfo.versionName}"
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

    @OptIn(DelicateCoroutinesApi::class)
    private fun wakePhone(context: Context){
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val screenOn = powerManager.isInteractive
        if (!screenOn) {
            //唤醒屏幕
            Log.d(TAG, "screen off, now waking up phone")
            val wakeLock = powerManager.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK,
                "autoDing:bright"
            )
            wakeLock.acquire(10000)
            launchWithExpHandler {
                withContext(Dispatchers.IO){
                    delay(50000)
                    wakeLock.release()
                }
            }
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
                        val emailAddress = Utils.readEmailAddress()
                        SendMailUtil.send(emailAddress, "解锁手机异常，可能手机未设置锁屏显示，导致手机收到了消息但无法正常唤醒手机锁屏");
//                        launchWithExpHandler {
//                            SwipeUnlockAction().run(ActivityUtils.getTopActivity())
//                            delay(3000)
//                            callback.invoke()
//                        }
                    }

                    override fun onDismissCancelled() {
                        Log.d("unlock", "cancelled")
                        ToastUtils.showShort("解锁取消")
                    }
                })
            }else{
                Log.d("unlock", "old version dismiss function")
                keyGuardManager.newKeyguardLock("dismiss").disableKeyguard()
                callback.invoke()
            }
        }else{
            callback.invoke()
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun reply(targetRegId:String){
        launchWithExpHandler {
            withContext(Dispatchers.IO){
                val api = RetrofitManager.retrofitClient.create(PushApi::class.java)
                val pushMessage = PushMessage()
                pushMessage.audience = PushAudience().apply {
                    registration_id = mutableListOf(targetRegId)
                }
                pushMessage.message = BodyMsg().apply {
                    msg_content = MSG_REPLY
                    title = CacheDiskUtils.getInstance().getString("pushRegId")
                }
                api.pushMsg(pushMessage)
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
        const val MSG_REPLY = "reply"
    }
}