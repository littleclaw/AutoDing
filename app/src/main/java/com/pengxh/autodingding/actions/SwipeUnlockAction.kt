package com.pengxh.autodingding.actions

import android.app.Activity
import cn.vove7.andro_accessibility_api.api.swipe
import kotlinx.coroutines.delay

class SwipeUnlockAction(val orientation: ORIENTATION=ORIENTATION.VERTICAL): Action() {
    enum class ORIENTATION{
        HORIZONTAL,VERTICAL,CUSTOM
    }
    constructor(startX:Int, startY:Int, endX:Int, endY:Int) : this(ORIENTATION.CUSTOM) {
        this.startX = startX
        this.startY = startY
        this.endX = endX
        this.endY = endY
    }
    var startX= 0
    var startY = 0
    var endX = 0
    var endY = 0
    override val name: String
        get() = "解锁"

    override suspend fun run(act: Activity) {
        delay(1000)
        //从下往上的滑动
        when (orientation) {
            ORIENTATION.VERTICAL -> swipe(500, 1200, 500, 400, 800)
            ORIENTATION.HORIZONTAL -> swipe(100, 900, 600, 900, 800)
            else -> swipe(startX, startY, endX, endY, 800)
        }
    }
}