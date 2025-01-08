package com.pengxh.autodingding.service

import android.content.Context
import android.content.Intent
import android.os.PowerManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.blankj.utilcode.util.SPUtils
import com.blankj.utilcode.util.TimeUtils
import com.pengxh.autodingding.BaseApplication
import com.pengxh.autodingding.ui.MainActivity
import com.pengxh.autodingding.utils.launchWithExpHandler
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

class WakeUpWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {
    companion object{
        const val SP_KEY = "wakeUpWorkerLog"
    }
    override fun doWork(): Result {
        checkLog()
        writeLog("开始执行定时唤醒")
        BaseApplication.application?.let {
            wakePhone(it.baseContext)
            val intent = Intent(it, MainActivity::class.java)
            intent.putExtra(MainActivity.EXTRA_ACTION, MainActivity.ACTION_PERIODIC_WAKE)
            it.startActivity(intent)
            return Result.success()
        }
        return Result.failure()
    }

    private fun checkLog(){
        val sp = SPUtils.getInstance()
        val logTxt = sp.getString(SP_KEY, "")
        if(logTxt.lines().size> 2*24*7){ //行数多于7天的会清空一下
            sp.put(SP_KEY, "")
        }
    }
    private fun writeLog(line: String){
        val sp = SPUtils.getInstance()
        val logTxt = sp.getString(SP_KEY, "")
        sp.put(SP_KEY, "${logTxt}${TimeUtils.getNowString()}:${line}\n")
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun wakePhone(context: Context){
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val screenOn = powerManager.isInteractive
        if (!screenOn) {
            //唤醒屏幕
            writeLog("屏幕关闭，唤醒屏幕")
            val wakeLock = powerManager.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
                "autoDing:bright"
            )
            wakeLock.acquire(50000)
        }
    }
}