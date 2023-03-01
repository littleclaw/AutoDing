package com.pengxh.autodingding.actions

import android.app.Activity
import cn.vove7.andro_accessibility_api.api.swipe
import kotlinx.coroutines.delay

class SwipeUnlockAction: Action() {
    override val name: String
        get() = "解锁"

    override suspend fun run(act: Activity) {
        delay(1000)
        //从下往上的滑动
        swipe(500, 1200, 500, 400, 1000)
    }
}