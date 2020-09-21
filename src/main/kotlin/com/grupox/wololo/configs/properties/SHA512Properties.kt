package com.grupox.wololo.configs.properties

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.PropertySource

@Configuration
@ConfigurationProperties(prefix = "hash")
@PropertySource("classpath:application.properties")
class SHA512Properties {
    @Value("\${hash.salt}")
    lateinit var salt: String
}