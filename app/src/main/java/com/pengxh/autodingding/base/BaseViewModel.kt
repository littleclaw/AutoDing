package com.pengxh.autodingding.base

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blankj.utilcode.util.LogUtils
import com.pengxh.autodingding.ApiException
import com.pengxh.autodingding.BuildConfig
import com.tencent.bugly.crashreport.CrashReport
import kotlinx.coroutines.*
import org.json.JSONException
import retrofit2.HttpException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException


typealias VmError =  (e: ApiException) -> Unit

open class BaseViewModel: ViewModel() {
    val coroutineExceptionHandler = CoroutineExceptionHandler { coroutineContext, throwable ->
        throwable.printStackTrace()
        LogUtils.e(coroutineContext, throwable)
    }

    /**
     * 错误信息liveData
     */
    val errorLiveData = MutableLiveData<ApiException>()

    /**
     * 无更多数据
     */
    val footLiveDate = MutableLiveData<Boolean>()

    /**
     * 无数据
     */
    val emptyLiveDate = MutableLiveData<Boolean>()

    /**
     * 处理错误
     */

    protected fun <T> launch(
        block:  () -> T
        , error:VmError? = null) {
        viewModelScope.launch {
            runCatching {
                block()
            }.onFailure {
                it.printStackTrace()
                CrashReport.postCatchedException(it)
                getApiException(it).apply {
                    withContext(Dispatchers.Main){
                        error?.invoke(this@apply)
                    }
                }
            }
        }
    }

    protected fun <T> launch(block: suspend () -> T) {
        viewModelScope.launch {
            runCatching {
                block()
            }.onFailure {
                if (BuildConfig.DEBUG) {
                    it.printStackTrace()
                }else{
                    CrashReport.postCatchedException(it)
                }
                getApiException(it).apply {
                    withContext(Dispatchers.Main){
                        //统一响应错误信息
                        errorLiveData.value = this@apply
                    }
                }
            }
        }
    }

    /**
     * 捕获异常信息
     */
    private fun getApiException(e: Throwable): ApiException {
        return when (e) {
            is UnknownHostException -> {
                ApiException("网络异常", -100)
            }
            is JSONException -> {//|| e is JsonParseException
                ApiException("数据异常", -100)
            }
            is SocketTimeoutException -> {
                ApiException("连接超时", -100)
            }
            is ConnectException -> {
                ApiException("连接错误", -100)
            }
            is HttpException -> {
                ApiException("http code ${e.code()}", -100)
            }
            is ApiException -> {
                e
            }
            /**
             * 如果协程还在运行，个别机型退出当前界面时，viewModel会通过抛出CancellationException，
             * 强行结束协程，与java中InterruptException类似，所以不必理会,只需将toast隐藏即可
             */
            is CancellationException -> {
                ApiException("", -10)
            }
            else -> {
                ApiException("未知错误", -100)
            }
        }
    }

}