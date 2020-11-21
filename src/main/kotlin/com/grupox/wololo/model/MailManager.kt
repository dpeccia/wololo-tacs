package com.grupox.wololo.model

import com.grupox.wololo.model.helpers.MailSender
import java.util.*
import kotlin.concurrent.schedule


class MailManager(val mailSender: MailSender){
    fun sendMail(sendTo: String){
        mailSender.sendMail(sendTo)
    }

    fun cancel(mailTask: TimerTask){
        mailTask.cancel()
    }

    fun setTimerForUser(sendTo: String): TimerTask{
        val mailTask: TimerTask = Timer("SettingUp", false).schedule(1800000) {
            sendMail(sendTo)
        }
        return mailTask }

}