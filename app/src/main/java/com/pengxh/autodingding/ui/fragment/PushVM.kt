package com.pengxh.autodingding.ui.fragment

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.pengxh.autodingding.BaseViewModel
import com.pengxh.autodingding.MyPushReceiver
import com.pengxh.autodingding.bean.BodyMsg
import com.pengxh.autodingding.bean.PushAudience
import com.pengxh.autodingding.bean.PushMessage
import com.pengxh.autodingding.bean.PushResp
import com.pengxh.autodingding.net.RetrofitManager
import com.pengxh.autodingding.net.api.PushApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PushVM: BaseViewModel() {
    val pushResult = MutableLiveData<PushResp>()

    fun pushCheck(regId:String) = launch {
        pushResult.value = withContext(Dispatchers.IO){
            val api = RetrofitManager.retrofitClient.create(PushApi::class.java)
            val pushMessage = PushMessage()
            pushMessage.audience = PushAudience().apply {
                registration_id = mutableListOf(regId)
            }
            pushMessage.message = BodyMsg().apply {
                msg_content = MyPushReceiver.MSG_MAIL_CHECK
            }
            api.pushMsg(pushMessage)
        }
    }

    fun pushSign(regId:String) = launch {
        pushResult.value = withContext(Dispatchers.IO){
            val api = RetrofitManager.retrofitClient.create(PushApi::class.java)
            val pushMessage = PushMessage()
            pushMessage.audience = PushAudience().apply {
                registration_id = mutableListOf(regId)
            }
            pushMessage.message = BodyMsg().apply {
                msg_content = MyPushReceiver.MSG_SIGN
            }
            api.pushMsg(pushMessage)
        }
    }
}