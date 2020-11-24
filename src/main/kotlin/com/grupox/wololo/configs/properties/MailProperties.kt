package com.grupox.wololo.configs.properties

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.PropertySource

@Configuration
@ConfigurationProperties(prefix = "mail")
@PropertySource("classpath:services.properties")
class MailProperties {
    @Value("\${mail.sender}")
    lateinit var sender: String
    @Value("\${mail.password}")
    lateinit var password: String

}