package com.pengxh.autodingding

import android.content.Context
import android.content.Intent
import android.util.Log
import cn.jpush.android.api.CustomMessage
import cn.jpush.android.service.JPushMessageReceiver
import com.pengxh.autodingding.ui.MainActivity
import com.pengxh.autodingding.utils.Utils

class MyPushReceiver : JPushMessageReceiver() {
    override fun onMessage(context: Context, customMessage: CustomMessage) {
        Log.d(TAG, customMessage.message)
        val intent = Intent(context, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        if (MSG_MAIL_CHECK == customMessage.message) {
            Utils.wakeUpAndUnlock()
            intent.putExtra(MainActivity.EXTRA_ACTION, MainActivity.ACTION_SEND_MAIL)
            context.startActivity(intent)
        } else if (MSG_SIGN == customMessage.message) {
            Utils.wakeUpAndUnlock()
            intent.putExtra(MainActivity.EXTRA_ACTION, MainActivity.ACTION_LAUNCH_DING)
            context.startActivity(intent)
        }
        super.onMessage(context, customMessage)
    }

    companion object {
        private const val TAG = "MyReceiver"
        const val MSG_MAIL_CHECK = "check"
        const val MSG_SIGN = "sign"
    }
}