package com.grupox.wololo.model.configs

import com.github.benmanes.caffeine.cache.Caffeine
import org.springframework.beans.factory.annotation.Value
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.EnableCaching
import org.springframework.cache.caffeine.CaffeineCacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.concurrent.TimeUnit


@Configuration
@EnableCaching(proxyTargetClass = true)
class CacheConfig(
    private val properties: CacheProperties
) {
    @Bean
    fun caffeine(): Caffeine<Any, Any> =
        Caffeine.newBuilder().expireAfterAccess(properties.timeToLive, TimeUnit.HOURS)

    @Bean
    fun cacheManager(caffeine: Caffeine<Any, Any>): CacheManager {
        val caffeineManager = CaffeineCacheManager("withTimeToLive")
        caffeineManager.setCaffeine(caffeine)
        return caffeineManager
    }
}