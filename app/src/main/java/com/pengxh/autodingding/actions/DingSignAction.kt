package com.pengxh.autodingding.actions

import android.app.Activity
import cn.vove7.andro_accessibility_api.api.back
import cn.vove7.andro_accessibility_api.api.requireBaseAccessibility
import cn.vove7.andro_accessibility_api.api.waitBaseAccessibility
import cn.vove7.andro_accessibility_api.api.waitForApp
import cn.vove7.andro_accessibility_api.viewfinder.SF
import cn.vove7.andro_accessibility_api.viewfinder.containsText
import com.blankj.utilcode.util.TimeUtils
import com.pengxh.autodingding.utils.Constant
import com.pengxh.autodingding.utils.toast
import kotlinx.coroutines.delay

class DingSignAction: Action() {
    var result = false
    var message = StringBuilder()
    override val name: String
        get() = "打开钉钉，手动打卡"

    override suspend fun run(act: Activity) {
        requireBaseAccessibility(true)

        toast("1秒后启动钉钉")
        delay(1000)
        val targetApp = Constant.DINGDING
        genLog("启动钉钉")
        act.startActivity(act.packageManager.getLaunchIntentForPackage(targetApp))

        if (
            waitForApp(targetApp, 5000).also {
                toast("等待启动 " + if (it) "成功" else "失败")
            }
        ){
            delay(2000)
            genLog("查找钉钉首页元素")
            val messageTab = SF.containsText("消息").findFirst(false)

            toast(if (messageTab!=null) "找到消息按钮" else "未找到消息按钮")
            if (messageTab != null){
                messageTab.tryClick()
                delay(2000)
            }
            val signTab = SF.containsText("打卡").findFirst(false)
            toast("找到${signTab!= null}打卡")
            genLog("查找打卡入口")
            if(signTab != null){
                val signClick = signTab.tryClick()
                genLog("点击打卡入口"+ if (signClick) "成功" else "失败")
                toast("8秒后尝试寻找打卡按钮")
                delay(8000)
            }
            val signButton = SF.containsText("下班打卡")
                .or(SF.containsText("上班打卡"))
                .or(SF.containsText("更新打卡"))
                .findFirst(false)
            genLog("查找上班打卡或下班打卡大按钮"+if (signButton!=null) "成功" else "失败")
            val clickSucceed:Boolean = signButton?.tryClick()?:false
            delay(5000)
            genLog("点击打卡"+ if (clickSucceed) "成功" else "失败" +"，准备查看页面结果")
            val succeed = SF.containsText("成功")
                .findFirst(false) != null
            toast("手动打卡 "+ if (succeed) "成功" else "失败")
            genLog("页面结果判断打卡" + if (succeed) "成功" else "失败" + ",准备回退")
            delay(2000)
            back()
            genLog("回退第一次")
            delay(2000)
            back()
            result = clickSucceed && succeed
            genLog("回退第二次，执行结束，最终执行判定：" + if (result) "成功" else "失败")
        }
    }

    private fun genLog(actionDesc: String) {
        message.append(actionDesc + "-----" + TimeUtils.getNowString()+"\n")
    }
}