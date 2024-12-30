package com.pengxh.autodingding

import android.app.Application
import android.content.Context
import cn.jpush.android.api.JPushInterface
import cn.vove7.andro_accessibility_api.AccessibilityApi
import com.blankj.utilcode.util.CacheDiskUtils
import com.pengxh.autodingding.greendao.DaoMaster
import com.pengxh.autodingding.greendao.DaoMaster.DevOpenHelper
import com.pengxh.autodingding.greendao.DaoSession
import com.pengxh.autodingding.service.BaseAccessibilityService
import com.pengxh.autodingding.service.GestureService
import com.pengxh.autodingding.utils.Utils

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
        CacheDiskUtils.getInstance().put("pushRegId", pushRegId)

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