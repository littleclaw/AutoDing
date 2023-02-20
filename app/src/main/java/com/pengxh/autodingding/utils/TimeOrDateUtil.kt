package com.pengxh.autodingding.utils

import com.blankj.utilcode.util.ToastUtils
import java.text.SimpleDateFormat
import java.util.*

object TimeOrDateUtil {
    private var dateFormat: SimpleDateFormat? = null

    /**
     * 时间戳转日期
     */
    fun rTimestampToDate(millSeconds: Long): String {
        dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.CHINA)
        return dateFormat!!.format(Date(millSeconds))
    }

    /**
     * 时间戳转时间
     */
    fun timestampToTime(millSeconds: Long): String {
        dateFormat = SimpleDateFormat("HH:mm:ss", Locale.CHINA)
        return dateFormat!!.format(Date(millSeconds))
    }

    /**
     * 时间戳转详细日期时间
     */
    fun timestampToDate(millSeconds: Long): String {
        dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA)
        return dateFormat!!.format(Date(millSeconds))
    }

    /**
     * 计算时间差
     *
     * @param fixedTime 结束时间
     */
    fun deltaTime(fixedTime: Long): Long {
        val currentTime = System.currentTimeMillis() / 1000
        if (fixedTime > currentTime) {
            return fixedTime - currentTime
        } else {
            ToastUtils.showLong("时间设置异常")
        }
        return 0L
    }
}