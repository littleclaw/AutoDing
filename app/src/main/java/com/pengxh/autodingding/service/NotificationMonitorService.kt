package com.pengxh.autodingding.service

import android.app.Notification
import android.content.ComponentName
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.drake.channel.sendEvent
import com.pengxh.autodingding.BaseApplication
import com.pengxh.autodingding.bean.HistoryRecordBean
import com.pengxh.autodingding.greendao.HistoryRecordBeanDao
import com.pengxh.autodingding.ui.fragment.SettingsFragment
import com.pengxh.autodingding.utils.TimeOrDateUtil
import java.util.*

class NotificationMonitorService : NotificationListenerService() {
    private var recordBeanDao: HistoryRecordBeanDao? = null

    /**
     * 有可用的并且和通知管理器连接成功时回调
     */
    override fun onListenerConnected() {
        recordBeanDao = BaseApplication.getDaoSession().historyRecordBeanDao
    }

    /**
     * 当有新通知到来时会回调
     */
    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val extras = sbn.notification.extras
        // 获取接收消息APP的包名
        val packageName = sbn.packageName
        // 获取接收消息的内容
        val notificationText = extras.getString(Notification.EXTRA_TEXT)
        if (packageName == "com.alibaba.android.rimet") {
            if (notificationText == null || notificationText == "") {
                return
            }
            if (notificationText.contains("考勤打卡")) {
                //保存打卡记录
                val bean = HistoryRecordBean()
                bean.uuid = UUID.randomUUID().toString()
                bean.date = TimeOrDateUtil.timestampToDate(System.currentTimeMillis())
                bean.message = notificationText
                recordBeanDao!!.save(bean)
                //通知发送邮件和更新界面
                sendEvent(notificationText)
                SettingsFragment.sendEmptyMessage()
            }
        }
    }

    /**
     * 当有通知移除时会回调
     */
    override fun onNotificationRemoved(sbn: StatusBarNotification) {}
    override fun onListenerDisconnected() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            // 通知侦听器断开连接 - 请求重新绑定
            requestRebind(ComponentName(this, NotificationListenerService::class.java))
        }
    }

    companion object {
        private const val TAG = "MonitorService"
    }
}