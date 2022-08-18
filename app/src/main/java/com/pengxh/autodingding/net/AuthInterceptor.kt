package com.pengxh.autodingding.net

import com.blankj.utilcode.util.EncodeUtils
import com.blankj.utilcode.util.LogUtils
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

object AuthInterceptor : Interceptor {
    private const val appKey = "c48c0906e554f88ad914329b"
    private const val masterSecret = "9b6082ef0c5d1ad5d6734da7"
    private const val authString = "$appKey:$masterSecret"

    private var token = "Basic ${EncodeUtils.base64Encode2String(authString.toByteArray())}"
        set(value) {
            LogUtils.d("token set", value)
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