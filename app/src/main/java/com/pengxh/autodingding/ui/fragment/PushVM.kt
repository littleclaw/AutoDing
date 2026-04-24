package com.pengxh.autodingding.ui.fragment

import androidx.lifecycle.MutableLiveData
import com.blankj.utilcode.util.LogUtils
import com.pengxh.autodingding.BaseApplication
import com.pengxh.autodingding.base.BaseViewModel
import com.pengxh.autodingding.bean.PushResp
import com.pengxh.autodingding.health.DeviceHealthStatus
import com.pengxh.autodingding.health.PushHealthStore
import com.pengxh.autodingding.health.PushRuntimeManager
import com.pengxh.autodingding.service.PushApiHelper
import com.pengxh.autodingding.service.PushReceiver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PushVM: BaseViewModel() {
    val pushResult = MutableLiveData<PushResp?>()
    var lastCommand: String = ""

    fun pushCheck(regId:String) {
        pushCmd(regId, PushReceiver.MSG_HEALTH_PING)
    }

    fun pushSign(regId:String) {
        pushCmd(regId, PushReceiver.MSG_SIGN)
    }

    fun pushStatusFetch(regId: String){
        pushCmd(regId, PushReceiver.MSG_STATUS_REPORT)
    }

    fun pushScreenShot(regId: String){
        pushCmd(regId, PushReceiver.MSG_SCREEN_SHOT)
    }

    fun pushManualSign(regId: String){
        pushCmd(regId, PushReceiver.MSG_MANUAL_SIGN)
    }

    fun pushMailPeriodicLog(regId: String){
        pushCmd(regId, PushReceiver.MSG_WAKE_LOG)
    }

    private fun pushCmd(regId: String, cmd: String) = launch {
        lastCommand = cmd
        pushResult.value = withContext(Dispatchers.IO){
            val context = BaseApplication.application?.applicationContext
            val response = context?.let {
                PushApiHelper.pushCommand(it, regId, cmd)
            }
            if (cmd == PushReceiver.MSG_HEALTH_PING && response?.errorCode == 0 && context != null) {
                PushHealthStore.markHeartbeatPingSent(regId)
                PushHealthStore.updateTargetStatus(DeviceHealthStatus.DEGRADED)
                PushRuntimeManager.scheduleHeartbeatTimeout(context)
            }
            LogUtils.d("push, regId:${regId}, cmd:${cmd}")
            response
        }
    }
}
