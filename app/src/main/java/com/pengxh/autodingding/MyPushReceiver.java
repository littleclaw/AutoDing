package com.pengxh.autodingding;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.AppUtils;
import com.blankj.utilcode.util.IntentUtils;
import com.pengxh.autodingding.bean.MailInfo;
import com.pengxh.autodingding.ui.MainActivity;
import com.pengxh.autodingding.utils.Constant;
import com.pengxh.autodingding.utils.MailSender;
import com.pengxh.autodingding.utils.SendMailUtil;
import com.pengxh.autodingding.utils.Utils;

import cn.jpush.android.api.CustomMessage;
import cn.jpush.android.service.JPushMessageReceiver;

public class MyPushReceiver extends JPushMessageReceiver {
    private static final String TAG = "MyReceiver";
    public static final String MSG_MAIL_CHECK = "check";
    public static final String MSG_SIGN = "sign";

    @Override
    public void onMessage(Context context, CustomMessage customMessage) {
        Log.d(TAG, customMessage.message);
        Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if(MSG_MAIL_CHECK.equals(customMessage.message)){
            intent.putExtra(MainActivity.EXTRA_ACTION, MainActivity.ACTION_SEND_MAIL);
            context.startActivity(intent);
        }else if (MSG_SIGN.equals(customMessage.message)){
            intent.putExtra(MainActivity.EXTRA_ACTION, MainActivity.ACTION_LAUNCH_DING);
            context.startActivity(intent);
        }
        super.onMessage(context, customMessage);
    }
}
