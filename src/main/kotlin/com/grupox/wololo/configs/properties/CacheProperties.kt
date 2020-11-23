package com.grupox.wololo.configs.properties

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.PropertySource

@Configuration
@ConfigurationProperties(prefix="cache")
@PropertySource("classpath:services.properties")
class CacheProperties {
    @Value("\${cache.timeToLive}")
    var timeToLive: Long = 10
}
