package com.pengxh.autodingding.ui.fragment

import com.pengxh.autodingding.BaseViewModel
import com.pengxh.autodingding.utils.MailSender
import com.pengxh.autodingding.utils.SendMailUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MailConfVM : BaseViewModel() {

    fun sendMail(fromEmail: String, fromAuth: String, toEmail: String) = launch {
        withContext(Dispatchers.IO) {
            MailSender().sendTextMail(
                SendMailUtil.createMail(
                    toEmail,
                    "测试邮件",
                    fromEmail,
                    fromAuth
                )
            )
        }
    }
}