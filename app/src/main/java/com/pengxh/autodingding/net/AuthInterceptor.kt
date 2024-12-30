package com.pengxh.autodingding.net

import com.blankj.utilcode.util.EncodeUtils
import com.blankj.utilcode.util.LogUtils
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

object AuthInterceptor : Interceptor {
    private const val appKey = "f01b8a02b66ea2d632e52e2e"
    private const val masterSecret = "5e9ebea1163a6f3e2197f35d"
    private const val authString = "$appKey:$masterSecret"

    private var token = "Basic ${EncodeUtils.base64Encode2String(authString.toByteArray())}"
        set(value) {
            field = value
        }

    override fun intercept(chain: Interceptor.Chain): Response {
        val origin = chain.request()
        val builder = origin.newBuilder().addHeader("Authorization", token)
        val response : Response
        try {
            response = chain.proceed(builder.build())

        }catch (e:Exception){
            e.printStackTrace()
            throw IOException(e)
        }
        return response
    }
}