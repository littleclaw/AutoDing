package com.pengxh.autodingding.bean

import java.io.File
import java.util.Properties

/**
 * @author: lttclaw
 * @email: lttclaw@qq.com
 * @description: 邮件相关
 * @date: 2020/1/16 15:40
 */
class MailInfo {
    // 发送邮件的服务器的IP和端口
    var mailServerHost: String? = null
    var mailServerPort: String? = null

    // 邮件发送者的地址
    var fromAddress: String? = null

    // 邮件接收者的地址
    var toAddress: String? = null

    // 登陆邮件发送服务器的用户名和密码
    var userName: String? = null
    var password: String? = null

    // 是否需要身份验证
    var isValidate = false

    // 邮件主题
    var subject: String? = null

    // 邮件的文本内容
    var content: String? = null

    // 邮件的附件
    var attachFile: File? = null

    // 邮件附件的文件名
    var attachFileName: String? = null
    val properties: Properties
        /**
         * 获得邮件会话属性
         */
        get() {
            val p = Properties()
            p["mail.smtp.host"] = mailServerHost
            p["mail.smtp.port"] = mailServerPort
            p["mail.smtp.ssl.enable"] = "true"
            p["mail.smtp.auth"] = "true"
            return p
        }
}