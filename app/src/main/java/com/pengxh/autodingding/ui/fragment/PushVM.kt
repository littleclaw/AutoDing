package com.pengxh.autodingding.ui.fragment

import androidx.lifecycle.MutableLiveData
import cn.jpush.android.api.JPushInterface
import com.blankj.utilcode.util.CacheDiskUtils
import com.pengxh.autodingding.base.BaseViewModel
import com.pengxh.autodingding.bean.BodyMsg
import com.pengxh.autodingding.bean.PushAudience
import com.pengxh.autodingding.bean.PushMessage
import com.pengxh.autodingding.bean.PushResp
import com.pengxh.autodingding.net.RetrofitManager
import com.pengxh.autodingding.net.api.PushApi
import com.pengxh.autodingding.service.PushCoreService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PushVM: BaseViewModel() {
    val pushResult = MutableLiveData<PushResp>()

    fun pushCheck(regId:String) {
        pushCmd(regId, PushCoreService.MSG_MAIL_CHECK)
    }

    fun pushSign(regId:String) {
        pushCmd(regId, PushCoreService.MSG_SIGN)
    }

    fun pushStatusFetch(regId: String){
        pushCmd(regId, PushCoreService.MSG_STATUS_REPORT)
    }

    fun pushScreenShot(regId: String){
        pushCmd(regId, PushCoreService.MSG_SCREEN_SHOT)
    }

    fun pushManualSign(regId: String){
        pushCmd(regId, PushCoreService.MSG_MANUAL_SIGN)
    }

    private fun pushCmd(regId: String, cmd: String) = launch {
        pushResult.value = withContext(Dispatchers.IO){
            val api = RetrofitManager.retrofitClient.create(PushApi::class.java)
            val pushMessage = PushMessage()
            pushMessage.audience = PushAudience().apply {
                registration_id = mutableListOf(regId)
            }
            pushMessage.message = BodyMsg().apply {
                msg_content = cmd
                title = CacheDiskUtils.getInstance().getString("pushRegId")
            }
            api.pushMsg(pushMessage)
        }
    }
}