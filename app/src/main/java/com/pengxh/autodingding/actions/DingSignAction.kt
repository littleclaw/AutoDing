package com.pengxh.autodingding.actions

import android.app.Activity
import android.util.Log
import cn.vove7.andro_accessibility_api.api.findAllWith
import cn.vove7.andro_accessibility_api.api.waitBaseAccessibility
import cn.vove7.andro_accessibility_api.api.waitForApp
import cn.vove7.andro_accessibility_api.viewfinder.SF
import cn.vove7.andro_accessibility_api.viewfinder.containsText
import com.blankj.utilcode.util.LogUtils
import com.pengxh.autodingding.utils.Constant
import com.pengxh.autodingding.utils.toast
import kotlinx.coroutines.delay

class DingSignAction: Action() {
    override val name: String
        get() = "打开钉钉，手动打卡"

    override suspend fun run(act: Activity) {
        waitBaseAccessibility()

        toast("start ding after 1s")
        delay(1000)
        val targetApp = Constant.DINGDING
        act.startActivity(act.packageManager.getLaunchIntentForPackage(targetApp))

        if (
            waitForApp(targetApp, 5000).also {
                toast("wait " + if (it) "success" else "failed")
            }
        ){
            delay(2000)
            val messageTab = SF.containsText("消息").findFirst(false)

            toast("找到${messageTab!=null}消息")
            if (messageTab != null){
                messageTab.tryClick()
                delay(2000)
            }
            val signTab = SF.containsText("打卡").findFirst(false)
            toast("找到${signTab!= null}打卡")
            if(signTab != null){
                signTab.tryClick()
                delay(2000)
            }
            val signButton = SF.containsText("下班打卡")
                .or(SF.containsText("上班打卡"))
                .findFirst(false)
            signButton?.tryClick()
        }
    }
}