package com.pengxh.autodingding.net.api

import com.pengxh.autodingding.base.ApiResponse
import com.pengxh.autodingding.bean.Version
import retrofit2.http.GET

interface UpdateApi {
    @GET("api/version.json")
    suspend fun getVersion(): ApiResponse<Version>
}