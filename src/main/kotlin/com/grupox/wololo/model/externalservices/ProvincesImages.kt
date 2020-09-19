package com.grupox.wololo.model.externalservices

import arrow.core.Either
import arrow.core.rightIfNotNull
import com.grupox.wololo.errors.CustomException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.PropertySource
import org.springframework.core.env.Environment
import org.springframework.stereotype.Service

@Service
@PropertySource("classpath:provinces.properties")
class ProvincesImages {
    @Autowired
    private lateinit var env: Environment

    fun getUrl(provinceName: String): Either<CustomException.Service.InvalidExternalResponseException, String> =
        env.getProperty("${provinceName.toUpperCase().replace(' ', '_')}.url")?.removeSurrounding("\"").rightIfNotNull { CustomException.Service.InvalidExternalResponseException("") }
}