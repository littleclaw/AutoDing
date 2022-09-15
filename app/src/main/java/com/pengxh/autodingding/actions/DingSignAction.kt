package com.pengxh.autodingding.actions

import android.app.Activity
import cn.vove7.andro_accessibility_api.api.back
import cn.vove7.andro_accessibility_api.api.requireBaseAccessibility
import cn.vove7.andro_accessibility_api.api.waitBaseAccessibility
import cn.vove7.andro_accessibility_api.api.waitForApp
import cn.vove7.andro_accessibility_api.viewfinder.SF
import cn.vove7.andro_accessibility_api.viewfinder.containsText
import com.pengxh.autodingding.utils.Constant
import com.pengxh.autodingding.utils.toast
import kotlinx.coroutines.delay

class DingSignAction: Action() {
    var result = false
    override val name: String
        get() = "打开钉钉，手动打卡"

    override suspend fun run(act: Activity) {
        requireBaseAccessibility(true)

        toast("1秒后启动钉钉")
        delay(1000)
        val targetApp = Constant.DINGDING
        act.startActivity(act.packageManager.getLaunchIntentForPackage(targetApp))

        if (
            waitForApp(targetApp, 5000).also {
                toast("等待启动 " + if (it) "成功" else "失败")
            }
        ){
            delay(2000)
            val messageTab = SF.containsText("消息").findFirst(false)

            toast(if (messageTab!=null) "找到消息按钮" else "未找到消息按钮")
            if (messageTab != null){
                messageTab.tryClick()
                delay(2000)
            }
            val signTab = SF.containsText("打卡").findFirst(false)
            toast("找到${signTab!= null}打卡")
            if(signTab != null){
                signTab.tryClick()
                toast("8秒后尝试寻找打卡按钮")
                delay(8000)
            }
            val signButton = SF.containsText("下班打卡")
                .or(SF.containsText("上班打卡"))
                .findFirst(false)
            val clickSucceed:Boolean = signButton?.tryClick()?:false
            delay(5000)
            val succeed = SF.containsText("成功")
                .findFirst(false) != null
            toast("手动打卡 "+ if (succeed) "成功" else "失败")
            delay(2000)
            back()
            delay(2000)
            back()
            result = clickSucceed && succeed
        }
    }
}