package com.pengxh.autodingding.actions

import android.app.Activity
import android.graphics.Point
import cn.vove7.andro_accessibility_api.requireBaseAccessibility
import cn.vove7.auto.core.api.back
import cn.vove7.auto.core.api.click
import cn.vove7.auto.core.viewfinder.SF
import cn.vove7.auto.core.viewfinder.containsText
import cn.vove7.auto.core.viewfinder.id
import com.blankj.utilcode.util.TimeUtils
import com.pengxh.autodingding.utils.Constant
import com.pengxh.autodingding.utils.toast
import kotlinx.coroutines.delay

class DingSignAction : Action() {
    private var result = false
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

        delay(9000)
        genLog("查找钉钉首页元素")
        val messageTab = SF.containsText("消息").findFirst(false)

        toast(if (messageTab != null) "找到消息按钮" else "未找到消息按钮")
        if (messageTab != null) {
            messageTab.tryClick()
            delay(2000)
        }
        val signTab = SF.containsText("打卡").findFirst(false)
        toast("找到${signTab != null}打卡")
        genLog("查找打卡入口")
        if (signTab != null) {
            val signClick = signTab.tryClick()
            genLog("点击打卡入口" + if (signClick) "成功" else "失败")
            toast("8秒后尝试寻找打卡按钮")
            delay(8000)
        }
        val webPage = SF.id("com.alibaba.android.rimet:id/h5_pc_container").findFirst()
        genLog("查找打卡webView" + if (webPage != null) "成功" else "失败")
        var clickSucceed = false
        if (webPage != null) {
            val centerP = webPage.getCenterPoint()
            for (i in 1..5) {
                val delta = 70
                tryClick(Point(centerP.x, centerP.y + i * delta))
                clickSucceed = SF.containsText("成功")
                    .findFirst(false) != null
                if (clickSucceed) {
                    break
                }
            }
        }
        genLog("点击打卡" + if (clickSucceed) "成功" else "失败" + "，准备查看页面结果")
        val succeed = SF.containsText("成功")
            .findFirst(false) != null
        toast("手动打卡 " + if (succeed) "成功" else "失败")
        genLog("页面结果判断打卡" + if (succeed) "成功" else "失败" + ",准备回退")
        delay(2000)
        back()
        genLog("回退第一次")
        delay(2000)
        back()
        result = succeed
        genLog("回退第二次，执行结束，最终执行判定：" + if (result) "成功" else "失败")
    }

    private fun genLog(actionDesc: String) {
        message.append(actionDesc + "-----" + TimeUtils.getNowString() + "\n")
    }

    private suspend fun tryClick(p: Point) {
        click(p.x, p.y)
        val delayTime = 5L
        genLog("点击坐标点${p.x},${p.y},并等待${delayTime}秒")
        delay(delayTime * 1000)
    }
}