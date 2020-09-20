package com.grupox.wololo.model.externalservices

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.grupox.wololo.configs.properties.PixabayProperties
import com.grupox.wololo.errors.CustomException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.annotation.Cacheable
import org.springframework.context.annotation.PropertySource
import org.springframework.context.annotation.Scope
import org.springframework.context.annotation.ScopedProxyMode
import org.springframework.core.env.Environment
import org.springframework.stereotype.Service
import java.io.File

@Service
@PropertySource("classpath:provinces.properties")
@Scope(proxyMode = ScopedProxyMode.TARGET_CLASS)
class ProvincesService {
    @Autowired
    private lateinit var pixabayProperties: PixabayProperties

    @Autowired
    private lateinit var env: Environment

    @Cacheable(cacheNames = ["withTimeToLive"])
    fun getUrl(provinceName: String): String =
        env.getProperty("${provinceName.toUpperCase().replace(' ', '_')}.url") ?: pixabayProperties.defaultImage

    @Cacheable(cacheNames = ["withTimeToLive"])
    fun availableProvinces(): Either<CustomException, List<String>> {
        val eitherFile = runCatching { File("src/main/resources/provinces.properties") }
                    .fold({ it.right() }, { CustomException.Service.ProvincePropertiesNotAvailableException().left() })

        return eitherFile.map { file -> file.readLines().filter { it.isNotBlank() }.map { formatLine(it) } }
    }

    private fun formatLine(line: String): String =
            line.substringBefore('.')
                    .removeSurrounding(" ")
                    .replace('_', ' ')
                    .toLowerCase()
                    .split(' ')
                    .joinToString(" ") { if (it.length > 3) it.capitalize() else it }
                    .capitalize()
}