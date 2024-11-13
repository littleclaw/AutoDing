package com.pengxh.autodingding.utils

import com.blankj.utilcode.util.CacheDiskUtils
import com.blankj.utilcode.util.ToastUtils
import com.pengxh.autodingding.bean.MailInfo
import java.io.File

object SendMailUtil {
    fun send(
        toAddress: String?, emailMessage: String
    ) {
        Thread {
            MailSender().sendTextMail(
                createMail(
                    toAddress,
                    emailMessage,
                    CacheDiskUtils.getInstance().getString("senderEmail", "lttclaw@qq.com"),
                    CacheDiskUtils.getInstance().getString("senderAuth", "hwpzapzrkmgpgaba")
                )
            )
        }
            .start()
    }

    @JvmStatic
    fun sendAttachFileEmail(toAddress: String, filePath: String?) {
        val file = File(filePath)
        if (!file.exists()) {
            ToastUtils.showLong("打卡记录不存在，请检查")
            return
        }
        Thread {
            val isSendSuccess = MailSender().sendAccessoryMail(
                createAttachMail(
                    toAddress,
                    file
                )
            )
        }.start()
    }

    fun createMail(
        toAddress: String?,
        emailMessage: String,
        senderEmail: String = "lttclaw@qq.com",
        senderAuth: String = "hwpzapzrkmgpgaba"
    ): MailInfo {
        val mailInfo = MailInfo()
        mailInfo.mailServerHost = "smtp.qq.com" //发送方邮箱服务器
        mailInfo.mailServerPort = "465" //发送方邮箱端口号
        mailInfo.isValidate = true
        mailInfo.userName = senderEmail // 发送者邮箱地址
        mailInfo.password = senderAuth //邮箱授权码，不是密码
        mailInfo.toAddress = toAddress // 接收者邮箱
        mailInfo.fromAddress = senderEmail // 发送者邮箱
        mailInfo.subject = "自动打卡通知" // 邮件主题
        if (emailMessage == "") {
            mailInfo.content =
                "未监听到打卡成功的通知，请手动登录检查" + TimeOrDateUtil.timestampToDate(System.currentTimeMillis()) // 邮件文本
        } else {
            mailInfo.content = emailMessage // 邮件文本
        }
        return mailInfo
    }

    fun createAttachMail(
        toAddress: String,
        file: File,
        senderEmail: String = "lttclaw@qq.com",
        senderAuth: String = "hwpzapzrkmgpgaba"
    ): MailInfo {
        val mailInfo = MailInfo()
        mailInfo.mailServerHost = "smtp.qq.com" //发送方邮箱服务器
        mailInfo.mailServerPort = "465" //发送方邮箱端口号
        mailInfo.isValidate = true
        mailInfo.userName = senderEmail // 发送者邮箱地址
        mailInfo.password = senderAuth //邮箱授权码，不是密码
        mailInfo.toAddress = toAddress // 接收者邮箱
        mailInfo.fromAddress = senderEmail // 发送者邮箱
        mailInfo.subject = "打卡记录" // 邮件主题
        mailInfo.attachFile = file
        return mailInfo
    }
}