package com.pengxh.autodingding.service

import android.content.Context
import com.blankj.utilcode.util.CacheDiskUtils
import com.pengxh.autodingding.bean.BodyMsg
import com.pengxh.autodingding.bean.PushAudience
import com.pengxh.autodingding.bean.PushMessage
import com.pengxh.autodingding.bean.PushResp
import com.pengxh.autodingding.net.RetrofitManager
import com.pengxh.autodingding.net.api.PushApi

object PushApiHelper {
    suspend fun pushCommand(context: Context, regId: String, cmd: String): PushResp? {
        val trimmed = regId.trim()
        if (trimmed.isBlank()) {
            return null
        }
        val api = RetrofitManager.retrofitClient.create(PushApi::class.java)
        val pushMessage = PushMessage()
        pushMessage.audience = PushAudience().apply {
            registration_id = mutableListOf(trimmed)
        }
        pushMessage.message = BodyMsg().apply {
            msg_content = cmd
            title = CacheDiskUtils.getInstance().getString("pushRegId")
        }
        return api.pushMsg(pushMessage)
    }
}
