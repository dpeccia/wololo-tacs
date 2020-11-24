package com.grupox.wololo.model

import com.grupox.wololo.model.helpers.GamesConfigHelper
import com.grupox.wololo.model.helpers.MailService
import java.util.*
import kotlin.concurrent.schedule

class MailManager(val mailSender: MailService){
    fun sendMail(sendTo: String){
        mailSender.sendMail(sendTo)
    }

    fun cancel(mailTask: TimerTask){
        mailTask.cancel()
    }

    fun setTimerForUser(sendTo: String): TimerTask =
            Timer("SettingUp", false).schedule(GamesConfigHelper.getTimeToSendMail()) { sendMail(sendTo) }
}