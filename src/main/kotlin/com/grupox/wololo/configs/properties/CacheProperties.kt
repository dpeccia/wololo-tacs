package com.grupox.wololo.configs.properties

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix="cache")
class CacheProperties {
    @Value("\${cache.timeToLive}")
    var timeToLive: Long = 10
}
