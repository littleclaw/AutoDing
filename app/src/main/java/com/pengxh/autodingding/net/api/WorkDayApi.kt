package com.pengxh.autodingding.net.api

import com.pengxh.autodingding.bean.WorkdayResp
import retrofit2.http.GET
import retrofit2.http.Headers

interface WorkDayApi {
    @Headers("User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/129.0.0.0 Safari/537.36")
    @GET("/api/holiday/workday/next/")
    suspend fun getNextWorkday(): WorkdayResp
}