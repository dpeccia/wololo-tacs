package com.grupox.wololo.model.helpers

import com.grupox.wololo.configs.properties.MailProperties
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.*
import javax.mail.Message
import javax.mail.Session
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage


@Service
class MailService {

    @Autowired
    private lateinit var mailProperties: MailProperties


    fun getSender() : String {
        return mailProperties.sender
    }
    fun sendMail(sendTo: String) {
        val properties = Properties()
        properties["mail.smtp.host"] = "smtp.gmail.com"
        properties["mail.smtp.port"] = "587"
        properties["mail.smtp.starttls.enable"] = "true"
        properties["mail.smtp.auth"] = "true"
        properties["mail.user"] = mailProperties.sender
        properties["mail.password"] = mailProperties.password

        val session = Session.getInstance(properties, null)

        val mimeMessage = MimeMessage(session)

        mimeMessage.addRecipient(Message.RecipientType.TO, InternetAddress(sendTo))
        mimeMessage.subject = "It's your turn to play! ⚔"
        mimeMessage.setText("Come back to Wololo, your gauchos need you!")

        val transport = session.getTransport("smtp")
        transport.connect("smtp.gmail.com", mailProperties.sender, mailProperties.password)
        transport.sendMessage(mimeMessage, mimeMessage.allRecipients)
        transport.close()

    }


}
