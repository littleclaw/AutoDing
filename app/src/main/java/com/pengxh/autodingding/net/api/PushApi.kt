package com.pengxh.autodingding.net.api

import com.pengxh.autodingding.bean.PushMessage
import com.pengxh.autodingding.bean.PushResp
import retrofit2.http.Body
import retrofit2.http.POST

interface PushApi {
    @POST("v3/push")
    suspend fun pushMsg(@Body pushMessage: PushMessage): PushResp
}