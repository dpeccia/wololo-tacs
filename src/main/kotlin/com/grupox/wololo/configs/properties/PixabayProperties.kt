package com.grupox.wololo.configs.properties

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.PropertySource

@Configuration
@ConfigurationProperties(prefix = "pixabay")
@PropertySource("classpath:services.properties")
class PixabayProperties {
    @Value("\${pixabay.apiKey}")
    lateinit var apiKey: String

    @Value("\${pixabay.defaultImage}")
    lateinit var defaultImage: String
}