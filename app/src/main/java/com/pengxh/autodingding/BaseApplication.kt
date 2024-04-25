package com.pengxh.autodingding

import android.app.Application
import android.content.Context
import android.util.Log
import cn.jpush.android.api.JPushInterface
import cn.vove7.andro_accessibility_api.AccessibilityApi
import com.blankj.utilcode.util.CacheDiskUtils
import com.blankj.utilcode.util.ToastUtils
import com.pengxh.autodingding.greendao.DaoMaster
import com.pengxh.autodingding.greendao.DaoMaster.DevOpenHelper
import com.pengxh.autodingding.greendao.DaoSession
import com.pengxh.autodingding.service.BaseAccessibilityService
import com.pengxh.autodingding.service.GestureService
import com.pengxh.autodingding.ui.MainActivity
import com.pengxh.autodingding.utils.Utils
import com.tencent.bugly.Bugly
import com.tencent.bugly.beta.Beta

class BaseApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        application = this
        Utils.init(this)
        initDataBase()
        JPushInterface.setDebugMode(true)
        JPushInterface.init(this)
        val strings = HashSet<String>()
        strings.add("main")
        JPushInterface.setTags(this, 0, strings)
        val pushRegId = JPushInterface.getRegistrationID(this)
        Log.d("push", pushRegId)
        CacheDiskUtils.getInstance().put("pushRegId", pushRegId)

        //Bugly初使化
        Bugly.init(this, "84a0dd7960", BuildConfig.DEBUG)
        Beta.autoCheckUpgrade = true
        Beta.autoDownloadOnWifi = true
        Beta.largeIconId = R.mipmap.ic_launcher
        Beta.enableNotification = true
        Beta.canShowUpgradeActs.add(MainActivity::class.java)

        //辅助服务
        AccessibilityApi.apply {
            BASE_SERVICE_CLS = BaseAccessibilityService::class.java
            GESTURE_SERVICE_CLS = GestureService::class.java
        }
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        application = this;
    }

    private fun initDataBase() {
        val helper = DevOpenHelper(this, "DingRecord.db")
        val db = helper.writableDatabase
        val daoMaster = DaoMaster(db)
        daoSession = daoMaster.newSession()
    }

    companion object {
        @JvmStatic
        var daoSession: DaoSession? = null
            private set

        @Volatile
        var application: BaseApplication? = null
    }
}