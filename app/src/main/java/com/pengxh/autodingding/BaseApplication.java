package com.pengxh.autodingding;

import android.app.Application;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.pengxh.app.multilib.utils.SaveKeyValues;
import com.pengxh.app.multilib.widget.EasyToast;
import com.pengxh.autodingding.greendao.DaoMaster;
import com.pengxh.autodingding.greendao.DaoSession;
import com.pengxh.autodingding.ui.MainActivity;
import com.pengxh.autodingding.utils.Utils;
import com.tencent.bugly.Bugly;
import com.tencent.bugly.beta.Beta;
import com.tencent.bugly.crashreport.CrashReport;

import java.util.HashSet;

import cn.jpush.android.api.JPushInterface;


public class BaseApplication extends Application {

    private static DaoSession daoSession;
    private volatile static BaseApplication application;

    @Override
    public void onCreate() {
        super.onCreate();
        application = this;
        Utils.init(this);
        EasyToast.init(this);
        SaveKeyValues.initSharedPreferences(this);
        initDataBase();
        JPushInterface.setDebugMode(true);
        JPushInterface.init(this);
        HashSet<String> strings = new HashSet<>();
        strings.add("main");
        JPushInterface.setTags(this, 0, strings);
        Log.d("push", JPushInterface.getRegistrationID(this));

        //Bugly初使化
        Bugly.init(this, "84a0dd7960", BuildConfig.DEBUG);
        Beta.autoCheckUpgrade = true;
        Beta.autoDownloadOnWifi = true;
        Beta.largeIconId = R.mipmap.ic_launcher;
        Beta.enableNotification = true;
        Beta.canShowUpgradeActs.add(MainActivity.class);

    }

    /**
     * 双重锁单例
     */
    public static BaseApplication getInstance() {
        if (application == null) {
            synchronized (BaseApplication.class) {
                if (application == null) {
                    application = new BaseApplication();
                }
            }
        }
        return application;
    }

    private void initDataBase() {
        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(this, "DingRecord.db");
        SQLiteDatabase db = helper.getWritableDatabase();
        DaoMaster daoMaster = new DaoMaster(db);
        daoSession = daoMaster.newSession();
    }

    public static DaoSession getDaoSession() {
        return daoSession;
    }
}
